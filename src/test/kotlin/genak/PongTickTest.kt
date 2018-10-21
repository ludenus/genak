package genak

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ticker
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


class PongTickTest : PongBase() {

    val aHttp = AHttp()

    @Test
    fun tickTest() = runBlocking {
        log.info("sessionTag: {}", sessionTag)

        val rateRps = 5000
        val sendPerMs = rateRps / 100
        val tick = ticker(delayMillis = 10, initialDelayMillis = 0)


        val totalTime = measureTimeMillis {
            val channel = Channel<Deferred<Pair<ListenableFuture<Response>, Timings>>>(1000)

            val sender = launch(newSingleThreadContext("sender")) {
                while (isActive) {
                    withTimeoutOrNull(100) { tick.receive() }?.let{
                        repeat(sendPerMs) {
                            channel.send(
                                    async {
                                        time {
                                            aHttp.httpGetFuture(url(promisedCount.getAndIncrement()))
                                        }
                                    }
                            )
                        }
                    }

                }
            }

            val receiver = launch(newSingleThreadContext("receiver")) {
                for (promise in channel) {
//                        val promise = channel.receive()
                    val fulfilled = promise.await()
//                    measurement(fulfilled.first.get(), fulfilled.second, sessionTag).reportFile()
                    measurement(fulfilled.first.get(), fulfilled.second, sessionTag).reportHttp()

                    fulfilledCount.getAndIncrement()
                }
            }

            log.info("waiting....")
            delay(100, TimeUnit.SECONDS)
            log.info("canceling...")
            sender.cancel()
            receiver.cancelAndJoin()
            channel.cancel()
            channel.close()
            log.info("done")
        }
        log.info("sent {}, received {} totalTime {} msec, rate {} rps ", promisedCount, fulfilledCount.get().toInt(), totalTime, fulfilledCount.get().toInt() / (totalTime / 1000))
    }
}
