package genak

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import org.asynchttpclient.Response
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

inline fun timestamp() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(Date())

var nextLogMsec = System.currentTimeMillis() + 1000

inline fun logMsec(msg: Any, tag: String = "", periodInMilliseconds: Long = 1000) {
    val now = System.currentTimeMillis()
    if (now >= nextLogMsec) {
        log.info("{}: {}", tag, msg)
        nextLogMsec = now + periodInMilliseconds
    }
}

inline fun logProgressPart(i: Int, total: Int, tag: String = "", parts: Int = 100) {
    if (0 == i % (total / parts)) {
        log.info("{}: {} / {}", tag, i, total)
    }
}

val objectMapper by lazy { ObjectMapper() }
fun Any.toJson() = objectMapper.writeValueAsString(this)

inline fun record(resTime: Pair<FuelRes, Timings>, tag: String): PostgresRecord {
    //beginMs, endMs, elapsedMs, session, result, tags, message
    val (res, tim) = resTime
    return PostgresRecord(
            beginMs = tim.beginMs,
            endMs = tim.endMs,
            elapsedMs = tim.elapsedMs,
            session = tag,
            result = if (res.response.statusCode == 200) {
                "OK"
            } else {
                "KO"
            },
            tags = mapOf("session" to tag),
            message = res.response.responseMessage
    )

}

inline fun record(res: Response, tim: Timings, tag: String) = PostgresRecord(
        beginMs = tim.beginMs,
        endMs = tim.endMs,
        elapsedMs = tim.elapsedMs,
        session = tag,
        result = if (res.statusCode == 200) {
            "OK"
        } else {
            "KO"
        },
        tags = mapOf("session" to tag),
        message = res.responseBody
)


inline fun measurement(resTime: Pair<FuelRes, Timings>, tag: String) = measurement(resTime.first, resTime.second, tag)

inline fun measurement(response: Response, timings: Timings, tag: String) =
        Point.measurement("timing")
                .time(timings.endMs, TimeUnit.MILLISECONDS)
                .tag("session", tag)
                .addField("elapsedMs", timings.elapsedMs)
                .addField("endMs", timings.endMs)
                .addField("code", response.statusCode)
                .addField("result", response.responseBody)
//                .addField("res2",result.component2().toString())
                .build()


inline fun measurement(result: FuelRes, timings: Timings, tag: String) =
        Point.measurement("timing")
                .time(timings.endMs, TimeUnit.MILLISECONDS)
                .tag("session", tag)
                .addField("elapsedMs", timings.elapsedMs)
                .addField("endMs", timings.endMs)
                .addField("code", result.response.statusCode)
                .addField("result", result.resolve())
//                .addField("res2",result.component2().toString())
                .build()

