package batch.scheduler.interval

import com.brein.time.timeintervals.intervals.LongInterval
import com.brein.time.timeintervals.collections.ListIntervalCollection
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder.IntervalType
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder
import com.brein.time.timeintervals.intervals.IdInterval



class IntervalTest {

    companion object {
        private val CITY_ID_A: Long = 1L
        private val CITY_ID_B: Long = 2L
        private val BATCH_ID_A: Long = 10L
        private val BATCH_ID_B: Long = 11L

        @JvmStatic
        fun main(args: Array<String>) {

            val cities = IntervalTreeBuilder.newBuilder()
                    .usePredefinedType(IntervalType.LONG)
                    .collectIntervals { ListIntervalCollection() }
                    .build()

            val batches = IntervalTreeBuilder.newBuilder()
                    .usePredefinedType(IntervalType.LONG)
                    .collectIntervals { ListIntervalCollection() }
                    .build()

            cities.add(IdInterval<Long,Long>(CITY_ID_A, LongInterval(1L, 5L, true, true)))
            cities.add(IdInterval<Long,Long>(CITY_ID_A, LongInterval(2L, 5L, true, true)))
            cities.add(IdInterval<Long,Long>(CITY_ID_A, LongInterval(3L, 5L, true, true)))
            cities.add(IdInterval<Long,Long>(CITY_ID_B, LongInterval(1L, 5L, true, true)))
            cities.add(IdInterval<Long,Long>(CITY_ID_B, LongInterval(2L, 5L, true, true)))
            cities.add(IdInterval<Long,Long>(CITY_ID_B, LongInterval(3L, 5L, true, true)))

//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(1L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(2L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(3L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(4L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(5L, 6L)))

            val a = cities.overlap(IdInterval(CITY_ID_A, 0L, 2L))
            a.forEach { println("city $it") }
//
//            val b = batches.find(IdInterval(BATCH_ID,2L, 5L))
//            b.forEach { println("batch $it") }
        }
    }
}