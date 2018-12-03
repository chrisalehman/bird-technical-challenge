package batch.scheduler

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals


class FunctionApplicationTests: Spek({

    describe("batch-scheduler tests") {

        // spin up a server and client to execute tests
        val server = ApplicationContext.run(EmbeddedServer::class.java)
        val client = server.applicationContext.getBean(FunctionApplicationClient::class.java)

        it("loads the database") {
            val ctx: DSLContext = server.applicationContext.getBean(DSLContext::class.java)
            val sql: String = this::class.java.getResource("/ddl.sql").readText()
            ctx.execute(sql)
        }

        it("executes baseline tests") {
            assertEquals("OK", client.apply("CITY \"Los Angeles\" 34.048925 -118.428663"))
            assertEquals("OK", client.apply("CITY \"Austin\" 30.305804 -97.728682 500"))
            assertEquals("OK", client.apply("BATCH 1 250"))
            assertEquals("OK", client.apply("BATCH 2 500"))
            assertEquals("OK", client.apply("BATCH 3 200"))
            assertEquals("OK", client.apply("SCHEDULE 1 \"Austin\" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00"))
            assertEquals("Cannot schedule deployment; city cap exceeded by 250 Birds for given period\n", client.apply("SCHEDULE 2 \"Austin\" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00"))
            assertEquals("OK", client.apply("SCHEDULE 3 \"Austin\" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00"))
            assertEquals("Austin\n" +
                    "BATCH(1, 250) -> 2018-08-30T17:44:40Z 2018-09-23T17:44:40Z\n" +
                    "BATCH(3, 200) -> 2018-08-30T17:44:40Z 2018-09-23T17:44:40Z\n" +
                    "Los Angeles\n",
                    client.apply("SHOW CITIES"))
            assertEquals("OK", client.apply("SCHEDULE 2 \"Austin\" 2018-09-25T00:00:00+00:00 2018-10-18T12:00:00+00:00"))
            assertEquals("Austin\n" +
                    "BATCH(1, 250) -> 2018-08-30T17:44:40Z 2018-09-23T17:44:40Z\n" +
                    "BATCH(2, 500) -> 2018-09-24T17:00Z 2018-10-18T05:00Z\n" +
                    "BATCH(3, 200) -> 2018-08-30T17:44:40Z 2018-09-23T17:44:40Z\n" +
                    "Los Angeles\n",
                    client.apply("SHOW CITIES"))
            assertEquals("OK", client.apply("CANCEL 3 \"Austin\" 2018-08-31T00:44:41+00:00"))
            assertEquals("BATCH 1\n" +
                    "Austin 2018-08-30T17:44:40Z 2018-09-23T17:44:40Z\n" +
                    "BATCH 2\n" +
                    "Austin 2018-09-24T17:00Z 2018-10-18T05:00Z\n" +
                    "BATCH 3\n",
                    client.apply("SHOW BATCHES"))
            assertEquals("Cannot schedule deployment; batch 2 has a conflict\n", client.apply("SCHEDULE 2 \"Los Angeles\" 2018-09-26T00:00:00+00:00 2018-09-30T12:00:00+00:00"))
            assertEquals("OK", client.apply("SCHEDULE 2 \"Los Angeles\" 2018-10-20T12:00:00+00:00 2018-12-17T12:00:00+00:00"))
            assertEquals("Austin\n" +
                    "BATCH(1, 250) -> 2018-08-30T17:44:40Z 2018-09-23T17:44:40Z\n" +
                    "BATCH(2, 500) -> 2018-09-24T17:00Z 2018-10-18T05:00Z\n" +
                    "Los Angeles\n" +
                    "BATCH(2, 500) -> 2018-10-20T05:00Z 2018-12-17T04:00Z\n",
                    client.apply("SHOW CITIES"))
        }

        it ("runs the SHOW CITY and SHOW BATCH positive bonus commands") {

            assertEquals("BATCH(1, 250) -> 2018-08-30T17:44:40Z 2018-09-23T17:44:40Z\n" +
                    "BATCH(2, 500) -> 2018-09-24T17:00Z 2018-10-18T05:00Z\n", client.apply("SHOW CITY \"Austin\""))

            assertEquals("Austin 2018-09-24T17:00Z 2018-10-18T05:00Z\n" +
                    "Los Angeles 2018-10-20T05:00Z 2018-12-17T04:00Z\n", client.apply("SHOW BATCH 2"))
        }

        it("runs basic negative tests") {

            // bad input
            assertEquals("Invalid command: FOO\n", client.apply("FOO"))
            assertEquals("Invalid command: SHOW FOO\n", client.apply("SHOW FOO"))
            assertEquals("Invalid arguments\n", client.apply("CITY \"Seattle\" ABC DEF 20"))
            assertEquals("Missing arguments\n", client.apply("CITY \"Seattle\""))

            // entity issues
            assertEquals("City already exists\n", client.apply("CITY \"Los Angeles\" 34.048925 -118.428663"))
            assertEquals("Batch already exists\n", client.apply("BATCH 1 250"))
        }

        it ("runs batch conflict and city cap violation tests") {

            // attempt to re-schedule batch for same city
            assertEquals("Cannot schedule deployment; batch 1 has a conflict\n", client.apply("SCHEDULE 1 \"Austin\" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00"))

            // attempt to cancel batch 1, but with a date outside the time period - nothing happens
            assertEquals("OK", client.apply("CANCEL 1 \"Austin\" 2019-08-31T00:44:41+00:00"))

            // cancel batch 1, re-schedule for Los Angeles for exact same time period
            assertEquals("OK", client.apply("CANCEL 1 \"Austin\" 2018-08-31T00:44:41+00:00"))
            assertEquals("OK", client.apply("SCHEDULE 1 \"Los Angeles\" 2018-08-31T00:44:40+00:00 2018-09-24T00:44:40+00:00"))

            // attempt to schedule batch 1 back-to-back with Austin - should trigger error because of distance calculation
            assertEquals("Cannot schedule deployment; batch 1 has a conflict\n", client.apply("SCHEDULE 1 \"Austin\" 2018-09-24T01:44:41+00:00 2018-10-24T01:44:40+00:00"))

            // attempt to schedule it a day later - still fails
            assertEquals("Cannot schedule deployment; batch 1 has a conflict\n", client.apply("SCHEDULE 1 \"Austin\" 2018-09-25T01:44:41+00:00 2018-10-24T01:44:40+00:00"))

            // push it two days, now that's enough time - but we trigger the city cap rule
            assertEquals("Cannot schedule deployment; city cap exceeded by 250 Birds for given period\n", client.apply("SCHEDULE 1 \"Austin\" 2018-09-26T01:44:41+00:00 2018-10-24T01:44:40+00:00"))

            // cancel batch 2 that is causing the cap rule to trigger - and re-attempt to schedule for batch 1 two days later than Austin
            assertEquals("OK", client.apply("CANCEL 2 \"Austin\" 2018-09-28T00:44:41+00:00"))
            assertEquals("OK", client.apply("SCHEDULE 1 \"Austin\" 2018-09-26T01:44:41+00:00 2018-10-24T01:44:40+00:00"))
        }

        afterGroup {
            server.stop()
        }
    }
})