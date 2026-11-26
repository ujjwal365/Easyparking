package member1;

/**
 * GarageSummary — Poore garage ke live stats ka ek compact snapshot (Java 17 Record).
 * WHY JAVA RECORD (Normal class ki jagah `record` kyu use kiya?):
 *   Record ek read-only data holder hai. Yeh apne aap constructor, getters, equals(), hashCode(),
 *   Isse dashboard ya summary report ko ek hi function call me saara data mil jaata hai.
 */
public record GarageSummary(
        int totalCapacity,          // Garage me kul parking bays
        int activeVehicles,         // Abhi andar kitni gaadiyaan khadi hain
        int availableSlots,         // Abhi kitne bays khaali hain
        double occupancyRate,       // Garage kitna % bhara hai (0.0 - 100.0)
        double totalRevenue,        // Ab tak ki kul kamaai (₹)
        int totalCompletedSessions, // Kitni gaadiyaan checkout kar chuki hain
        int peakOccupancy           // Ek saath maximum kitni gaadiyaan park hui thi
) {
    //Saare stats ko ek multi-line summary text me format karke print karne ke liye.
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
