package batch.scheduler.domain

import java.time.ZonedDateTime


/**
 * Represents a grouped unit of the 'deployments by city' query.
 * <p>
 * @param batchNumber
 * @param batchSize
 * @param startDate
 * @param endDate
 */
data class DeploymentsByCityUnit(/*val batchNumber: Int, */val batchSize: Int, val startDate: ZonedDateTime,
                                 val endDate: ZonedDateTime)

/**
 * Represents a grouped unit of the 'deployments by batch' query.
 * <p>
 * @param cityName
 * @param startDate
 * @param endDate
 */
data class DeploymentsByBatchUnit(/*val cityName: String, */val startDate: ZonedDateTime, val endDate: ZonedDateTime)