package batch.scheduler.repository

import batch.scheduler.model.Batch
import batch.scheduler.model.City
import batch.scheduler.model.Deployment
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DefaultDSLContext
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource


@Singleton
class InMemoryRepository : Repository {

@Inject
private lateinit var ds: DataSource


//    private val cities: Map<String,City> = HashMap()
//    private val batches: Map<Int,Batch> = HashMap()
//    private val deployments: Map<DeploymentID,Deployment> = HashMap()

    @Inject
    private lateinit var ctx: DSLContext

    override fun createCity(city: City) {
        val result = ctx.query("select from 1").execute()
        println("Got result!!!!!: $result")
    }

    override fun createBatch(batch: Batch) {

    }

    override fun createDeployment(deployment: Deployment) {

    }

//    private fun getDSLContext(): DSLContext {
//        return DefaultDSLContext(ds, SQLDialect.H2)
//    }
}