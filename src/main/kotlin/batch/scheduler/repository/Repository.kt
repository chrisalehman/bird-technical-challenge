package batch.scheduler.repository

import batch.scheduler.model.Batch
import batch.scheduler.model.City
import batch.scheduler.model.Deployment


interface Repository {

    fun createCity(city: City)
    fun createBatch(batch: Batch)
    fun createDeployment(deployment: Deployment)
}