package batch.scheduler.service

import batch.scheduler.model.Batch
import batch.scheduler.model.Cancellation
import batch.scheduler.model.City
import batch.scheduler.model.Deployment
import batch.scheduler.repository.Repository
import javax.inject.Singleton


/**
 * Notes
 *  1. Cap on Birds applies to the relevant time period ONLY
 *  2. Can't re-schedule a batch if it's already deployed - you have to cancel it first
 *  3. Multiple deployments can exist for the same batch as long as the time periods don't overlap
 */
@Singleton class CommandService(private val repo: Repository) {

    fun createCity(data: City) {
        repo.createCity(data)
    }

    fun createBatch(data: Batch) {
        println("createBatch($data)")
    }

    fun scheduleBatch(data: Deployment) {
        println("scheduleBatch($data)")
    }

    fun cancelBatch(data: Cancellation) {
        println("cancelBatch($data)")
    }

    fun showCities() {
        println("showCities")
    }

    fun showBatches() {
        println("showBatches")
    }
}