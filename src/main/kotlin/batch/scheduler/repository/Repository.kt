package batch.scheduler.repository

import batch.scheduler.domain.CancelDeployment
import batch.scheduler.domain.Batch
import batch.scheduler.domain.City
import batch.scheduler.domain.Deployment
import java.util.*


interface Repository {

    // commands
    fun createCity(obj: City): Long
    fun createBatch(obj: Batch): Long
    fun createDeployment(obj: Deployment): Long
    fun deleteDeploymentByDate(obj: CancelDeployment): Boolean

    // queries
    fun getDeploymentsByCity(): TreeMap<City,List<Deployment>>
    fun getDeploymentsByBatch(): TreeMap<Batch,List<Deployment>>
}