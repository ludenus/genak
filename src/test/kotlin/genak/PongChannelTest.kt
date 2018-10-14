/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package genak

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.testng.annotations.Test
import kotlin.system.measureTimeMillis


class PongChannelTest : PongBase() {

    @Test
    fun channelTest() = runBlocking {
        log.info("begin")
        val total = 100_000
        val totalTime = measureTimeMillis {
            val channel = Channel<Deferred<Pair<FuelRes, Timings>>>(total)

            launch {
                for (i in 1..total) {
                    channel.send(
                            async {
                                time {
                                    logProgressPart(i, total, "promised", 10)
                                    wget(id.getAndIncrement())
                                }
                            }
                    )
                }
            }

            for (i in 1..total) {
                val promise = channel.receive()
                logProgressPart(i, total, "promised", 10)

                val fulfilled = promise.await()
                measurement(fulfilled).reportHttp()
            }

            channel.cancel()
            channel.close()
        }
        log.info("totalCount {} totalTime {} msec, rate {} rps ", total, totalTime, total / (totalTime / 1000))
    }
}
