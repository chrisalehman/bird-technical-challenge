package batch.scheduler.repository

import batch.scheduler.domain.*
import java.util.*


interface Repository {

    // commands
    fun createCity(obj: City): Long
    fun createBatch(obj: Batch): Long
    fun createDeployment(obj: Deployment): Long
    fun deleteDeploymentByDate(obj: CancelDeployment): Boolean

    // queries
    fun getDeployments(city: String): List<DeploymentsByCityUnit>
    fun getDeploymentsByCity(): SortedMap<String,List<DeploymentsByCityUnit>>
    fun getDeployments(batchNumber: Int): List<DeploymentsByBatchUnit>
    fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentsByBatchUnit>>
}