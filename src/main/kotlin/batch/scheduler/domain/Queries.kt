package batch.scheduler.domain

import java.time.ZonedDateTime


/**
 * Represents a city record fetched from the data store.
 * <p>
 * @param id - Surrogate primary key
 * @param name
 * @param latitude
 * @param longitude
 * @param cap
 */
data class CityRecord(val id: Long, val name: String, val latitude: Float, val longitude: Float, val cap: Int)

/**
 * Represents a batch record fetched from the data store.
 * <p>
 * @param id - Surrogate primary key
 * @param batchNumber - Publicly exposed identifier, i.e., business key
 * @param latitude
 * @param longitude
 * @param cap
 */
data class BatchRecord(val id: Long, val batchNumber: Int, val size: Int)

/**
 * Represents a deployment record fetched from the data store.
 * <p>
 * @param id - Surrogate primary key.
 * @param cityId - CreateCity's surrogate primary key.
 * @param batchId - CreateBatch's surrogate primary key.
 * @param startDate
 * @param endDate
 */
data class DeploymentRecord(val id: Long, val cityId: Long, val batchId: Long, val startDate: ZonedDateTime,
                            val endDate: ZonedDateTime)

/**
 * Represents a unit of the 'deployments grouped by city' query.
 * <p>
 * @param batchNumber
 * @param batchSize
 * @param startDate
 * @param endDate
 */
data class DeploymentByCityResult(val batchNumber: Int, val batchSize: Int, val startDate: ZonedDateTime,
                                  val endDate: ZonedDateTime) {

    override fun toString(): String {
        return "BATCH($batchNumber, $batchSize) -> $startDate $endDate"
    }
}

/**
 * Represents a unit of the 'deployments grouped by batch' query.
 * <p>
 * @param cityName
 * @param startDate
 * @param endDate
 */
data class DeploymentByBatchResult(val cityName: String, val startDate: ZonedDateTime, val endDate: ZonedDateTime) {

    override fun toString(): String {
        return "$cityName $startDate $endDate"
    }
}