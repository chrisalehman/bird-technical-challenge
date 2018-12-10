package batch.scheduler.domain

import java.io.Serializable
import java.time.OffsetDateTime


/**
 * Represents a location (x,y) coordinate.
 */
data class Coordinate(val latitude: Double, val longitude: Double)

/**
 * Batch data class used in the interval calculations.
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

/**
 * City data class used in interval calculations.
 */
data class City(val name: String, val location: Coordinate, val cap: Int)

/**
 * Deployment data class used in interval calculations.
 */
data class Deployment(val batch: Batch, val city: City, val start: OffsetDateTime, val end: OffsetDateTime?)