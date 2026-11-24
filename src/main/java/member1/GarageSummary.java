package member1;

/**
 * GarageSummary — Garage ki poori state ek snapshot me (Java 17 record).
 *
 * Usage:
 *   GarageSummary s = garage.getSummarySnapshot();
 *   s.activeVehicles()   → kitni gaadiyaan andar hain
 *   s.occupancyRate()    → kitna % bhara hai
 *   s.peakOccupancy()    → ek saath max kitni gaadiyaan thi
 *
 * Record automatically equals(), hashCode(), toString() bana deta hai.
 */
public record GarageSummary(
        int totalCapacity,          // total parking bays
        int activeVehicles,         // abhi kitni gaadiyaan andar
        int availableSlots,         // kitne slots khali
        double occupancyRate,       // % filled (0.0 - 100.0)
        double totalRevenue,        // total kamaai (₹)
        int totalCompletedSessions, // kitni gaadiyaan exit ho chuki
        int peakOccupancy           // max gaadiyaan ek saath (record)
) {
    // Readable format for printing
    public String toPrettyString() {
        return String.format(
                "=== Garage Summary ===%n" +
                "Capacity     : %d%n" +
                "Active       : %d%n" +
                "Available    : %d%n" +
                "Occupancy    : %.1f%%%n" +
                "Revenue      : ₹%.2f%n" +
                "Completed    : %d%n" +
                "Peak         : %d",
                totalCapacity, activeVehicles, availableSlots,
                occupancyRate, totalRevenue, totalCompletedSessions, peakOccupancy
        );
    }
}
