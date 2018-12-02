package batch.scheduler.service

import batch.scheduler.domain.*
import batch.scheduler.repository.Repository
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton


/**
 *  Core logic:
 *      a) Double-booking - Prevent a deployment if it overlaps any pre-existing deployment for the same batch ID.
 *      b) Enforcing cityName cap - Prevent a deployment if the sum of batch size and overlapping intervals' batch sizes
 *         are greater than the cityName's limit.
 */
@Singleton
@Transactional
class CommandService(private val repo: Repository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CommandService::class.java)
    }

    /* commands */

    fun createCity(data: City): Long {
        val id = repo.createCity(data)
        LOG.info("Created city $id: $data")
        return id
    }

    fun createBatch(data: Batch): Long {
        val id = repo.createBatch(data)
        LOG.info("Created batch $id: $data")
        return id
    }

    fun scheduleBatch(data: Deployment): Long {
        val id = repo.createDeployment(data)
        LOG.info("Created deployment $id: $data")
        return id
    }

    fun cancelBatch(data: CancelDeployment): Boolean {

        val d: DeploymentRecord? = repo.getDeployment(data.batchNumber, data.cityName, data.date)

        if (d == null) {
            LOG.info("No deployment found for batch ${data.batchNumber} city ${data.cityName} and date ${data.date}")
            return false
        }

        val numRecords = repo.deleteDeployment(d.id)
        return when (numRecords) {
            0 -> {
                LOG.warn("Failure to delete deployment ${d.id}")
                false
            }
            1 -> {
                LOG.info("Deployment ${d.id} cancelled")
                true
            }
            else -> throw IllegalStateException("Attempt to delete $numRecords deployments, rolling back.")
        }
    }

    /* queries */

    fun getDeployments(city: String): List<DeploymentByCityUnit> {
        return repo.getDeployments(city)
    }

    fun getDeployments(batchNumber: Int): List<DeploymentByBatchUnit> {
        return repo.getDeployments(batchNumber)
    }

    fun getDeploymentsByCity(): SortedMap<String, List<DeploymentByCityUnit>> {
        return repo.getDeploymentsByCity()
    }

    fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentByBatchUnit>> {
        return repo.getDeploymentsByBatch()
    }
}