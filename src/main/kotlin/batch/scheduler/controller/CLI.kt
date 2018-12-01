package batch.scheduler.controller

import batch.scheduler.domain.*
import batch.scheduler.service.CommandService
import java.lang.StringBuilder
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Singleton
import java.util.regex.Pattern


@Singleton
class CLI(private val commandService: CommandService) {

    private enum class Tokens { CITY, CITIES, BATCH, BATCHES, SCHEDULE, CANCEL, SHOW }
    private var terminateProcess: Boolean = false

    fun start() {

        print(getUsage())
        print("> ")

        // listen on std in
        val scanner = Scanner(System.`in`)
        scanner.use {
            while (!terminateProcess) {
                process(parseWords(scanner.nextLine()))
                print("> ")
            }
        }
    }

    // return words delimited by space, but preserving quotations around phrases
    private fun parseWords(line: String): List<String> {
        val words: ArrayList<String> = ArrayList()
        val m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line)
        while (m.find()) {
            words.add(m.group(1).replace("\"", ""))
        }
        return words
    }

    // todo: make sure to address quotes around city names
    private fun process(input: List<String>) {

        println(input)

        // minimum threshold of expected tokens
        if (input.size < 2) {
            print(getUsage())
            return
        }

        try {

            // process input
            val command1 = input[0]
            when (command1) {
                Tokens.CITY.name -> commandService.createCity(parseCity(input.subList(1, input.size)))
                Tokens.BATCH.name -> commandService.createBatch(parseBatch(input.subList(1, input.size)))
                Tokens.SCHEDULE.name -> commandService.scheduleBatch(parseSchedule(input.subList(1, input.size)))
                Tokens.CANCEL.name -> commandService.cancelBatch(parseCancel(input.subList(1, input.size)))
                Tokens.SHOW.name -> {
                    val command2 = input[1]
                    when (input[1]) {
                        Tokens.CITIES.name -> {
                            val map = commandService.getDeploymentsByCity()
                            map.keys.forEach {
                                println(it)                                             // print city
                                map[it]?.forEach { deployment -> println(deployment) }  // print deployments
                            }
                        }
                        Tokens.BATCHES.name -> {
                            val map = commandService.getDeploymentsByBatch()
                            map.keys.forEach {
                                println("BATCH $it")                                    // print batch
                                map[it]?.forEach { deployment -> println(deployment) }  // print deployments
                            }
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
        } catch (e: RuntimeException) {
            e.printStackTrace()
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

    private fun parseCancel(input: List<String>): CancelDeployment {

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
                .append("Batch scheduler CLI tool for managing Bird deployments.\n\n")
                .append("Options:\n")
                .append("  -h, --help           Show this help message.\n")
                .append("  -q, --quit           Exit the command line.\n\n")
                .append("Commands:\n")
                .append("  CITY \"<name>\" <latitude> <longitude> [<cap>]         Creates a city.\n")
                .append("  BATCH <id> <size>                                    Creates a batch.\n")
                .append("  SCHEDULE <batch-id> \"<city>\" <start-date> <end-date> Deploys a batch to a city.\n")
                .append("  CANCEL <city> <date>                                 Cancels batches for the given city and date.\n")
                .append("  SHOW CITIES                                          Prints scheduled deployments by city.\n")
                .append("  SHOW BATCHES                                         Prints scheduled deployments by batch.\n")
                .append("\n")

        return sb.toString()
    }
}