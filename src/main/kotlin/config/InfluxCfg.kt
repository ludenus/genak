package config

data class InfluxCfg(
        var url: String = "http://localhost:8086",
        var user: String = "dbuser",
        var pass: String = "********",
        var dbName: String = "qadb"
)