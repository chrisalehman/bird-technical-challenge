package batch.scheduler.repository

import batch.scheduler.domain.*
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
import java.util.SortedMap


@Singleton
class H2Repository : Repository {

    @Inject
    private lateinit var ds: DataSource

    @Inject
    private lateinit var ctx: DSLContext

    override fun createCity(obj: City): Long {
        val record: Record = ctx.insertInto(CITY)
            .set(CITY.NAME, obj.name)
            .set(CITY.LATITUDE, obj.latitude)
            .set(CITY.LONGITUDE, obj.longitude)
            .set(CITY.CAP, obj.cap)
            .returning(CITY.ID)
            .fetchOne()

        // todo: log creation of record

        return record.get(CITY.ID)
    }

    override fun createBatch(obj: Batch): Long {
        val record: Record = ctx.insertInto(BATCH)
                .set(BATCH.BATCH_NUMBER, obj.batchNumber)
                .set(BATCH.SIZE, obj.size)
                .returning(BATCH.ID)
                .fetchOne()

        // todo: log creation of record

        return record.get(BATCH.ID)
    }

    override fun createDeployment(obj: Deployment): Long {
        val record: Record = ctx.insertInto(DEPLOYMENT)
                                .set(DEPLOYMENT.BATCH_ID, getBatch(obj.batchNumber).id)
                                .set(DEPLOYMENT.CITY_ID, getCity(obj.city).id)
                                .set(DEPLOYMENT.START_DATE, obj.startDate.toOffsetDateTime())
                                .set(DEPLOYMENT.END_DATE, obj.endDate.toOffsetDateTime())
                                .returning(DEPLOYMENT.ID)
                                .fetchOne()

        // todo: log creation of record

        return record.get(BATCH.ID)
    }

    // todo
    override fun deleteDeploymentByDate(obj: CancelDeployment): Boolean {
        return false
    }

    override fun getDeploymentsByCity(): SortedMap<String,List<DeploymentsByCityUnit>> {
        return ctx.select(CITY.NAME, BATCH.BATCH_NUMBER, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
            .from(CITY)
            .leftOuterJoin(DEPLOYMENT).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
            .leftOuterJoin(BATCH).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
            .orderBy(CITY.NAME.asc(), DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
            .fetch()
            .asSequence()
            .groupBy { it.get(CITY.NAME) }
            .mapValues { (_,v) ->
                if (v.first().get(DEPLOYMENT.START_DATE) == null) listOf()
                else v.map {
                    DeploymentsByCityUnit(
                        it.get(BATCH.BATCH_NUMBER),
                        it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                        it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
                }
            .toSortedMap()
    }

    override fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentsByBatchUnit>> {
        return ctx.select(BATCH.BATCH_NUMBER, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
            .from(BATCH)
            .leftOuterJoin(DEPLOYMENT).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
            .orderBy(DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
            .fetch()
            .asSequence()
            .groupBy { it.get(BATCH.BATCH_NUMBER) }
            .mapValues { (_,v) ->
                if (v.first().get(DEPLOYMENT.START_DATE) == null) listOf()
                else v.map {
                    DeploymentsByBatchUnit(
                        it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                        it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
                }
            .toSortedMap()
    }

    /* internal */

    // todo: handle non-existence
    private fun getCity(name: String): CityRecord {
        // todo: check that only record returned
        return ctx.selectFrom(CITY)
                    .where(CITY.NAME.eq(name))
                    .fetchOne()
    }

    // todo: handle non-existence
    private fun getBatch(batchNumber: Int): BatchRecord {
        // todo: check that only record returned
        return ctx.selectFrom(BATCH)
                    .where(BATCH.BATCH_NUMBER.eq(batchNumber))
                    .fetchOne()
    }
}