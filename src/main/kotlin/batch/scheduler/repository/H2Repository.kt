package batch.scheduler.repository

import batch.scheduler.domain.*
import org.jooq.DSLContext
import org.jooq.generated.tables.Batch.BATCH
import javax.inject.Inject
import javax.inject.Singleton
import org.jooq.generated.tables.City.*
import org.jooq.generated.tables.Deployment.DEPLOYMENT
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.time.ZonedDateTime
import java.util.SortedMap


@Singleton
class H2Repository : Repository {

    companion object {
        private val LOG = LoggerFactory.getLogger(H2Repository::class.java)
    }

    @Inject
    private lateinit var ctx: DSLContext

    /* commands */

    override fun createCity(obj: City): Long {
        try {
            return ctx.insertInto(CITY)
                    .set(CITY.NAME, obj.name)
                    .set(CITY.LATITUDE, obj.latitude)
                    .set(CITY.LONGITUDE, obj.longitude)
                    .set(CITY.CAP, obj.cap)
                    .returning(CITY.ID)
                    .fetchOne()
                    .get(CITY.ID)
        } catch (e: SQLException) {
            val isDuplicate = e.message?.contains("idx_unique_city_name", ignoreCase = true) ?: false
            if (isDuplicate)
                throw DuplicateEntityException(e)
            else
                throw e
        }
    }

    override fun createBatch(obj: Batch): Long {
        try {
            return ctx.insertInto(BATCH)
                    .set(BATCH.BATCH_NUMBER, obj.batchNumber)
                    .set(BATCH.SIZE, obj.size)
                    .returning(BATCH.ID)
                    .fetchOne()
                    .get(BATCH.ID)
        } catch (e: SQLException) {
            val isDuplicate = e.message?.contains("idx_unique_batch_number", ignoreCase = true) ?: false
            if (isDuplicate)
                throw DuplicateEntityException(e)
            else
                throw e
        }
    }

    override fun createDeployment(obj: Deployment): Long {
        try {

            val city: CityRecord = getCity(obj.city)
                    ?: throw IllegalArgumentException("Cannot create deployment for non-existent city '${obj.city}'")
            val batch: BatchRecord = getBatch(obj.batchNumber)
                    ?: throw IllegalArgumentException("Cannot create deployment for non-existent batch '${obj.batchNumber}'")

            return ctx.insertInto(DEPLOYMENT)
                    .set(DEPLOYMENT.BATCH_ID, batch.id)
                    .set(DEPLOYMENT.CITY_ID, city.id)
                    .set(DEPLOYMENT.START_DATE, obj.startDate.toOffsetDateTime())
                    .set(DEPLOYMENT.END_DATE, obj.endDate.toOffsetDateTime())
                    .returning(DEPLOYMENT.ID)
                    .fetchOne()
                    .get(BATCH.ID)

        } catch (e: SQLException) {
            val isDuplicate = e.message?.contains("idx_deployment_business_key", ignoreCase = true) ?: false
            if (isDuplicate)
                throw DuplicateEntityException(e)
            else
                throw e
        }
    }

    override fun deleteDeployment(id: Long): Int  {
        return ctx.delete(DEPLOYMENT)
                .where(DEPLOYMENT.ID.eq(id))
                .execute()
    }

    /* queries */

    override fun getCity(name: String): CityRecord? {
        return ctx.selectFrom(CITY)
                .where(CITY.NAME.eq(name))
                .fetchOne()
                .map { CityRecord(
                        it.get(CITY.ID),
                        it.get(CITY.NAME),
                        it.get(CITY.LATITUDE),
                        it.get(CITY.LONGITUDE),
                        it.get(CITY.CAP))}
    }

    override fun getBatch(batchNumber: Int): BatchRecord? {
        return ctx.selectFrom(BATCH)
                .where(BATCH.BATCH_NUMBER.eq(batchNumber))
                .fetchOne()
                .map { BatchRecord(
                        it.get(BATCH.ID),
                        it.get(BATCH.BATCH_NUMBER),
                        it.get(BATCH.SIZE))}
    }

