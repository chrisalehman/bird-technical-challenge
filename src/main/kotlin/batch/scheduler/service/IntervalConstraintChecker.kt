package batch.scheduler.service

import batch.scheduler.domain.Batch
import batch.scheduler.domain.City
import batch.scheduler.domain.Coordinate
import batch.scheduler.domain.Deployment
import batch.scheduler.domain.exceptions.BatchConstraintException
import batch.scheduler.domain.exceptions.CityCapConstraintException
import com.brein.time.timeintervals.collections.ListIntervalCollection
import com.brein.time.timeintervals.indexes.IntervalTree
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder
import com.brein.time.timeintervals.intervals.IdInterval
import com.brein.time.timeintervals.intervals.LongInterval
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import javax.inject.Singleton


/**
 * Core class for handling interval overlapping logic. It utilizes an interval tree data structure for achieving
 * O(log(n)) time complexity for inserts, removals, and finding incidence of overlap. O(log(n)+k) is required to
 * find a specific interval among an overlap set.
 *
 *  1. Batch conflict map:  batchId -> (city name -> city tuple (contains interval tree))
 *  2. City cap map: City name -> batch interval tree
 */
@Singleton class IntervalConstraintChecker(private val latencyCalculator: LatencyCalculator) {

    companion object {
        private val LOG = LoggerFactory.getLogger(IntervalConstraintChecker::class.java)
    }

    /**
     * Associates data required for calculating overlaps for the same batch across cities, taking travel time into
     * account.
     */
    data class CityTuple(val city: String, val location: Coordinate, val intervalTree: IntervalTree)

    private val batchConflictIntervals: MutableMap<Int, MutableMap<String, CityTuple>> = HashMap()
    private val cityCapIntervals: MutableMap<String, IntervalTree> = HashMap()


    fun addIntervalConstraints(d: Deployment) {

        val batchInterval: IdInterval<Batch, Long> = createBatchInterval(d.batch, d.start, d.end)

        // 1. process batch constraint, adding interval to associated interval tree if successful
        if (!processBatchConstraint(d.city, batchInterval)) {
            throw BatchConstraintException(
                    "Cannot schedule deployment; batch ${d.batch.batchNumber} has a conflict")
        }

        // 2. process city cap constraint, adding interval to associated interval tree if successful
        val diff: Int = processCityCapConstraint(d.city, batchInterval)
        if (diff > 0) {

            // city cap rule violated, so we need to clean up - interval needs to be removed from batch constraint
            // interval tree
            removeIntervalConstraints(d.batch.batchNumber, d.city.name, d.start)

            throw CityCapConstraintException(
                    "Cannot schedule deployment; city cap exceeded by $diff Birds for given period")
        }
    }

    fun removeIntervalConstraints(batchNumber: Int, city: String, date: OffsetDateTime) {

        // interval only has one date - works as long as it falls with range of intervals
        val interval = createBatchInterval(Batch(batchNumber, 0), date, null)

        // 1. remove interval from batch conflict interval tree
        val bcTree = batchConflictIntervals[batchNumber]?.get(city)?.intervalTree
        val bcMatch: IdInterval<*,*>? = (bcTree?.overlap(interval) ?: listOf())
            .asSequence()
            .map { if (it is IdInterval<*,*>) it else null }
            .firstOrNull { it != null && (it.getId() as Batch).batchNumber == batchNumber }
        if (bcMatch != null) {
            if (bcTree?.remove(bcMatch) == false) {
                LOG.warn("Failed to remove interval $bcMatch from batch conflict interval tree")
            }
        }

        // 2. remove interval from city cap interval tree
        val cpiTree = cityCapIntervals[city]
        val cpiMatch: IdInterval<*,*>? = (cpiTree?.overlap(interval) ?: listOf())
            .asSequence()
            .map { if (it is IdInterval<*,*>) it else null }
            .firstOrNull { it != null && (it.getId() as Batch).batchNumber == batchNumber }
        if (cpiMatch != null) {
            if (cpiTree?.remove(cpiMatch) == false) {
                LOG.warn("Failed to remove interval $cpiMatch from city cap interval tree")
            }
        }
    }

