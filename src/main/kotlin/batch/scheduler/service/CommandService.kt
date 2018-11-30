package batch.scheduler.service

import batch.scheduler.domain.Batch
import batch.scheduler.domain.Cancellation
import batch.scheduler.domain.City
import batch.scheduler.domain.Deployment
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
        val id = repo.createCity(data)
        println("Created city $id")
    }

    fun createBatch(data: Batch) {
        val id = repo.createBatch(data)
        println("Created batch $id")
    }

    fun scheduleBatch(data: Deployment) {
        val id = repo.createDeployment(data)
        println("Created deployment $id")
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