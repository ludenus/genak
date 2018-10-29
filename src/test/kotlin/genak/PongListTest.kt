package genak

import kotlinx.coroutines.runBlocking
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

            val (points, pointsMs) = time {
                fulfilled.map {
                    measurement(it, sessionTag)
//                    record(it,sessionTag)
                }
            }

            val (_, reportMs) = time {
                points.map {
//                    it.reportUdp()
                    it.reportHttp()
//                    it.reportFile()
//                    postgresReporter.addRow(it)
                }
            }

            val totalMs = promisesMs.elapsedMs + fulfilledMs.elapsedMs + pointsMs.elapsedMs + reportMs.elapsedMs

            log.info("totalMs {} = promisesMs {} + fulfilledMs {} + pointsMs {} + reportMs {}", totalMs, promisesMs.elapsedMs, fulfilledMs.elapsedMs, pointsMs.elapsedMs, reportMs.elapsedMs)
            log.info("promisesMs/totalMs {}%,  promisesMs/fulfilledMs {}%", 100 * promisesMs.elapsedMs / totalMs, 100 * promisesMs.elapsedMs / fulfilledMs.elapsedMs)
            log.info("pointsMs/totalMs {}%,  pointsMs/fulfilledMs {}%", 100 * pointsMs.elapsedMs / totalMs, 100 * pointsMs.elapsedMs / fulfilledMs.elapsedMs)
            log.info("reportMs/totalMs {}%,  reportMs/fulfilledMs {}%", 100 * reportMs.elapsedMs / totalMs, 100 * reportMs.elapsedMs / fulfilledMs.elapsedMs)

        }
        log.info("totalCount {} totalTime {} msec, rate {} rps ", total, totalTime, total / (totalTime / 1000))
    }
}
