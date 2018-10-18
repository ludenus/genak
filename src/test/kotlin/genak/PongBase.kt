package genak

import com.github.kittinunf.fuel.httpGet
import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import config.InfluxCfg
import config.PongCfg
import config.PostgresCfg
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.atomic.AtomicLong


open class PongBase {

    val log = LoggerFactory.getLogger(this.javaClass.name)

    val config by lazy { ConfigFactory.load() }
    val influxCfg by lazy { ConfigBeanFactory.create(config.getConfig("influxdb"), InfluxCfg::class.java) }
    val pongCfg by lazy { ConfigBeanFactory.create(config.getConfig("pong"), PongCfg::class.java) }
    val postgresCfg by lazy { ConfigBeanFactory.create(config.getConfig("postgres"), PostgresCfg::class.java) }

    open val sessionTag = timestamp()

    val promisedCount = AtomicLong(0)
    val fulfilledCount = AtomicLong(0)

    lateinit var influxDb: InfluxDB
    lateinit var postgresConnection: Connection
    lateinit var postgresReporter: PostgresReporter

    val reportFile by lazy { createTempFile("report.gnk") }
    val reportWriter by lazy { reportFile.bufferedWriter(bufferSize = 1 * 1024 * 1024) }

    inline fun url(id: Long) = "${pongCfg.url}/ping/${id}"

    inline fun wget(id: Long) = FuelRes(url(id).httpGet().responseString())

    inline fun Point.reportHttp() = influxDb.write(this)
    inline fun Point.reportUdp() = influxDb.write(influxCfg.udpPort, this)

    inline fun Point.reportFile() = reportWriter.write("${this}\n")


    @BeforeClass
    fun init() {
        influxDb = InfluxDBFactory.connect(influxCfg.url, influxCfg.user, influxCfg.pass)
        influxDb.enableGzip()
        influxDb.setDatabase(influxCfg.dbName)
        influxDb.enableBatch(BatchOptions.DEFAULTS.actions(influxCfg.flushEveryPoints).flushDuration(influxCfg.flushEveryMsec))
//        val query = Query("SELECT idle FROM cpu", dbName)
//        influxDb.query(query)
        postgresConnection = DriverManager.getConnection(postgresCfg.url)
        postgresConnection.autoCommit = true
        postgresReporter = PostgresReporter(postgresConnection, reCreateTable = false)

    }

    @AfterClass
    fun cleanup() {
        postgresReporter.flush()
        postgresConnection.close()
        log.info("reportFile {}", reportFile.canonicalPath)
        reportWriter.flush()
        reportWriter.close()
        influxDb.flush()
        influxDb.close()
    }


}
