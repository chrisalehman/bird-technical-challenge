package batch.scheduler.repository

import batch.scheduler.domain.*
import batch.scheduler.domain.exceptions.DuplicateEntityException
import org.jooq.DSLContext
import org.jooq.generated.tables.Batch.BATCH
import javax.inject.Inject
import javax.inject.Singleton
import org.jooq.generated.tables.City.*
import org.jooq.generated.tables.Deployment.DEPLOYMENT
import java.time.ZonedDateTime
import java.util.SortedMap


/**
 * Repository backed by H2 database. Current implementation is in-memory.
 */
@Singleton class H2Repository : Repository {

    @Inject
    private lateinit var ctx: DSLContext

    /* commands */

    override fun createCity(obj: CreateCity): Long {
        try {
            return ctx.insertInto(CITY)
                    .set(CITY.NAME, obj.name)
                    .set(CITY.LATITUDE, obj.coordinate.latitude)
                    .set(CITY.LONGITUDE, obj.coordinate.longitude)
                    .set(CITY.CAP, obj.cap)
                    .returning(CITY.ID)
                    .fetchOne()
                    .get(CITY.ID)
        } catch (e: RuntimeException) {
            val isDuplicate = e.message?.contains("idx_unique_city_name", ignoreCase = true) ?: false
            if (isDuplicate)
                throw DuplicateEntityException("City already exists", e)
            else
                throw e
        }
    }

    override fun createBatch(obj: CreateBatch): Long {
        try {
            return ctx.insertInto(BATCH)
                    .set(BATCH.BATCH_NUMBER, obj.batchNumber)
                    .set(BATCH.SIZE, obj.size)
                    .returning(BATCH.ID)
                    .fetchOne()
                    .get(BATCH.ID)
        } catch (e: RuntimeException) {
            val isDuplicate = e.message?.contains("idx_unique_batch_number", ignoreCase = true) ?: false
            if (isDuplicate)
                throw DuplicateEntityException("Batch already exists", e)
            else
                throw e
        }
    }

    override fun createDeployment(obj: ScheduleDeployment, city: CityRecord, batch: BatchRecord): Long {
        return ctx.insertInto(DEPLOYMENT)
                .set(DEPLOYMENT.BATCH_ID, batch.id)
                .set(DEPLOYMENT.CITY_ID, city.id)
                .set(DEPLOYMENT.START_DATE, obj.startDate.toOffsetDateTime())
                .set(DEPLOYMENT.END_DATE, obj.endDate.toOffsetDateTime())
                .returning(DEPLOYMENT.ID)
                .fetchOne()
                .get(BATCH.ID)
    }

    override fun deleteDeployment(id: Long): Int  {
        return ctx.delete(DEPLOYMENT)
                .where(DEPLOYMENT.ID.eq(id))
                .execute()
    }

    /* queries */

    override fun getCity(name: String): CityRecord? {

        val record = ctx.selectFrom(CITY)
                .where(CITY.NAME.eq(name))
                .fetchOptional()

        return if (!record.isPresent) null else {
            record.get()
                  .map { CityRecord(
                    it.get(CITY.ID),
                    it.get(CITY.NAME),
                    Coordinate(it.get(CITY.LATITUDE), it.get(CITY.LONGITUDE)),
                    it.get(CITY.CAP))}
        }
    }

    override fun getBatch(batchNumber: Int): BatchRecord? {

        val record = ctx.selectFrom(BATCH)
                .where(BATCH.BATCH_NUMBER.eq(batchNumber))
                .fetchOptional()
        return if (!record.isPresent) null else {
            record.get()
                  .map { BatchRecord(
                    it.get(BATCH.ID),
                    it.get(BATCH.BATCH_NUMBER),
                    it.get(BATCH.SIZE))}
        }
    }

    override fun getDeployment(batchNumber: Int, cityName: String, date: ZonedDateTime): DeploymentRecord? {

        val record
                = ctx.selectDistinct(DEPLOYMENT.ID, DEPLOYMENT.CITY_ID, DEPLOYMENT.BATCH_ID, DEPLOYMENT.START_DATE,
                                     DEPLOYMENT.END_DATE)
            .from(DEPLOYMENT)
            .join(BATCH).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
            .join(CITY).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
            .where(BATCH.BATCH_NUMBER.eq(batchNumber))
            .and(CITY.NAME.eq(cityName))
            .and(DEPLOYMENT.START_DATE.lessOrEqual(date.toOffsetDateTime()))
            .and(DEPLOYMENT.END_DATE.greaterOrEqual(date.toOffsetDateTime()))
            .fetchOptional()

        return if (!record.isPresent) null else {
            record.get()
                  .map { DeploymentRecord(
                    it.get(DEPLOYMENT.ID),
                    it.get(DEPLOYMENT.CITY_ID),
                    it.get(DEPLOYMENT.BATCH_ID),
                    it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                    it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
        }
    }

    override fun getDeployments(city: String): List<DeploymentByCityResult> {
        return ctx.selectDistinct(CITY.NAME, BATCH.BATCH_NUMBER, BATCH.SIZE, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
            .from(CITY)
            .join(DEPLOYMENT).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
            .join(BATCH).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
            .where(CITY.NAME.eq(city))
            .orderBy(BATCH.BATCH_NUMBER.asc(), DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
            .fetch()
            .asSequence()
            .map { DeploymentByCityResult(
                    it.get(BATCH.BATCH_NUMBER),
                    it.get(BATCH.SIZE),
                    it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                    it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
            .toList()
    }

    override fun getDeployments(batchNumber: Int): List<DeploymentByBatchResult> {
        return ctx.selectDistinct(BATCH.BATCH_NUMBER, CITY.NAME, DEPLOYMENT.START_DATE, DEPLOYMENT.END_DATE)
            .from(BATCH)
            .join(DEPLOYMENT).on(BATCH.ID.eq(DEPLOYMENT.BATCH_ID))
            .join(CITY).on(CITY.ID.eq(DEPLOYMENT.CITY_ID))
            .where(BATCH.BATCH_NUMBER.eq(batchNumber))
            .orderBy(CITY.NAME.asc(), DEPLOYMENT.START_DATE.asc(), DEPLOYMENT.END_DATE.asc())
            .fetch()
            .asSequence()
            .map { DeploymentByBatchResult(
                    it.get(CITY.NAME),
                    it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                    it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
            .toList()
    }

    override fun getDeploymentsByCity(): SortedMap<String,List<DeploymentByCityResult>> {
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
                    DeploymentByCityResult(
                            it.get(BATCH.BATCH_NUMBER),
                            it.get(BATCH.SIZE),
                            it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                            it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
            }
            .toSortedMap()
    }

    override fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentByBatchResult>> {
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
                    DeploymentByBatchResult(
                        it.get(CITY.NAME),
                        it.get(DEPLOYMENT.START_DATE).toZonedDateTime(),
                        it.get(DEPLOYMENT.END_DATE).toZonedDateTime()) }
                }
            .toSortedMap()
    }
}