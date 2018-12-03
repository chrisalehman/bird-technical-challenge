package batch.scheduler.controller

import batch.scheduler.domain.*
import batch.scheduler.domain.exceptions.*
import batch.scheduler.service.CommandQueryProcessor
import org.slf4j.LoggerFactory
import java.lang.StringBuilder
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import java.util.regex.Pattern


@Singleton
class CLI(private val processor: CommandQueryProcessor) {

    companion object {
        private val LOG = LoggerFactory.getLogger(CLI::class.java)
    }

    private enum class Tokens { CITY, CITIES, BATCH, BATCHES, SCHEDULE, CANCEL, SHOW }
    private var terminateProcess: Boolean = false

    fun start() {

        print(getUsage())
        print("> ")

        // listen on std in
        val scanner = Scanner(System.`in`)
        scanner.use {
            while (!terminateProcess) {
                process(parseTokens(scanner.nextLine()))
                print("> ")
            }
        }
    }

    // return words delimited by space, but preserving quotations around phrases
    private fun parseTokens(line: String): List<String> {
        val words: ArrayList<String> = ArrayList()
        val m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line)
        while (m.find()) {
            words.add(m.group(1).replace("\"", ""))
        }
        return words
    }

    private fun process(input: List<String>) {

        // minimum threshold of expected tokens
        if (input.size < 2) {
            print(getUsage())
            return
        }

        try {

            // process input
            val command1 = input[0]
            when (command1) {
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
                                println(it)
                                map[it]?.forEach { e -> println(e) }
                            }
                        }
                        Tokens.BATCHES.name -> {
                            val map = processor.getDeploymentsByBatch()
                            map.keys.forEach {
                                println("BATCH $it")
                                map[it]?.forEach { e -> println(e) }
                            }
                        }
                        Tokens.CITY.name -> if (input.size <= 2) println("Invalid arguments") else {
                            processor.getDeployments(input[2])
                                    .forEach { e -> println(e) }
                        }
                        Tokens.BATCH.name -> if (input.size <= 2) println("Invalid arguments") else {
                            processor.getDeployments(input[2].toInt())
                                    .forEach { e -> println(e) }
                        }
                        else -> println("Invalid command: $command1 $command2")
                    }
                }
                else -> println("Invalid command: $command1")
            }

        } catch (e: ArgumentListException) {
            println("Missing arguments")
        } catch (e: NumberFormatException) {
            println("Invalid arguments")
        } catch (e: BusinessException) {
            println(e.message)
        } catch (e: RuntimeException) {
            println("Unexpected error; see application log for details")
            LOG.error(e.message, e)
        }
    }

    private fun parseCityCommand(input: List<String>): CreateCity {

        if (input.size < 3) {
           throw ArgumentListException("Missing arguments")
        }

        val name: String = input[0]
        val latitude: Float = input[1].toFloat()
        val longitude: Float = input[2].toFloat()
        val cap: Int = if (input.size > 3) input[3].toInt() else Integer.MAX_VALUE

        return if (input.size > 3) CreateCity(name, latitude, longitude, cap)
            else CreateCity(name, latitude, longitude)
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
        val startDate: ZonedDateTime = ZonedDateTime.parse(input[2])
        val endDate: ZonedDateTime = ZonedDateTime.parse(input[3])

        return ScheduleDeployment(batchId, city, startDate, endDate)
    }

    private fun parseCancelCommand(input: List<String>): CancelDeployment {

        if (input.size < 3) {
            throw ArgumentListException("Missing arguments")
        }

        val batchId: Int = input[0].toInt()
        val city: String = input[1]
        val date: ZonedDateTime = ZonedDateTime.parse(input[2])

        return CancelDeployment(batchId, city, date)
    }

    private fun getUsage(): String {

        val sb = StringBuilder()
                .append("\nUsage: COMMAND [-hq] <arguments...>\n")
                .append("CreateBatch scheduler CLI tool for managing Bird deployments.\n\n")
                .append("Options:\n")
                .append("  -h, --help           Show this help message.\n")
                .append("  -q, --quit           Exit the command line.\n\n")
                .append("Commands:\n")
                .append("  CITY \"<name>\" <latitude> <longitude> [<cap>]             Creates a city.\n")
                .append("  BATCH <id> <size>                                        Creates a batch.\n")
                .append("  SCHEDULE <batch-id> \"<city>\" <start-date> <end-date>     Deploys a batch to a city.\n")
                .append("  CANCEL <batch-id> \"<city>\" <date>                        Cancels a batch deployment based on the batch-id, city and date.\n")
                .append("  SHOW CITIES                                              Prints scheduled deployments by city.\n")
                .append("  SHOW BATCHES                                             Prints scheduled deployments by batch.\n")
                .append("\n")

        return sb.toString()
    }
}