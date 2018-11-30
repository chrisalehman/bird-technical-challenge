package batch.scheduler.repository

import batch.scheduler.domain.Batch
import batch.scheduler.domain.City
import batch.scheduler.domain.Deployment


interface Repository {

    fun createCity(city: City): Long
    fun createBatch(batch: Batch): Long
    fun createDeployment(deployment: Deployment): Long
}