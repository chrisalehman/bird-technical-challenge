package batch.scheduler.repository

import batch.scheduler.domain.*
import java.time.ZonedDateTime
import java.util.*


interface Repository {

    /* commands */

    fun createCity(obj: City): Long
    fun createBatch(obj: Batch): Long
    fun createDeployment(obj: Deployment): Long
    fun deleteDeployment(id: Long): Int

    /* queries */

    fun getCity(name: String): CityRecord?
    fun getBatch(batchNumber: Int): BatchRecord?
    fun getDeployment(batchNumber: Int, cityName: String, date: ZonedDateTime): DeploymentRecord?
    fun getDeployments(city: String): List<DeploymentByCityUnit>
    fun getDeployments(batchNumber: Int): List<DeploymentByBatchUnit>
    fun getDeploymentsByCity(): SortedMap<String,List<DeploymentByCityUnit>>
    fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentByBatchUnit>>
}