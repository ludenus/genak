package genak

import kotlinx.coroutines.experimental.runBlocking
import org.testng.annotations.Test
import kotlin.system.measureTimeMillis


class PongSequenceTest : PongBase() {

    @Test
    fun sequenceTest() = runBlocking {
        log.info("sessionTag: {}", sessionTag)
        val total = 1_000_000
        val totalTime = measureTimeMillis {

            spawnSequence(total) {
                wget(promisedCount.getAndIncrement())
            }.mapIndexed { i, promise ->
                logProgressPart(i, total, "promised", 10)
                // logProgressTime(i, total, "promise", 1000)
                promise
            }.map {
                runBlocking { it.await() }
            }.mapIndexed { i, fulfilled ->
                logProgressPart(i, total, "fulfilled", 10)
                measurement(fulfilled).reportHttp()
                fulfilled
            }.count()//.toList()
        }
        log.info("totalCount {} totalTime {} msec, rate {} rps ", total, totalTime, total / (totalTime / 1000))
    }

}
