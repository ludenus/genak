package genak

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

val log = LoggerFactory.getLogger("Ext")

inline fun <V> time(lambda: () -> V): Pair<V, Timings> {

    val begin = System.currentTimeMillis()
    val result = lambda()
    val end = System.currentTimeMillis()

    return Pair(result, Timings(begin, end))
}

fun <V> CoroutineScope.spawnList(i: Int, lambda: () -> V): List<Deferred<Pair<V, Timings>>> = (1..i).map {
    async {
        time { lambda() }
    }
}

fun <V> spawnSequence(i: Int, lambda: () -> V): Sequence<Deferred<Pair<V, Timings>>> = (1..i).asSequence().map {
    async {
        time { lambda() }
    }
}


suspend fun <V> Channel<Deferred<Pair<V, Timings>>>.spawnToChannel(i: Int, lambda: () -> V) = repeat(i) {
    print("${i}")
    this.send(
            async {
                time { lambda() }
            }
    )
}


//suspend fun <V> spawnAndAwaitAll(i: Int, lambda: () -> V) = spawnSequence(i, lambda).map { it.await() }

inline fun timestamp() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())


inline fun logProgressTime(i: Int, total: Int, tag: String = "", periodInMilliseconds: Long = 1000) {
    if (0L == System.currentTimeMillis() % periodInMilliseconds) {
        log.info("{}: {} / {}", tag, i, total)
    }
}

inline fun logProgressPart(i: Int, total: Int, tag: String = "", parts: Int = 100) {
    if (0 == i % (total / parts)) {
        log.info("{}: {} / {}", tag, i, total)
    }
}

inline fun measurement(resTime: Pair<FuelRes, Timings>) = measurement(resTime.first, resTime.second)

inline fun measurement(result: FuelRes, timings: Timings) =
        Point.measurement("timing")
                .time(timings.endMs, TimeUnit.MILLISECONDS)
                .addField("elapsedMs", timings.elapsedMs)
                .addField("endMs", timings.endMs)
                .addField("result", result.resolve())
//                .addField("res2",result.component2().toString())
                .build()

