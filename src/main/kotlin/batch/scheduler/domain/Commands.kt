package batch.scheduler.domain

import java.time.ZonedDateTime


/**
 * Command object for creating a city.
 * <p>
 * @param name
 * @param coordinate
 * @param cap - Max number of Birds that can be allocated to a city.
 */
data class CreateCity(val name: String, val coordinate: Coordinate, var cap: Int) {
    constructor(name: String, coordinate: Coordinate): this(name, coordinate, Integer.MAX_VALUE)
}

/**
 * Command object for creating a batch.
 * <p>
 * @param batchNumber - CreateBatch number assigned by user.
 * @param size - Size of the batch.
 */
data class CreateBatch(val batchNumber: Int, val size: Int)

/**
 * Command object for scheduling a deployment.
 * <p>
 * @param batchNumber
 * @param city - Name of the city.
 * @param startDate - The date/time (with timezone) the batch is delivered and in effect.
 * @param endDate - The date/time (with timezone) the batch is decommissioned.
 */
data class ScheduleDeployment(val batchNumber: Int, val city: String, val startDate: ZonedDateTime,
                              val endDate: ZonedDateTime)

/**
 * Command object for canceling a scheduled deployment. Matching is based on given batch number, city name and date.
 * The date must be between an existing scheduled deployment's start and end dates.
 * <p>
 * @param batchNumber
 * @param cityName
 * @param date - Any date/time between a scheduled deployment's start and end dates.
 */
data class CancelDeployment(val batchNumber: Int, val cityName: String, val date: ZonedDateTime)