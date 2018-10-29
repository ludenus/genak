package genak


import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
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
        val sendPerMs = rateRps / 1000
        val tick = ticker(delayMillis = 1, initialDelayMillis = 0)


        val totalTime = measureTimeMillis {
            val channel = Channel<Deferred<Pair<ListenableFuture<Response>, Long>>>(1000)

            val sender = launch(newSingleThreadContext("sender")) {
                while (isActive) {
                    withTimeoutOrNull(100) { tick.receive() }?.let {
                        repeat(sendPerMs) {
                            channel.send(
                                    async {
                                        markTime {
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
                    val (fulfilled, beginMs) = promise.await()
                    val response = fulfilled.get()
                    val endMs = System.currentTimeMillis()
                    measurement(response, Timings(beginMs, endMs), sessionTag).reportHttp()
                    fulfilledCount.getAndIncrement()
                }
            }

            log.info("waiting....")
            delay(100_000)
            log.info("canceling...")
            sender.cancelAndJoin()
            receiver.cancelAndJoin()
            channel.cancel()
            channel.close()
            log.info("done")
        }
        log.info("sent {}, received {} totalTime {} msec, rate {} rps ", promisedCount, fulfilledCount.get().toInt(), totalTime, fulfilledCount.get().toInt() / (totalTime / 1000))
    }
}
