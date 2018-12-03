package batch.scheduler

import io.micronaut.function.client.FunctionClient
import javax.inject.Named


@FunctionClient
interface FunctionApplicationClient {

    @Named("batch-scheduler-test")
    fun apply(input: String): String
}