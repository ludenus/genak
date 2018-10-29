package genak

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


class PongTime2Test : PongBase() {

    val aHttp = AHttp()

    @Test
    fun timeTest() = runBlocking {
        log.info("sessionTag: {}", sessionTag)

        val totalTime = measureTimeMillis {
            val channel = Channel<Deferred<Pair<ListenableFuture<Response>, Timings>>>(1000)

            val sender = launch(newSingleThreadContext("sender")) {
                while (isActive) {
                    channel.send(
                            async {
                                time {
                                    val promised = aHttp.httpGetFuture(url(promisedCount.getAndIncrement()))
//                                    logMsec("${fulfilledCount} / ${promisedCount}", "promised", 10000)
                                    promised
                                }
                            }
                    )
                }
            }

            val receiver = launch(newSingleThreadContext("receiver")) {
                for (promise in channel) {
//                        val promise = channel.receive()
                    val fulfilled = promise.await()
                    measurement(fulfilled.first.get(), fulfilled.second, sessionTag).reportFile()
//                    measurement(fulfilled.first.get(), fulfilled.second, sessionTag).reportHttp()
//                    postgresReporter.addRow(record(fulfilled.first.get(), fulfilled.second, sessionTag))

                    fulfilledCount.getAndIncrement()
//                    logMsec("${fulfilledCount} / ${promisedCount}", "fulfilled", 10000)
                }
            }

            log.info("waiting....")
            delay(100_000)
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
