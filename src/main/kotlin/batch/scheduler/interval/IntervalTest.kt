package batch.scheduler.interval

import com.brein.time.timeintervals.intervals.LongInterval
import com.brein.time.timeintervals.collections.ListIntervalCollection
import com.brein.time.timeintervals.filters.IntervalFilter
import com.brein.time.timeintervals.filters.IntervalFilters
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder.IntervalType
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder
import com.brein.time.timeintervals.indexes.IntervalValueComparator
import com.brein.time.timeintervals.intervals.IInterval
import com.brein.time.timeintervals.intervals.IdInterval



class IntervalTest {

    companion object {
        private val ID_A: Long = 1L
        private val ID_B: Long = 2L

        @JvmStatic
        fun main(args: Array<String>) {

            val tree = IntervalTreeBuilder.newBuilder()
                    .usePredefinedType(IntervalType.LONG)
                    .collectIntervals { ListIntervalCollection() }
                    .build()

            tree.add(IdInterval<Long,Long>(ID_A, LongInterval(1L, 5L, true, true)))
            tree.add(IdInterval<Long,Long>(ID_A, LongInterval(2L, 5L, true, true)))
            tree.add(IdInterval<Long,Long>(ID_A, LongInterval(3L, 5L, true, true)))
//            tree.add(IdInterval<Long,Long>(ID_B, LongInterval(1L, 5L, true, true)))
//            tree.add(IdInterval<Long,Long>(ID_B, LongInterval(2L, 5L, true, true)))
//            tree.add(IdInterval<Long,Long>(ID_B, LongInterval(3L, 5L, true, true)))

//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(1L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(2L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(3L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(4L, 5L)))
//            batches.add(IdInterval<Long,Long>(BATCH_ID, LongInterval(5L, 6L)))

            val a = tree.overlap(IdInterval(ID_A, LongInterval(1L, 5L, true, true)))
            println("Overlapping--")
            a.forEach { println("   $it") }

            val b = tree.find(IdInterval(ID_A, LongInterval(1L, 5L, true, true)))
            println("Find exact--")
            b.forEach { println("   $it") }

//            val c = tree.find(
//                    IdInterval(ID_A, LongInterval(1L, 2L, true, true)),
//                    this::strictlyGreaterIntervals)
//            println("Find greater than first's end--")
//            c.forEach { println("   $it") }
//
//            val b = batches.find(IdInterval(BATCH_ID,2L, 5L))
//            b.forEach { println("batch $it") }
        }

        fun strictlyGreaterIntervals(cmp: IntervalValueComparator,
                     i1: IInterval<*>,
                     i2: IInterval<*>): Boolean {
            return cmp.compare(i2, i1) < 0
        }
    }
}