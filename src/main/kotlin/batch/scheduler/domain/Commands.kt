package batch.scheduler.domain

import java.time.ZonedDateTime


/**
 * Cancels a deployment.
 * <p>
 * @param batchId - The batch ID.
 * @param city - The name of the city.
 * @param date - Any date/time between the scheduled deployment start and end dates.
 */
data class CancelDeployment(val batchId: Int, val city: String, val date: ZonedDateTime)