package batch.scheduler.repository

import batch.scheduler.model.Batch
import batch.scheduler.model.City
import batch.scheduler.model.Deployment


interface Repository {

    fun createCity(city: City): Boolean
    fun createBatch(batch: Batch): Boolean
    fun createDeployment(deployment: Deployment): Boolean
}