package genak

import java.sql.Connection
import java.sql.PreparedStatement
import java.util.concurrent.atomic.AtomicInteger

class PostgresReporter(connection: Connection, val flushRecords: Int = Int.MAX_VALUE, val flushMillisecods: Long = 10000, reCreateTable: Boolean = false) {

    val counter = AtomicInteger(0)
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
        }

        insertTimingsStm = connection.prepareStatement(insertTimingsSql)

    }

    inline fun haveEnoughRecords() = 0 == counter.get() % flushRecords
    inline fun waitedEnoughMilliseconds() = 0L == System.currentTimeMillis() % flushMillisecods


    fun flush() {
        insertTimingsStm.executeBatch()
        counter.set(0)
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


        if (waitedEnoughMilliseconds() || haveEnoughRecords()) {
            flush()
        }


    }

}