package config

data class PostgresCfg(
        var url: String = "jdbc:postgresql://localhost:5132/qadb?user=postgres&password=********"
)