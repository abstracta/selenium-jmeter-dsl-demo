        influxDbListener(String.format("%s/api/v2/write?org=%s&bucket=%s",
            envConfig.get("INFLUX_URI"), envConfig.get("INFLUX_ORG"),
            envConfig.get("INFLUX_BUCKET")))
            .token(envConfig.get("INFLUX_TOKEN"))
