package batch.scheduler.domain

import java.time.ZonedDateTime


/**
 * City domain object.
 * <p>
 * @param name
 * @param latitude
 * @param longitude
 * @param cap - Max number of Birds that can be allocated to a city.
 */
data class City(val name: String, val latitude: Float, val longitude: Float, var cap: Int) {
    constructor(name: String, latitude: Float, longitude: Float): this(name, latitude, longitude, Integer.MAX_VALUE)
}

/**
 * Batch domain object.
 * <p>
 * @param batchNumber - Batch number assigned by user.
 * @param size - Size of the batch.
 */
data class Batch(val batchNumber: Int, val size: Int)

/**
 * Deployment domain object.
 * <p>
 * @param batchNumber
 * @param city - Name of the city.
 * @param startDate - The date/time (with timezone) the batch is delivered and in effect.
 * @param endDate - The date/time (with timezone) the batch is decommissioned.
 */
data class Deployment(val batchNumber: Int, val city: String, val startDate: ZonedDateTime, val endDate: ZonedDateTime)