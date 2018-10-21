package genak

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

class PostgresReporter(val connection: Connection, val flushRecords: Int = 10000, val flushMillisecods: Long = 10000, reCreateTable: Boolean = false) {

    val log = LoggerFactory.getLogger(this.javaClass.name)

    val counter = AtomicInteger(0)
    var flushTimeMsec = AtomicLong(System.currentTimeMillis() + flushMillisecods)

    val insertTimingsStm: PreparedStatement
    val createTableTimingsSql = """
      |DROP TABLE IF EXISTS timings;
      |DROP SEQUENCE IF EXISTS timings_id_seq;
      |CREATE SEQUENCE IF NOT EXISTS timings_id_seq;
      |CREATE TABLE IF NOT EXISTS timings
      |(
      |    id integer NOT NULL DEFAULT nextval('timings_id_seq'::regclass),
      |    beginMs bigint NOT NULL,
      |    endMs bigint NOT NULL,
      |    elapsedMs bigint,
      |    session varchar(30),
      |    result varchar(10),
      |    tags jsonb,
      |    message text,
      |    CONSTRAINT timings_pkey PRIMARY KEY (id)
      |);
      |""".trimMargin("|")

    val insertTimingsSql = "INSERT INTO timings (beginMs, endMs, elapsedMs, session, result, tags, message) values (?, ?, ?, ?, ?, to_json(?::json), ?)"


    init {
        if (reCreateTable) {
            connection.prepareStatement(createTableTimingsSql).execute()
            connection.commit()
        }

        insertTimingsStm = connection.prepareStatement(insertTimingsSql)

    }

    inline fun haveEnoughRecords() = 0 == counter.get() % flushRecords
    inline fun timeToFlush() = System.currentTimeMillis() >= flushTimeMsec.get()


    fun flush() {
        val ms = measureTimeMillis {
            insertTimingsStm.executeBatch()
            connection.commit()
        }

        log.info("flushed {} records in {} ms", counter, ms)

        counter.set(0)
        flushTimeMsec.set(System.currentTimeMillis() + flushMillisecods)
    }


    fun addRow(record: PostgresRecord) {

        insertTimingsStm.setLong(1, record.beginMs)
        insertTimingsStm.setLong(2, record.endMs)
        insertTimingsStm.setLong(3, record.elapsedMs)
        insertTimingsStm.setString(4, record.session)
        insertTimingsStm.setString(5, record.result)
        insertTimingsStm.setString(6, record.tags.toJson())
        insertTimingsStm.setString(7, record.message)

        insertTimingsStm.addBatch()

        counter.getAndIncrement()

        if (timeToFlush() || haveEnoughRecords()) {
            flush()
        }

    }

}