    override fun getDeployment(batchNumber: Int, cityName: String, date: ZonedDateTime): DeploymentRecord? {

        val rs: List<DeploymentRecord> = ctx.selectDistinct(DEPLOYMENT.ID, DEPLOYMENT.CITY_ID, DEPLOYMENT.BATCH_ID, DEPLOYMENT.START_DATE,
                                           DEPLOYMENT.END_DATE)
                .from(DEPLOYMENT)
                .join(BATCH).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
                .join(CITY).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
                .where(BATCH.BATCH_NUMBER.eq(batchNumber))
                .and(CITY.NAME.eq(cityName))
                .and(DEPLOYMENT.START_DATE.lessOrEqual(date.toOffsetDateTime()))
                .and(DEPLOYMENT.END_DATE.greaterOrEqual(date.toOffsetDateTime()))
                .fetch()
                .asSequence()
                .filter { it.get(DEPLOYMENT.ID) != null }
                .map { DeploymentRecord(
                        it.get(DEPLOYMENT.ID),
                        it.get(DEPLOYMENT.CITY_ID),
                        it.get(DEPLOYMENT.BATCH_ID),
                        it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                        it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
                .toList()

        // this should never happen
        if (rs.size > 1) {
            LOG.warn("Fetched ${rs.size} deployment records for batch number $batchNumber, city $cityName and date " +
                     "$date; fetching first one...")
        }

        return if (rs.isNotEmpty()) rs[0] else null
    }

    override fun getDeployments(city: String): List<DeploymentByCityUnit> {
        return ctx.selectDistinct(CITY.NAME, BATCH.BATCH_NUMBER, BATCH.SIZE, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
                .from(CITY)
                .join(DEPLOYMENT).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
                .join(BATCH).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
                .where(CITY.NAME.eq(city))
                .orderBy(BATCH.BATCH_NUMBER.asc(), DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
                .fetch()
                .asSequence()
                .map { DeploymentByCityUnit(
                        it.get(BATCH.BATCH_NUMBER),
                        it.get(BATCH.SIZE),
                        it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                        it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
                .toList()
    }

    override fun getDeployments(batchNumber: Int): List<DeploymentByBatchUnit> {
        return ctx.selectDistinct(BATCH.BATCH_NUMBER, CITY.NAME, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
                .from(BATCH)
                .join(DEPLOYMENT).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
                .join(CITY).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
                .where(BATCH.BATCH_NUMBER.eq(batchNumber))
                .orderBy(CITY.NAME.asc(), DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
                .fetch()
                .asSequence()
                .map { DeploymentByBatchUnit(
                        it.get(CITY.NAME),
                        it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                        it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
                .toList()
    }

    override fun getDeploymentsByCity(): SortedMap<String,List<DeploymentByCityUnit>> {
        return ctx.selectDistinct(CITY.NAME, BATCH.BATCH_NUMBER, BATCH.SIZE, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
            .from(CITY)
            .leftOuterJoin(DEPLOYMENT).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
            .leftOuterJoin(BATCH).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
            .orderBy(CITY.NAME.asc(), BATCH.BATCH_NUMBER.asc(), DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
            .fetch()
            .asSequence()
            .groupBy { it.get(CITY.NAME) }
            .mapValues { (_,v) ->
                if (v.first().get(DEPLOYMENT.START_DATE) == null) listOf()
                else v.map {
                    DeploymentByCityUnit(
                            it.get(BATCH.BATCH_NUMBER),
                            it.get(BATCH.SIZE),
                            it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                            it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
            }
            .toSortedMap()
    }

    override fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentByBatchUnit>> {
        return ctx.selectDistinct(BATCH.BATCH_NUMBER, CITY.NAME, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
            .from(BATCH)
            .leftOuterJoin(DEPLOYMENT).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
            .leftOuterJoin(CITY).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
            .orderBy(DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
            .fetch()
            .asSequence()
            .groupBy { it.get(BATCH.BATCH_NUMBER) }
            .mapValues { (_,v) ->
                if (v.first().get(DEPLOYMENT.START_DATE) == null) listOf()
                else v.map {
                    DeploymentByBatchUnit(
                        it.get(CITY.NAME),
                        it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                        it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
                }
            .toSortedMap()
    }
}