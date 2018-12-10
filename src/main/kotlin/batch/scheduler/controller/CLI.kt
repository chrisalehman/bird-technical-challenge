package batch.scheduler.controller

import batch.scheduler.domain.*
import batch.scheduler.domain.exceptions.*
import batch.scheduler.service.CommandQueryProcessor
import org.slf4j.LoggerFactory
import java.lang.StringBuilder
import java.time.OffsetDateTime
import java.util.Scanner
import javax.inject.Singleton
import java.util.regex.Pattern


/**
 * Basic command line interpreter. Provides a while-loop mode for listening for stdin, and a request/reply mode
 * that can be used for testing.
 */
@Singleton class CLI(private val processor: CommandQueryProcessor) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CLI::class.java)
    }

    private enum class Tokens { HELP, QUIT, CITY, CITIES, BATCH, BATCHES, SCHEDULE, CANCEL, SHOW }
    private var terminateProcess: Boolean = false

    /**
     * Normal entry point for CLI application. Executes in a while loop, blocking on stdin for commands from the
     * user.
     */
    fun start() {

        print(getUsage())
        print("> ")

        // listen on std in
        val scanner = Scanner(System.`in`)
        scanner.use {
            while (!terminateProcess) {
                print(process(parseTokens(scanner.nextLine())))
                print("> ")
            }
        }
    }

    /**
     * Used for executing request/response communication with service. Facilitates testing. Departing from the above
     * style, return 'OK' when the command has completed to guarantee there is always a response to interpret from
     * the test.
     */
    fun execute(input: String): String {
        val result: String = process(parseTokens(input))
        return if (result.isEmpty()) "OK" else result
    }

    // return words delimited by space, but preserving quotations around phrases
    private fun parseTokens(line: String): List<String> {
        val words: ArrayList<String> = ArrayList()
        val m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line.trim())
        while (m.find()) {
            words.add(m.group(1).replace("\"", ""))
        }
        return words
    }

    private fun process(input: List<String>): String {

        // minimum threshold of expected tokens
        if (input.isEmpty()) {
            return getUsage()
        }

        val out: StringBuilder = StringBuilder()
        try {

            // process input
            val command1 = input[0]

            // next check for commands
            when (command1) {
                Tokens.HELP.name -> print(getUsage())
                Tokens.QUIT.name -> terminateProcess = true
                Tokens.CITY.name -> processor.createCity(parseCityCommand(input.subList(1, input.size)))
                Tokens.BATCH.name -> processor.createBatch(parseBatchCommand(input.subList(1, input.size)))
                Tokens.SCHEDULE.name -> processor.scheduleDeployment(parseScheduleCommand(input.subList(1, input.size)))
                Tokens.CANCEL.name -> processor.cancelDeployment(parseCancelCommand(input.subList(1, input.size)))
                Tokens.SHOW.name -> {
                    val command2 = input[1]
                    when (input[1]) {
                        Tokens.CITIES.name -> {
                            val map = processor.getDeploymentsByCity()
                            map.keys.forEach {
                                out.append(it).append("\n")
                                map[it]?.forEach { e -> out.append(e).append("\n") }
                            }
                            return out.toString()
                        }
                        Tokens.BATCHES.name -> {
                            val map = processor.getDeploymentsByBatch()
                            map.keys.forEach {
                                out.append("BATCH $it\n")
                                map[it]?.forEach { e -> out.append(e).append("\n") }
                            }
                            return out.toString()
                        }
                        Tokens.CITY.name -> if (input.size <= 2) println("Invalid arguments") else {
                            processor.getDeployments(input[2])
                                .forEach { e -> out.append(e).append("\n") }
                            return out.toString()
                        }
                        Tokens.BATCH.name -> if (input.size <= 2) println("Invalid arguments") else {
                            processor.getDeployments(input[2].toInt())
                                    .forEach { e -> out.append(e).append("\n") }
                        }
                        else -> return out.append("Invalid command: $command1 $command2\n").toString()
                    }
                }
                else -> return out.append("Invalid command: $command1\n").toString()
            }

        } catch (e: ArgumentListException) {
            return out.append("Missing arguments\n").toString()
        } catch (e: NumberFormatException) {
            return out.append("Invalid arguments\n").toString()
        } catch (e: BusinessException) {
            return out.append(e.message).append("\n").toString()
        } catch (e: RuntimeException) {
            LOG.error(e.message, e)
            return out.append("Unexpected error; see application log for details\n").toString()
        }

        return out.toString()
    }

    private fun parseCityCommand(input: List<String>): CreateCity {

        if (input.size < 3) {
           throw ArgumentListException("Missing arguments")
        }

        val name: String = input[0]
        val latitude: Double = input[1].toDouble()
        val longitude: Double = input[2].toDouble()
        val cap: Int = if (input.size > 3) input[3].toInt() else Integer.MAX_VALUE

        return if (input.size > 3) CreateCity(name, Coordinate(latitude, longitude), cap)
            else CreateCity(name, Coordinate(latitude, longitude))
    }

    private fun parseBatchCommand(input: List<String>): CreateBatch {

        if (input.size < 2) {
            throw ArgumentListException("Missing arguments")
        }

        val id: Int = input[0].toInt()
        val count: Int = input[1].toInt()

        return CreateBatch(id, count)
    }

    private fun parseScheduleCommand(input: List<String>): ScheduleDeployment {

        if (input.size < 4) {
            throw ArgumentListException("Missing arguments")
        }

        val batchId: Int = input[0].toInt()
        val city: String = input[1]
        val startDate: OffsetDateTime = OffsetDateTime.parse(input[2])
        val endDate: OffsetDateTime = OffsetDateTime.parse(input[3])

        return ScheduleDeployment(batchId, city, startDate, endDate)
    }

    private fun parseCancelCommand(input: List<String>): CancelDeployment {

        if (input.size < 3) {
            throw ArgumentListException("Missing arguments")
        }

        val batchId: Int = input[0].toInt()
        val city: String = input[1]
        val date: OffsetDateTime = OffsetDateTime.parse(input[2])

        return CancelDeployment(batchId, city, date)
    }

    private fun getUsage(): String {

        val sb = StringBuilder()
                .append("\nUsage: COMMAND [<arguments...>\n")
                .append("Batch scheduler CLI tool for managing Bird deployments.\n\n")
                .append("Commands:\n")
                .append("  HELP                                                     Show this help message.\n")
                .append("  QUIT                                                     Terminate the command line.\n")
                .append("  CITY \"<name>\" <latitude> <longitude> [<cap>]             Creates a city.\n")
                .append("  BATCH <id> <size>                                        Creates a batch.\n")
                .append("  SCHEDULE <batch-id> \"<city>\" <start-date> <end-date>     Deploys a batch to a city.\n")
                .append("  CANCEL <batch-id> \"<city>\" <date>                        Cancels a batch deployment based on the batch-id, city and date.\n")
                .append("  SHOW CITIES                                              Prints scheduled deployments by city.\n")
                .append("  SHOW CITY \"<city>\"                                       Prints scheduled deployments for a city.\n")
                .append("  SHOW BATCHES                                             Prints scheduled deployments by batch.\n")
                .append("  SHOW BATCH <batch-id>                                    Prints scheduled deployment.\n")
                .append("\n")

        return sb.toString()
    }
}