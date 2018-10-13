package genak

data class Timings(val beginMs: Long, val endMs: Long) {

    val elapsedMs by lazy { endMs - beginMs }

    override fun toString(): String {
        return "Timings(beginMs=$beginMs, endMs=$endMs, elapsedMs=$elapsedMs)"
    }

}