package member1;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - GarageSummary Record (Java 17)
 * ============================================================================
 * Garage ki poori state ka ek read-only snapshot.
 * Java 17 ka `record` use kiya hai — automatically equals, hashCode, toString banata hai.
 *
 * Dashboard ya viva ke liye ek call me sab stats mil jaate hain:
 *   GarageSummary s = garage.getSummarySnapshot();
 *   System.out.println(s.occupancyRate());   // e.g., 30.0%
 *   System.out.println(s.peakOccupancy());   // e.g., 15
 *
 * FIELDS:
 *   totalCapacity        : Garage me total kitne parking bays hain
 *   activeVehicles       : Abhi kitni gaadiyaan parked hain
 *   availableSlots       : Kitne slots khali hain (park ke liye ready)
 *   occupancyRate        : Kitna percent bhara hai (0.0 to 100.0)
 *   totalRevenue         : Ab tak ki total kamaai (₹)
 *   totalCompletedSessions : Kitni gaadiyaan exit karke jaa chuki hain
 *   peakOccupancy        : Ek saath maximum kitni gaadiyaan thi (high watermark)
 * ============================================================================
 */
public record GarageSummary(
        int totalCapacity,
        int activeVehicles,
        int availableSlots,
        double occupancyRate,
        double totalRevenue,
        int totalCompletedSessions,
        int peakOccupancy
) {
    /**
     * Readable summary string for logging aur display ke liye.
     * Default toString() record ka raw format deta hai, yeh pretty hai.
     */
    public String toPrettyString() {
        return String.format(
                "=== Garage Summary ===%n" +
                "Total Capacity       : %d%n" +
                "Active Vehicles      : %d%n" +
                "Available Slots      : %d%n" +
                "Occupancy Rate       : %.1f%%%n" +
                "Total Revenue        : ₹%.2f%n" +
                "Completed Sessions   : %d%n" +
                "Peak Occupancy       : %d",
                totalCapacity, activeVehicles, availableSlots,
                occupancyRate, totalRevenue, totalCompletedSessions, peakOccupancy
        );
    }
}
