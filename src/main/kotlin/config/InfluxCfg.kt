package config

data class InfluxCfg(
        var url: String = "http://localhost:8086",
        var udpPort: Int = 8189,
        var user: String = "dbuser",
        var pass: String = "********",
        var dbName: String = "qadb",
        var flushEveryPoints: Int = 2000,
        var flushEveryMsec: Int = 1000
)