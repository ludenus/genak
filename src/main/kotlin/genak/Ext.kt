package genak

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.text.SimpleDateFormat
import java.util.*

inline fun <V> time(lambda: () -> V): Pair<V, Timings> {

    val begin = System.currentTimeMillis()
    val result = lambda()
    val end = System.currentTimeMillis()

    return Pair(result, Timings(begin, end))
}

fun <V> spawn(i: Int, lambda: () -> V): Sequence<Deferred<Pair<V, Timings>>> = (1..i).asSequence().map {
    async {
        time { lambda() }
    }
}


//suspend fun <V> spawnAndAwaitAll(i: Int, lambda: () -> V) = spawn(i, lambda).map { it.await() }

inline fun timestamp() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())

