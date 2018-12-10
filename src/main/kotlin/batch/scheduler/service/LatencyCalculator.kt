package batch.scheduler.service

import batch.scheduler.domain.Coordinate
import javax.inject.Singleton


@Singleton
class LatencyCalculator {

    companion object {
        const val BATCH_TRAVEL_SPEED_KM_PER_HOUR: Double = 50.0
        const val DISTANCE_KM_PER_DEGREE: Double = 111.0
    }

    /**
     * Calculate the time in millis to travel from c1(x,y) to c2(x,y). All calculations are performed using Doubles.
     * For greater precision, upgrade to BigDecimal.
     */
    fun latencyInMillis(c1: Coordinate, c2: Coordinate): Long {
        val degrees = calculateDistanceBetweenPoints(c1.latitude, c1.longitude, c2.latitude, c2.longitude)
        val km = degrees * DISTANCE_KM_PER_DEGREE
        val hours = (1.0 / BATCH_TRAVEL_SPEED_KM_PER_HOUR) * km
        val millis = hours * 60.0 * 60.0 * 1000.0
        return Math.round(millis)
    }

    /**
     * Calculates the distance between (x1,y1) and (x2,y2).
     * <p>
     * @see: https://www.baeldung.com/java-distance-between-two-points
     */
    private fun calculateDistanceBetweenPoints(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1))
    }
}