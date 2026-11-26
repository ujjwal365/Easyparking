package member2;

/**
 * ============================================================================
 * MEMBER 2 (Brain) - FeeBreakdown Record
 * ============================================================================
 * Yeh record parking session ke poore kharche ka hisaab-kitaab rakhta hai:
 * - parkingFee        : Normal parking ke charges (hourly + daily cap ke hisaab se)
 * - chargingFee       : EV charging ke charges (agar EV_CHARGING slot mila ho)
 * - totalFee          : Dono ka jod (parkingFee + chargingFee)
 * - totalMinutes      : Gaadi kitne minute khadi rahi
 * - billableHours     : Kitne ghanton ka charge laga (Math.ceil ke baad)
 * - isChargingApplied : Kya charging ka paisa joda gaya hai ya nahi
 * ============================================================================
 */
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

