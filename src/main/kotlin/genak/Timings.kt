package genak

inline fun <V> time(lambda: () -> V): Pair<V, Timings> {

    val begin = System.currentTimeMillis()
    val result = lambda()
    val end = System.currentTimeMillis()

    return Pair(result, Timings(begin, end))
}

data class Timings(val beginMs: Long, val endMs: Long) {

    val elapsedMs by lazy { endMs - beginMs }

    override fun toString(): String {
        return "Timings(beginMs=$beginMs, endMs=$endMs, elapsedMs=$elapsedMs)"
    }

}