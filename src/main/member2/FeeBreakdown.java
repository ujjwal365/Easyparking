package member2;

public record FeeBreakdown(
        double parkingFee,
        double chargingFee,
        double totalFee,
        long totalMinutes,
        long billableHours,
        boolean isChargingApplied
) {
    public String getFormattedSummary() {
        if (isChargingApplied) {
            return String.format("Parking: ₹%.2f + Charging: ₹%.2f = Total: ₹%.2f",
                    parkingFee, chargingFee, totalFee);
        }
        return String.format("Parking: ₹%.2f (No Charging) = Total: ₹%.2f",
                parkingFee, totalFee);
    }
}

