/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package genak

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import org.asynchttpclient.Response
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


class PongTime2Test : PongBase() {

    val aHttp = AHttp()

    @Test
    fun timeTest() = runBlocking {
        log.info("begin")
        log.info("sessionTag: {}", sessionTag)

        val totalTime = measureTimeMillis {
            val channel = Channel<Deferred<Pair<Response, Timings>>>(1000)

            val sender = launch(newSingleThreadContext("sender")) {
                while (isActive) {
                    channel.send(
                            async {
                                time {

                                    aHttp.httpGet(url(promisedCount.getAndIncrement()))
                                }
                            }
                    )
                }
            }

            val receiver = launch(newSingleThreadContext("receiver")) {
                for (promise in channel) {
//                        val promise = channel.receive()
                    val fulfilled = promise.await()
                    measurement(fulfilled.first, fulfilled.second, sessionTag).reportHttp()
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