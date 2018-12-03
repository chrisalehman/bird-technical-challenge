package batch.scheduler

import batch.scheduler.controller.CLI
import io.micronaut.function.FunctionBean
import io.micronaut.runtime.Micronaut
import java.util.function.Function
import io.micronaut.function.executor.FunctionApplication


@FunctionBean("batch-scheduler-test")
class FunctionApplication(private val cli: CLI): Function<String,String> {

    override fun apply(input: String): String {
        return cli.execute(input)
    }

    companion object Application {

        @JvmStatic
        fun main(args: Array<String>) {
            Micronaut.build()
                .packages("batch.scheduler")
                .mainClass(FunctionApplication::class.java)
                .start()
        }
    }
}