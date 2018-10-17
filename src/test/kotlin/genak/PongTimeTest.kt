package genak

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


class PongTimeTest : PongBase() {

    @Test
    fun timeTest() = runBlocking {
        log.info("sessionTag: {}", sessionTag)

        val totalTime = measureTimeMillis {
            val channel = Channel<Deferred<Pair<FuelRes, Timings>>>(1000)

            val sender = launch(newSingleThreadContext("sender")) {
                while (isActive) {
                    channel.send(
                            async {
                                time { wget(promisedCount.getAndIncrement()) }
                            }
                    )
                }
            }

            val receiver = launch(newSingleThreadContext("receiver")) {
                for (promise in channel) {
//                        val promise = channel.receive()
                    val fulfilled = promise.await()
                    measurement(fulfilled, sessionTag).reportHttp()
                    fulfilledCount.getAndIncrement()
//                    logMsec("${fulfilledCount.getAndIncrement()} / $promisedCount", "fulfilled", 1000) //                    delay(11)
                }
            }

            log.info("waiting....")
            delay(30, TimeUnit.SECONDS)
            log.info("canceling...")
            sender.cancel()
            receiver.cancel()
            channel.cancel()
            channel.close()
            log.info("done")
        }
        log.info("sent {}, received {} totalTime {} msec, rate {} rps ", promisedCount, fulfilledCount.get().toInt(), totalTime, fulfilledCount.get().toInt() / (totalTime / 1000))
    }
}
