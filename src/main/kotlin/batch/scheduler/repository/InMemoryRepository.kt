package batch.scheduler.repository

import batch.scheduler.model.Batch
import batch.scheduler.model.City
import batch.scheduler.model.Deployment
import batch.scheduler.repository.model.DeploymentID
import javax.inject.Singleton


@Singleton
class InMemoryRepository : Repository {

    private val cities: Map<String,City> = HashMap()
    private val batches: Map<Int,Batch> = HashMap()
    private val deployments: Map<DeploymentID,Deployment> = HashMap()


    override fun createCity(city: City) {

    }

    override fun createBatch(batch: Batch): Boolean {

    }

    override fun createDeployment(deployment: Deployment): Boolean {

    }
}