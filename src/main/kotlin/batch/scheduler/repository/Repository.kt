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
    fun getDeploymentsByCity(): SortedMap<String,List<DeploymentsByCityUnit>>
    fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentsByBatchUnit>>
}