package genak

import com.github.kittinunf.fuel.httpGet
import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import config.InfluxCfg
import config.PongCfg
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import java.util.concurrent.atomic.AtomicLong


open class PongBase {

    val log = LoggerFactory.getLogger(this.javaClass.name)

    val config by lazy { ConfigFactory.load() }
    val influxCfg by lazy { ConfigBeanFactory.create(config.getConfig("influxdb"), InfluxCfg::class.java) }
    val pongCfg by lazy { ConfigBeanFactory.create(config.getConfig("pong"), PongCfg::class.java) }

    open val sessionTag = timestamp()

    val promisedCount = AtomicLong(0)
    val fulfilledCount = AtomicLong(0)

    lateinit var influxDb: InfluxDB

    inline fun url(id: Long) = "${pongCfg.url}/ping/${id}"

    inline fun wget(id: Long) = FuelRes(url(id).httpGet().responseString())

    inline fun Point.reportHttp() = influxDb.write(this)
    inline fun Point.reportUdp() = influxDb.write(influxCfg.udpPort, this)

    @BeforeClass
    fun init() {
        log.info("sessionTag: {}", sessionTag)
        influxDb = InfluxDBFactory.connect(influxCfg.url, influxCfg.user, influxCfg.pass)
        influxDb.enableGzip()
        influxDb.setDatabase(influxCfg.dbName)
        influxDb.enableBatch(BatchOptions.DEFAULTS.actions(influxCfg.flushEveryPoints).flushDuration(influxCfg.flushEveryMsec))
//        val query = Query("SELECT idle FROM cpu", dbName)
//        influxDb.query(query)
    }

    @AfterClass
    fun cleanup() {
        influxDb.flush()
        influxDb.close()
    }

}
