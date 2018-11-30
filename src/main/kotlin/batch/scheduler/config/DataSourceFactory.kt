package batch.scheduler.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration
import javax.inject.Inject
import javax.inject.Singleton
import javax.sql.DataSource


@Singleton
@Factory
class DataSourceFactory() {

    @Inject
    private lateinit var ds: DataSource

    @Bean
    fun dslConext(): DSLContext {

        val settings = Settings()
                // Normally, the records are "attached" to the Configuration that created (i.e. fetch/insert) them.
                // This means that they hold an internal reference to the same database connection that was used.
                // The idea behind this is to make CRUD easier for potential subsequent store/refresh/delete
                // operations. We do not use or need that.
                .withAttachRecords(false)
                // To log or not to log the sql queries, that is the question
                .withExecuteLogging(true)

        // Configuration for JOOQ
        val conf: Configuration = DefaultConfiguration()
                .set(SQLDialect.H2)
                .set(DataSourceConnectionProvider(ds))
                .set(settings)

        return DSL.using(conf)
    }
}