    /**
     * Process batch conflict rule against own city and other cities, taking travel time into account.
     *
     * Algorithm:
     *  1. Look up map of city to "city tuple" based on batch number as we're only interested in testing conflicts
     *     against the same batch.
     *
     *  2. Iterate through map, checking whether there is any overlap with each city's interval tree.
     *
     *      a) For a deployment in the same city, rely on the vanilla overlap logic of the interval tree data structure.
     *         If a match is found, immediately bail - there is a conflict.
     *
     *      b) To test against a *different* city, it is necessary to take travel time into account. Extend
     *         the given deployment's interval in both directions based on the travel latency between cities. We need
     *         to account for the given deployment being both the origin and destination between cities. If there is
     *         an overlap with the extended interval, immediately bail - there is a conflict.
     *
     *  3. If by the end no conflict is found, we're good! Add the interval to the interval tree of the same city.
     *
     * @param city
     * @param interval - The interval we're testing
     * @return - True if successful, false otherwise.
     */
    private fun processBatchConstraint(city: City, interval: IdInterval<Batch,Long>): Boolean {

        var secondaryMap: MutableMap<String,CityTuple>? = batchConflictIntervals[interval.id.batchNumber]

        // no secondary map, so create one
        if (secondaryMap == null) {
            secondaryMap = HashMap()
            batchConflictIntervals[interval.id.batchNumber] = secondaryMap
        }

        // iterate through secondary map, looking for conflicts...

        for ((kCity, vCityTuple) in secondaryMap) {

            // no distance calculation required for same city
            if (kCity == city.name) {

                // overlap detected for batch, so block scheduling deployment as it would constitute a double-booking of
                // the batch for the given time period
                if (vCityTuple.intervalTree.overlap(interval).isNotEmpty()) {
                    return false
                }
            }

            // different city, so distance calculation applies
            else {

                // calculate latency between cities
                val latencyInMillis: Long = latencyCalculator.latencyInMillis(city.location, vCityTuple.location)

                // create extended interval in both directions - we need to account for conflicts where the given
                // city is the origin as well as the destination
                val intervalExtended = createBatchInterval(
                        interval.id,
                        interval.normStart - latencyInMillis,
                        interval.normEnd + latencyInMillis)

                // overlap detected for batch, so block scheduling deployment as it would constitute a double-booking of
                // the batch for the given time period
                if (vCityTuple.intervalTree.overlap(intervalExtended).isNotEmpty()) {
                    return false
                }
            }
        }

        // if we're here, it means there are no conflicts... proceed with adding

        var cityTuple = secondaryMap[city.name]

        // no city tuple exists, so create
        if (cityTuple == null) {

            // add interval to interval tree
            val intervalTree = createIntervalTree()
            intervalTree.add(interval)

            // add city tuple to secondary map
            cityTuple = CityTuple(city.name, city.location, intervalTree)
            secondaryMap[city.name] = cityTuple
        }

        // city tuple exists, add interval to existing interval tree
        else {
            cityTuple.intervalTree.add(interval)
        }

        return true
    }

    /**
     * Process city cap rule for the deployment.
     *
     * Algorithm:
     *  1. Look up interval tree based on the given deployment's city.
     *
     *  2. Search interval tree for overlaps with the given interval.
     *
     *  3. For all matches, sum up batch sizes, including the size of the proposed deployment's batch, and compare
     *     against the city's cap.
     *
     *     a) If the cap is exceeded, the rule is violated. Bail out. Return the number of Birds that exceeded the
     *        city's cap (for informational purposes).
     *
     *     b) Otherwise, add the interval to the data structure and return zero.
     *
     * @param city
     * @param interval
     * @return The number of Birds that exceed the city cap, or zero if there is no violation.
     */
    private fun processCityCapConstraint(city: City, interval: IdInterval<Batch,Long>): Int {

        var intervalTree = cityCapIntervals[city.name]

        // first check if the city's cap is violated by the deployment itself
        var diff: Int = interval.id.size - city.cap
        if (diff > 0) {
            return diff
        }

        // no tree exists for city, so create it
        if (intervalTree == null) {
            intervalTree = createIntervalTree()
            intervalTree.add(interval)
            cityCapIntervals[city.name] = intervalTree
        }

        // interval tree exists for city
        else {

            // sum up all batch sizes associated with overlapping intervals
            val summedBatchSizes: Int = intervalTree.overlap(interval)
                    .asSequence()
                    .map { if (it is IdInterval<*,*>) it.getId() as Batch else null }
                    .filterNotNull()
                    .sumBy { it.size }

            val total: Int = summedBatchSizes + interval.id.size
            diff = total - city.cap

            // city cap violated, so block scheduling the deployment
            if (diff > 0) {
                return diff
            }

            // add city batch interval
            intervalTree.add(interval)
        }

        return 0
    }

    private fun createIntervalTree(): IntervalTree {
        return IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals { ListIntervalCollection() }
                .build()
    }

    private fun createBatchInterval(batch: Batch, start: OffsetDateTime, end: OffsetDateTime?): IdInterval<Batch,Long> {
        return IdInterval(
                batch,
                LongInterval(
                        start.toInstant().toEpochMilli(),
                        end?.toInstant()?.toEpochMilli(),true,true))
    }

    private fun createBatchInterval(batch: Batch, startInMillis: Long, endInMillis: Long?): IdInterval<Batch,Long> {
        return IdInterval(
                Batch(batch.batchNumber, batch.size),
                LongInterval(startInMillis, endInMillis,true,true))
    }
}