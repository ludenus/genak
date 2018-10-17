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
import java.sql.PreparedStatement
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
    lateinit var postgres: Connection
    lateinit var insertTimingsStm: PreparedStatement

    val reportFile by lazy { createTempFile("report.gnk") }
    val reportWriter by lazy { reportFile.bufferedWriter(bufferSize = 1 * 1024 * 1024) }

    inline fun url(id: Long) = "${pongCfg.url}/ping/${id}"

    inline fun wget(id: Long) = FuelRes(url(id).httpGet().responseString())

    inline fun Point.reportHttp() = influxDb.write(this)
    inline fun Point.reportUdp() = influxDb.write(influxCfg.udpPort, this)

    inline fun Point.reportFile() = reportWriter.write("${this}\n")

    inline fun Point.reportDb() {

        insertTimingsStm.setString(1, currentRecord.topic)
        insertTimingsStm.setLong(2, currentRecord.partition)
        insertTimingsStm.setLong(3, currentRecord.offset)
        insertTimingsStm.setString(4, s"${currentRecord.partition}-${currentRecord.offset}")
        insertTimingsStm.setLong(5, currentRecord.timestamp)
        insertTimingsStm.setString(6, currentRecord.key)
        insertTimingsStm.setString(7, currentRecord.value)
        insertTimingsStm.addBatch()

        insertTimingsStm.executeBatch()

    }

    @BeforeClass
    fun init() {
        influxDb = InfluxDBFactory.connect(influxCfg.url, influxCfg.user, influxCfg.pass)
        influxDb.enableGzip()
        influxDb.setDatabase(influxCfg.dbName)
        influxDb.enableBatch(BatchOptions.DEFAULTS.actions(influxCfg.flushEveryPoints).flushDuration(influxCfg.flushEveryMsec))
//        val query = Query("SELECT idle FROM cpu", dbName)
//        influxDb.query(query)


        postgres = DriverManager.getConnection(postgresCfg.url)
        postgres.autoCommit = true
        insertTimingsStm = postgres.prepareStatement(insertTimingsSql)

    }

    @AfterClass
    fun cleanup() {
        log.info("reportFile {}", reportFile.canonicalPath)
        reportWriter.flush()
        reportWriter.close()
        influxDb.flush()
        influxDb.close()
    }


    val createTableTimingsSql = """
      |DROP TABLE IF EXISTS timings;
      |DROP SEQUENCE IF EXISTS timings_id_seq;
      |CREATE SEQUENCE IF NOT EXISTS timings_id_seq;
      |CREATE TABLE IF NOT EXISTS timings
      |(
      |    id integer NOT NULL DEFAULT nextval('timings_id_seq'::regclass),
      |    beginMs timestamp NOT NULL,
      |    endMs timestamp NOT NULL,
      |    elapsedMs bigint,
      |    session varchar(30),
      |    result varchar(10),
      |    tags jsonb,
      |    message text,
      |    CONSTRAINT timings_pkey PRIMARY KEY (id)
      |);
      |""".trimMargin("|")


    val insertTimingsSql = "INSERT INTO timings (beginMs, endMs, elapsedMs, session, result, tags, message) values (?, ?, ?, ?, ?, to_json(?::json), ?)"
    

}
