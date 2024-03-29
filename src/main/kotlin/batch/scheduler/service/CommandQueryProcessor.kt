package batch.scheduler.service

import batch.scheduler.domain.*
import batch.scheduler.domain.exceptions.NonExistentEntityException
import batch.scheduler.repository.Repository
import io.micronaut.spring.tx.annotation.Transactional
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton


/**
 * Core logic layer. Utilizes the Command/Query pattern for the clean separation of concerns. Transactions are applied
 * at the method level.
 **/
@Singleton @Transactional class CommandQueryProcessor(private val repo: Repository,
                                                      private val checker: IntervalConstraintChecker) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CommandQueryProcessor::class.java)
    }

    /* commands */

    fun createCity(command: CreateCity): Long {
        val id = repo.createCity(command)
        LOG.info("Created city $id: $command")
        return id
    }

    fun createBatch(command: CreateBatch): Long {
        val id = repo.createBatch(command)
        LOG.info("Created batch $id: $command")
        return id
    }

    fun scheduleDeployment(command: ScheduleDeployment): Long {

        val city: CityRecord = repo.getCity(command.city)
                ?: throw NonExistentEntityException("Cannot complete action for non-existent city '${command.city}'")
        val batch: BatchRecord = repo.getBatch(command.batchNumber)
                ?: throw NonExistentEntityException("Cannot complete action for non-existent batch '${command.batchNumber}'")

        // add interval constraints - this is where the main logic happens
        checker.addIntervalConstraints(
            Deployment(
                Batch(batch.batchNumber, batch.size),
                City(city.name, city.location, city.cap),
                command.startDate,
                command.endDate))

        // create the deployment
        val id = repo.createDeployment(command, city, batch)
        LOG.info("Scheduled deployment $id: $command")
        return id
    }

    fun cancelDeployment(command: CancelDeployment): Boolean {

        val d: DeploymentRecord? = repo.getDeployment(command.batchNumber, command.cityName, command.date)

        if (d == null) {
            LOG.info("No deployment found for batch ${command.batchNumber} city ${command.cityName} and date ${command.date}")
            return false
        }

        val numRecords = repo.deleteDeployment(d.id)
        return when (numRecords) {
            0 -> {
                LOG.warn("Failure to delete deployment ${d.id}")
                false
            }
            1 -> {
                // remove interval constraints
                checker.removeIntervalConstraints(command.batchNumber, command.cityName, command.date)
                LOG.info("Deployment ${d.id} cancelled")
                true
            }
            else -> throw IllegalStateException("Attempt to delete $numRecords deployments, rolling back.")
        }
    }

    /* queries */

    fun getDeployments(city: String): List<DeploymentByCityResult> {
        return repo.getDeployments(city)
    }

    fun getDeployments(batchNumber: Int): List<DeploymentByBatchResult> {
        return repo.getDeployments(batchNumber)
    }

    fun getDeploymentsByCity(): SortedMap<String, List<DeploymentByCityResult>> {
        return repo.getDeploymentsByCity()
    }

    fun getDeploymentsByBatch(): SortedMap<Int,List<DeploymentByBatchResult>> {
        return repo.getDeploymentsByBatch()
    }
}