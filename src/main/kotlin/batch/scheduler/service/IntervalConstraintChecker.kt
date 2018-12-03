package batch.scheduler.service

import batch.scheduler.domain.exceptions.BatchConstraintException
import batch.scheduler.domain.exceptions.CityCapConstraintException
import com.brein.time.timeintervals.collections.ListIntervalCollection
import com.brein.time.timeintervals.indexes.IntervalTree
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder
import com.brein.time.timeintervals.intervals.IdInterval
import com.brein.time.timeintervals.intervals.LongInterval
import java.io.Serializable
import java.time.ZonedDateTime
import javax.inject.Singleton


@Singleton
class IntervalConstraintChecker(private val latencyCalculator: LatencyCalculator) {

    /**
     * Batch data class associated with intervals
     */
    data class Batch(val batchNumber: Int, val size: Int) : Comparable<Batch>, Serializable {

        override fun hashCode(): Int {
            return Integer.hashCode(batchNumber)
        }

        override fun equals(other: Any?): Boolean {
            return if (other == null || other !is Batch) false
                else batchNumber == other.batchNumber
        }

        override fun compareTo(other: Batch): Int {
            return batchNumber - other.batchNumber
        }
    }

    private val intervalsByBatchNumber: MutableMap<Int, IntervalTree> = HashMap()
    private val intervalsByCity: MutableMap<String, IntervalTree> = HashMap()


    fun addIntervalConstraints(batchNumber: Int, batchSize: Int, city: String, cityCap: Int, start: ZonedDateTime,
                               end: ZonedDateTime) {

        val batchInterval: IdInterval<Batch, Long> = createBatchInterval(batchNumber, batchSize, start, end)

        // process batch constraint
        if (!processBatchConstraint(batchInterval)) {
            throw BatchConstraintException(
                    "Cannot schedule deployment; batch $batchNumber already deployed for period ($start, $end)")
        }

        // process city cap constraint
        val excess: Int = processCityCapConstraint(city, cityCap, batchInterval)
        if (excess > 0) {
            throw CityCapConstraintException(
                    "Cannot schedule deployment; city cap exceeded by $excess Birds for period ($start, $end)")
        }
    }

    fun removeIntervalConstraints(batchNumber: Int, city: String, date: ZonedDateTime) {

        // remove from batch map
        intervalsByBatchNumber.remove(batchNumber)

        val treeMatches =
                intervalsByCity[city]?.overlap(createBatchInterval(batchNumber, 0, date, null)) ?: listOf()
        if (!treeMatches.isEmpty()) {

            // remove from city map's tree interval
            treeMatches
                .asSequence()
                .map { if (it is IdInterval<*,*>) it else null }
                .filter { it != null }
                .filter { (it?.getId() as Batch).batchNumber == batchNumber }
                .forEach { treeMatches.remove(it) }
        }
    }

    private fun processBatchConstraint(interval: IdInterval<Batch,Long>): Boolean {

        var batchTree = intervalsByBatchNumber[interval.id.batchNumber]

        // no tree exists for batch, so create it
        if (batchTree == null) {
            batchTree = createIntervalTree()
            batchTree.add(interval)
            intervalsByBatchNumber[interval.id.batchNumber] = batchTree
        }

        // interval tree exists for batch
        else {

            // overlap detected for batch, so block scheduling deployment as it would constitute a double-booking of
            // the batch for the given time period
            if (batchTree.overlap(interval).isNotEmpty()) {
                return false
            }

            // add batch interval
            batchTree.add(interval)
        }

        return true
    }

    private fun processCityCapConstraint(city: String, cityCap: Int, interval: IdInterval<Batch,Long>): Int {

        var cityTree = intervalsByCity[city]

        // no tree exists for city, so create it
        if (cityTree == null) {
            cityTree = createIntervalTree()
            cityTree.add(interval)
            intervalsByCity[city] = cityTree
        }

        // interval tree exists for city
        else {

            // sum up all batch sizes associated with overlapping intervals
            val summedBatchSizes: Int = cityTree.overlap(interval)
                    .asSequence()
                    .map { if (it is IdInterval<*,*>) it.getId() as Batch else null }
                    .filter { it != null }
                    .sumBy { it?.size ?: 0 }

            val total: Int = summedBatchSizes + interval.id.size
            val excess: Int = total - cityCap

            // city cap violated, so block scheduling the deployment
            if (excess > 0) {
                return excess
            }

            // add city batch interval
            cityTree.add(interval)
        }

        return 0
    }

    private fun createIntervalTree(): IntervalTree {
        return IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals { ListIntervalCollection() }
                .build()
    }

    private fun createBatchInterval(batchNumber: Int, batchSize: Int, start: ZonedDateTime, end: ZonedDateTime?): IdInterval<Batch,Long> {
        return IdInterval(
                Batch(batchNumber, batchSize),
                LongInterval(
                        start.toInstant().toEpochMilli(),
                        end?.toInstant()?.toEpochMilli(),true,true))
    }
}