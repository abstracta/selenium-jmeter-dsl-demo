package us.abstracta.selenium.jmeterdsl.execution;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

public class InstrumentedTelemetryCollector implements AutoCloseable {

  public static final ThreadLocal<TelemetryCollector> COLLECTOR = ThreadLocal.withInitial(
      TelemetryCollector::new);
  private String testClassName;
  private String testMethod;
  private String basePageObject;

  public InstrumentedTelemetryCollector testMethod(String className, String testMethod) {
    this.testClassName = className;
    this.testMethod = testMethod;
    return this;
  }

  public InstrumentedTelemetryCollector basePageObject(String basePageObject) {
    this.basePageObject = basePageObject;
    return this;
  }

  public InstrumentedTelemetryCollector start() {
    instrumentClassMethod(testClassName, b -> b.method(ElementMatchers.named(testMethod)),
        TestMethodCollector.class);
    instrumentClassMethod(basePageObject, b -> b.constructor(ElementMatchers.any()),
        PageObjectCollector.class);
    return this;
  }

  private <T> void instrumentClassMethod(String className,
      Function<DynamicType.Builder<T>, ImplementationDefinition<T>> methodMatcher,
      Class<?> advice) {
    TypePool typePool = TypePool.Default.ofSystemLoader();
    DynamicType.Builder<T> rebaseClassBuilder = new ByteBuddy()
        .rebase(typePool.describe(className).resolve(),
            ClassFileLocator.ForClassLoader.ofSystemLoader());
    methodMatcher.apply(rebaseClassBuilder)
        .intercept(Advice.to(advice))
        .make()
        .load(ClassLoader.getSystemClassLoader(), Default.INJECTION);
  }

  public void reset() {
    COLLECTOR.get().close();
      /*
       we just recreate to avoid issues of keeping context. For instance, metric provider keep
       publishing "old" registered meters.
       */
    COLLECTOR.set(new TelemetryCollector());
  }

  @Override
  public void close() {
    COLLECTOR.get().close();
  }

  public static TelemetryStep startStep(String name) {
    return COLLECTOR.get().startStep(name);
  }

  public static class TestMethodCollector {

    public static final ThreadLocal<TelemetryStep> TEST_STEP = ThreadLocal.withInitial(() -> null);

    @Advice.OnMethodEnter
    public static void startMethod(@Advice.Origin Method origin) {
      TEST_STEP.set(startStep(origin.getName()));
    }

    @Advice.OnMethodExit(onThrowable = Exception.class)
    public static void endMethod() {
      try {
        PageObjectCollector.endLast();
        TEST_STEP.get().close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  public static class PageObjectCollector {

    public static final ThreadLocal<TelemetryStep> PAGE_STEP = ThreadLocal.withInitial(() -> null);

    @Advice.OnMethodEnter
    public static void newPage(@Advice.Origin Constructor<?> origin) {
      Optional.ofNullable(PAGE_STEP.get())
          .ifPresent(TelemetryStep::close);
      PAGE_STEP.set(startStep(extractPageName(origin)));
    }

    public static String extractPageName(Constructor<?> origin) {
      /*
      We use stack trace to get the specific class name (subclass of the base webpage that was
      created), we didn't find a simpler way with bytebuddy to get it (@Advice.This is not
      supported)
      Additionally, we start at index 3 since first three are the Tread.getStackTrace call,
      PageObjectObserver.extractPageName, and origin.getDeclaringClass().constructor.
       */
      return extractSpecificClassConstruction(3, Thread.currentThread().getStackTrace(),
          origin.getDeclaringClass()).getSimpleName();
    }

    private static Class<?> extractSpecificClassConstruction(int stackIndex,
        StackTraceElement[] stackTrace,
        Class<?> parentClass) {
      try {
        Class<?> stackPositionClass = Class.forName(stackTrace[stackIndex].getClassName());
        return !parentClass.isAssignableFrom(stackPositionClass)
            ? parentClass
            : extractSpecificClassConstruction(stackIndex + 1, stackTrace, stackPositionClass);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    public static void endLast() {
      Optional.ofNullable(PAGE_STEP.get())
          .ifPresent(TelemetryStep::close);
    }

  }

}
