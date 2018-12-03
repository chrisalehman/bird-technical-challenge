package batch.scheduler

import batch.scheduler.controller.CLI
import io.micronaut.function.FunctionBean
import io.micronaut.runtime.Micronaut
import java.util.function.Supplier


/**
 * Main standalone CLI application entry point.
 */
@FunctionBean("batch-scheduler") class SupplierApplication(private val cli: CLI): Supplier<Unit> {

    override fun get() {
        cli.start()
    }

    companion object Application {

        @JvmStatic
        fun main(args: Array<String>) {
            Micronaut.build()
                .packages("batch.scheduler")
                .mainClass(SupplierApplication::class.java)
                .start()
        }
    }
}