package batch.scheduler

import batch.scheduler.util.CLI
import io.micronaut.function.FunctionBean
import io.micronaut.runtime.Micronaut
import java.util.function.Supplier


@FunctionBean("batch-scheduler")
class Application(private val cli: CLI): Supplier<Unit> {

    override fun get() {
        cli.start()
    }

    companion object Application {

        @JvmStatic
        fun main(args: Array<String>) {
            Micronaut.build()
                .packages("batch.scheduler")
                .mainClass(Application::class.java)
                .start()
        }
    }
}