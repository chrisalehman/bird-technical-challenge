package batch.scheduler.beans

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration
import javax.inject.Inject
import javax.sql.DataSource


@Factory
class DataSourceFactory {

    @Inject
    private lateinit var ds: DataSource

    @Bean
    fun dslContext(): DSLContext {
        return DSL.using(DefaultConfiguration().set(DataSourceConnectionProvider(ds)))
    }
}