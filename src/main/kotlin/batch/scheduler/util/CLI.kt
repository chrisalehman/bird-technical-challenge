package batch.scheduler.util

import batch.scheduler.domain.*
import batch.scheduler.service.CommandService
import java.lang.StringBuilder
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton


@Singleton
class CLI(private val commandService: CommandService) {

    private enum class Token { CITY, CITIES, BATCH, BATCHES, SCHEDULE, CANCEL, SHOW }
    private var terminateProcess: Boolean = false

    fun start() {

        print(getUsage())
        print("> ")

        // listen on stdin
        val scanner = Scanner(System.`in`)
        scanner.use {

            while (!terminateProcess) {

                // read line of text
                val args: List<String> = ArrayList(scanner.nextLine().split(" "))

                // execute command line interpreter
                parse(args)

                print("> ")
            }
        }
    }

    private fun parse(input: List<String>) {

        // minimum threshold of expected tokens
        if (input.size < 2) {
            print(getUsage())
            return
        }

        try {

            // parse input
            val command1 = input[0]
            when (command1) {
                Token.CITY.name -> commandService.createCity(parseCity(input.subList(1, input.size)))
                Token.BATCH.name -> commandService.createBatch(parseBatch(input.subList(1, input.size)))
                Token.SCHEDULE.name -> commandService.scheduleBatch(parseSchedule(input.subList(1, input.size)))
                Token.CANCEL.name -> commandService.cancelBatch(parseCancel(input.subList(1, input.size)))
                Token.SHOW.name -> {
                    val command2 = input[1]
                    when (input[1]) {
                        Token.CITIES.name -> commandService.showCities()
                        Token.BATCHES.name -> commandService.showBatches()
                        else -> println("Invalid command: $command1 $command2")
                    }
                }
                else -> println("Invalid command: $command1")
            }

        } catch (e: ArgumentListException) {
            println("Missing arguments")
        } catch (e: NumberFormatException) {
            println("Invalid arguments")
        } catch (e: RuntimeException) {
            throw e
        }

    }

    private fun parseCity(input: List<String>): City {

        if (input.size < 3) {
           throw ArgumentListException("Missing arguments")
        }

        val name: String = input[0]
        val latitude: Float = input[1].toFloat()
        val longitude: Float = input[2].toFloat()
        val cap: Int = if (input.size > 3) input[3].toInt() else Integer.MAX_VALUE

        return if (input.size > 3) City(name, latitude, longitude, cap)
            else City(name, latitude, longitude)
    }

    private fun parseBatch(input: List<String>): Batch {

        if (input.size < 2) {
            throw ArgumentListException("Missing arguments")
        }

        val id: Int = input[0].toInt()
        val count: Int = input[1].toInt()

        return Batch(id, count)
    }

    private fun parseSchedule(input: List<String>): Deployment {

        if (input.size < 4) {
            throw ArgumentListException("Missing arguments")
        }

        val batchId: Int = input[0].toInt()
        val city: String = input[1]
        val startDate: ZonedDateTime = ZonedDateTime.parse(input[2])
        val endDate: ZonedDateTime = ZonedDateTime.parse(input[3])

        return Deployment(batchId, city, startDate, endDate)
    }

    private fun parseCancel(input: List<String>): Cancellation {

        if (input.size < 3) {
            throw ArgumentListException("Missing arguments")
        }

        val batchId: Int = input[0].toInt()
        val city: String = input[1]
        val date: ZonedDateTime = ZonedDateTime.parse(input[2])

        return Cancellation(batchId, city, date)
    }

    private fun getUsage(): String {

        val sb = StringBuilder()
                .append("\nUsage: COMMAND [-hq] <arguments...>\n")
                .append("Batch scheduler CLI tool for managing Bird deployments.\n\n")
                .append("Options:\n")
                .append("  -h, --help           Show this help message.\n")
                .append("  -q, --quit           Exit the command line.\n\n")
                .append("Commands:\n")
                .append("  CITY <name> <latitude> <longitude> [<cap>]           Creates a city.\n")
                .append("  BATCH <id> <size>                                    Creates a batch.\n")
                .append("  SCHEDULE <batch-id> <city> <start-date> <end-date>   Deploys a batch to a city.\n")
                .append("  CANCEL <city> <date>                                 Cancels batches for the given city and date.\n")
                .append("  SHOW CITIES                                          Prints scheduled deployments by city.\n")
                .append("  SHOW BATCHES                                         Prints scheduled deployments by batch.\n")
                .append("\n")

        return sb.toString()
    }
}