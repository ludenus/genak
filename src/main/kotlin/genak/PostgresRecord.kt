package genak


data class PostgresRecord(
        var beginMs: Long = 0L,
        var endMs: Long = 0L,
        var elapsedMs: Long = 0L,
        var session: String = "",
        var result: String = "",
        var tags: Map<String, String> = emptyMap(),
        var message: String = ""
)
