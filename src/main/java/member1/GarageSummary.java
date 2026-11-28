package member1;

public record GarageSummary(
        int totalCapacity,          
        int activeVehicles,         
        int availableSlots,         
        double occupancyRate,      
        double totalRevenue,        
        int totalCompletedSessions, 
        int peakOccupancy           
) {

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
