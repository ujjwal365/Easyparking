package member2;

import member1.SlotType;
import member1.Ticket;
import member1.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {

    public static final long FREE_GRACE_PERIOD_MINUTES = 30;

    public static final double RATE_BIKE_PER_HOUR = 10.0;
    public static final double RATE_CAR_PER_HOUR  = 30.0;
    public static final double RATE_SUV_PER_HOUR  = 50.0;
    public static final double RATE_EV_PER_HOUR   = 40.0;

    public static final double DAILY_CAP_BIKE = 80.0;
    public static final double DAILY_CAP_CAR  = 250.0;
    public static final double DAILY_CAP_SUV  = 400.0;
    public static final double DAILY_CAP_EV   = 350.0;

    public static final double EV_CHARGING_FLAT_FEE = 60.0;
    public static final double EV_CHARGING_RATE_PER_HOUR = 15.0;

    public FeeBreakdown calculateFee(Ticket ticket, LocalDateTime exitTime) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket null nahi ho sakta.");
        }
        return calculateFee(
                ticket.getVehicle().getType(),
                ticket.getSlot().getSlotType(),
                ticket.getEntryTime(),
                exitTime
        );
    }

    public FeeBreakdown calculateFee(VehicleType vehicleType,
                                     SlotType slotType,
                                     LocalDateTime entryTime,
                                     LocalDateTime exitTime) {
        if (vehicleType == null || slotType == null || entryTime == null || exitTime == null) {
            throw new IllegalArgumentException("Sabhi parameters non-null hone chahiye.");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time entry time se pehle ka nahi ho sakta.");
        }

        Duration duration = Duration.between(entryTime, exitTime);
        long totalMinutes = duration.toMinutes();

        // 1. Pehle 30 minute free
        if (totalMinutes <= FREE_GRACE_PERIOD_MINUTES) {
            return new FeeBreakdown(0.0, 0.0, 0.0, totalMinutes, 0, false);
        }

        // 2. Math.ceil se ghanta round up
        long billableHours = (long) Math.ceil((double) totalMinutes / 60.0);

        // 3 & 4. Daily Cap aur Hourly Rates
        double hourlyRate = getHourlyRate(vehicleType);
        double dailyCap   = getDailyCap(vehicleType);

        long fullDays = billableHours / 24;
        long remainingHours = billableHours % 24;

        double remainingHoursFee = Math.min(remainingHours * hourlyRate, dailyCap);
        double baseParkingFee = (fullDays * dailyCap) + remainingHoursFee;

        // 5. EV Charging Fee
        double chargingFee = 0.0;
        boolean chargingApplied = false;

        if (vehicleType == VehicleType.EV && slotType == SlotType.EV_CHARGING) {
            chargingApplied = true;
            chargingFee = EV_CHARGING_FLAT_FEE + (billableHours * EV_CHARGING_RATE_PER_HOUR);
        }

        double totalFee = baseParkingFee + chargingFee;

        return new FeeBreakdown(
                baseParkingFee,
                chargingFee,
                totalFee,
                totalMinutes,
                billableHours,
                chargingApplied
        );
    }

    public double getHourlyRate(VehicleType type) {
        return switch (type) {
            case BIKE -> RATE_BIKE_PER_HOUR;
            case CAR  -> RATE_CAR_PER_HOUR;
            case SUV  -> RATE_SUV_PER_HOUR;
            case EV   -> RATE_EV_PER_HOUR;
        };
    }

    public double getDailyCap(VehicleType type) {
        return switch (type) {
            case BIKE -> DAILY_CAP_BIKE;
            case CAR  -> DAILY_CAP_CAR;
            case SUV  -> DAILY_CAP_SUV;
            case EV   -> DAILY_CAP_EV;
        };
    }
}

