package batch.scheduler.repository

import batch.scheduler.domain.*
import java.time.OffsetDateTime
import java.util.SortedMap


interface Repository {

    /* commands */

    fun createCity(obj: CreateCity): Long
    fun createBatch(obj: CreateBatch): Long
    fun createDeployment(obj: ScheduleDeployment, city: CityRecord, batch: BatchRecord): Long
    fun deleteDeployment(id: Long): Int

    /* queries */

    fun getCity(name: String): CityRecord?
    fun getBatch(batchNumber: Int): BatchRecord?
    fun getDeployment(batchNumber: Int, cityName: String, date: OffsetDateTime): DeploymentRecord?
    fun getDeployments(city: String): List<DeploymentByCityResult>
    fun getDeployments(batchNumber: Int): List<DeploymentByBatchResult>
    fun getDeploymentsByCity(): SortedMap<String,List<DeploymentByCityResult>>
    fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentByBatchResult>>
}