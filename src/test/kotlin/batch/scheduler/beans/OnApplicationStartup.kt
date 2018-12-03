//package batch.scheduler.beans
//
//import io.micronaut.context.annotation.Requires
//import io.micronaut.context.env.Environment
//import io.micronaut.context.event.ApplicationEventListener
//import io.micronaut.context.event.StartupEvent
//import io.micronaut.runtime.event.annotation.EventListener
//import org.jooq.DSLContext
//import org.slf4j.LoggerFactory
//import javax.inject.Inject
//import javax.inject.Singleton
//
//
//@Singleton
//@Requires(notEnv = [Environment.DEVELOPMENT])
//class OnApplicationStartup: ApplicationEventListener<StartupEvent> {
//
//    companion object {
//        private val LOG = LoggerFactory.getLogger(OnApplicationStartup::class.java)
//        var hasExecuted = false
//    }
//
//    @Inject
//    private lateinit var ctx: DSLContext
//
//    @EventListener
//    override fun onApplicationEvent(event: StartupEvent) {
//        loadSchema()
//    }
//
//    // note: for some reason the StartupEvent is getting triggered multiple times on startup... so run this code in a
//    // synchronized block with a hasExecuted flag to prevent it from being executed multiple times
//    @Synchronized
//    private fun loadSchema() {
//        if (!hasExecuted) {
//            hasExecuted = true
//            LOG.warn("Loading schema at startup...")
//            val sql: String = this::class.java.getResource("/ddl-test.sql").readText()
//            try { ctx.execute(sql) } catch (e: Exception) {} // suppress errors caused by duplicate runs
//        }
//    }
//}