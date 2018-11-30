package batch.scheduler.domain

import java.time.ZonedDateTime


/**
 * City domain object.
 * <p>
 * @param name
 * @param latitude
 * @param longitude
 * @param cap - The max number of Birds that can be allocated to a city.
 */
data class City(val name: String, val latitude: Float, val longitude: Float, var cap: Int) {
    constructor(name: String, latitude: Float, longitude: Float): this(name, latitude, longitude, Integer.MAX_VALUE)
}

/**
 * Batch domain object.
 * <p>
 * @param batchId - The batch ID.
 * @param count - The number of Birds in the batch.
 */
data class Batch(val batchId: Int, val count: Int)

/**
 * Deployment domain object.
 * <p>
 * @param batchId - The batch ID.
 * @param city - The name of the city.
 * @param startDate - The date/time (with timezone) the batch is delivered and in effect.
 * @param endDate - The date/time (with timezone) the batch is decommissioned.
 */
data class Deployment(val batchId: Int, val city: String, val startDate: ZonedDateTime, val endDate: ZonedDateTime)