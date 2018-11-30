package batch.scheduler.repository

import batch.scheduler.domain.Batch
import batch.scheduler.domain.City
import batch.scheduler.domain.Deployment
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.generated.tables.Batch.BATCH
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource
import org.jooq.generated.tables.City.*
import org.jooq.generated.tables.Deployment.DEPLOYMENT
import org.jooq.generated.tables.records.BatchRecord
import org.jooq.generated.tables.records.CityRecord


@Singleton
class H2Repository : Repository {

@Inject
private lateinit var ds: DataSource

    @Inject
    private lateinit var ctx: DSLContext

    override fun createCity(city: City): Long {
        val record: Record = ctx.insertInto(CITY)
            .set(CITY.NAME, city.name)
            .set(CITY.LATITUDE, city.latitude)
            .set(CITY.LONGITUDE, city.longitude)
            .set(CITY.CAP, city.cap)
            .returning(CITY.ID)
            .fetchOne()

        // todo: log creation of record

        return record.get(CITY.ID)
    }

    override fun createBatch(batch: Batch): Long {
        val record: Record = ctx.insertInto(BATCH)
                .set(BATCH.BATCH_ID, batch.batchId)
                .set(BATCH.BATCH_COUNT, batch.count)
                .returning(BATCH.ID)
                .fetchOne()

        // todo: log creation of record

        return record.get(BATCH.ID)
    }

    override fun createDeployment(deployment: Deployment): Long {
        val record: Record = ctx.insertInto(DEPLOYMENT)
                                .set(DEPLOYMENT.BATCH_ID, getBatch(deployment.batchId).id)
                                .set(DEPLOYMENT.CITY_ID, getCity(deployment.city).id)
                                .set(DEPLOYMENT.START_DATE, deployment.startDate.toOffsetDateTime())
                                .set(DEPLOYMENT.END_DATE, deployment.endDate.toOffsetDateTime())
                                .returning(DEPLOYMENT.ID)
                                .fetchOne()

        // todo: log creation of record

        return record.get(BATCH.ID)
    }

    /* internal */

    // todo: handle non-existence
    private fun getCity(name: String): CityRecord {
        return ctx.selectFrom(CITY)
                    .where(CITY.NAME.eq(name))
                    .fetchOne()

    }

    // todo: handle non-existence
    private fun getBatch(batchId: Int): BatchRecord {
        return ctx.selectFrom(BATCH)
                    .where(BATCH.BATCH_ID.eq(batchId))
                    .fetchOne()
    }
}