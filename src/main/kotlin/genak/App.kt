/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package genak


import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*


fun <V> spawn(i: Int, lambda: () -> V): List<Deferred<Pair<V, Timings>>> = (1..i).map {
    async {
        time { lambda() }
    }
}

suspend fun <V> spawnAndAwaitAll(i: Int, lambda: () -> V) = spawn(i, lambda).map { it.await() }


inline fun timestamp() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())

suspend fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger("main")

    val (result, totalTime) = time {
        spawn(100) { timestamp() }.map { it.await() }
    }
    val zeroMsCount = result.filter { it.second.elapsedMs == 0L }.count()

    log.info("totalTime {}, zeroMsCount {}", totalTime, zeroMsCount)
}