package genak

import kotlinx.coroutines.experimental.runBlocking
import org.testng.annotations.Test
import kotlin.system.measureTimeMillis


class PongListTest : PongBase() {

    @Test
    fun listTest() = runBlocking {
        log.info("sessionTag: {}", sessionTag)
        val total = 100_000
        val totalTime = measureTimeMillis {

            val (promises, promisesMs) = time {
                spawnList(total) {
                    wget(promisedCount.getAndIncrement())
                }
            }

            val (fulfilled, fulfilledMs) = time {
                promises.map {
                    it.await()
                }
            }

            val (_, reportMs) = time {
                fulfilled.forEach {
                    measurement(it, sessionTag).reportHttp()
//                    measurement(it,sessionTag).reportFile()
                }
            }

            val reportOverhead = 100 * reportMs.elapsedMs / fulfilledMs.elapsedMs
            log.info("promisesMs {} fulfilledMs {}, reportMs {} ({}%)", promisesMs.elapsedMs, fulfilledMs.elapsedMs, reportMs.elapsedMs, reportOverhead)
        }
        log.info("totalCount {} totalTime {} msec, rate {} rps ", total, totalTime, total / (totalTime / 1000))
    }
}
