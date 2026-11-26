package garage;

import member1.SlotType;
import member1.VehicleType;
import member2.FeeBreakdown;
import member2.FeeCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * MEMBER 3 (Face) & MEMBER 2 (Brain) - FeeCalculator Tests
 * ============================================================================
 * Yeh test class FeeCalculator ke saare pricing rules ko test karti hai:
 * 1. 30 minute ka free period (<= 30 min -> ₹0.00)
 * 2. Math.ceil se ghante ka round up (31 min -> 1 hr, 61 min -> 2 hrs)
 * 3. Daily Caps (Bike ₹80, Car ₹250, SUV ₹400, EV ₹350)
 * 4. EV Charging Fee (₹60 flat + ₹15/hr sirf EV_CHARGING slot me)
 * 5. EV fallback slot me charging fee ₹0
 * 6. Multi-day stay (24 ghante se upar ka stay)
 * ============================================================================
 */
class FeeCalculatorTest {

    private FeeCalculator calculator;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        calculator = new FeeCalculator();
        baseTime = LocalDateTime.of(2026, 11, 17, 10, 0, 0);
    }

    @Test
    @DisplayName("1. 30 minute ya usse kam ruka toh fees ₹0.00 honi chahiye")
    void testFreeGracePeriod() {
        // 15 minute ka stay
        LocalDateTime exit15m = baseTime.plusMinutes(15);
        FeeBreakdown fee15 = calculator.calculateFee(VehicleType.CAR, SlotType.REGULAR, baseTime, exit15m);
        assertEquals(0.0, fee15.totalFee(), "15 minute free hone chahiye");
        assertEquals(0.0, fee15.parkingFee());
        assertEquals(0, fee15.billableHours());

        // Exactly 30 minute ka stay
        LocalDateTime exit30m = baseTime.plusMinutes(30);
        FeeBreakdown fee30 = calculator.calculateFee(VehicleType.SUV, SlotType.LARGE, baseTime, exit30m);
        assertEquals(0.0, fee30.totalFee(), "30 minute bilkul free hone chahiye");
    }

    @Test
    @DisplayName("2. 30 minute ke baad har partial ghanta round up hona chahiye")
    void testHourlyRounding() {
        // 31 minute -> 1 ghante ka charge (Car: ₹30)
        LocalDateTime exit31m = baseTime.plusMinutes(31);
        FeeBreakdown fee31 = calculator.calculateFee(VehicleType.CAR, SlotType.REGULAR, baseTime, exit31m);
        assertEquals(1, fee31.billableHours());
        assertEquals(30.0, fee31.totalFee());

        // 60 minute -> 1 ghante ka charge (Car: ₹30)
        LocalDateTime exit60m = baseTime.plusMinutes(60);
        FeeBreakdown fee60 = calculator.calculateFee(VehicleType.CAR, SlotType.REGULAR, baseTime, exit60m);
        assertEquals(1, fee60.billableHours());
        assertEquals(30.0, fee60.totalFee());

        // 61 minute -> 2 ghante ka charge (Car: 2 * ₹30 = ₹60)
        LocalDateTime exit61m = baseTime.plusMinutes(61);
        FeeBreakdown fee61 = calculator.calculateFee(VehicleType.CAR, SlotType.REGULAR, baseTime, exit61m);
        assertEquals(2, fee61.billableHours());
        assertEquals(60.0, fee61.totalFee());
    }

    @Test
    @DisplayName("3. 24 ghante ke andar daily cap se zyada fees nahi lagni chahiye")
    void testDailyCaps() {
        LocalDateTime exit10h = baseTime.plusHours(10);

        // Bike: 10 * ₹10 = ₹100, par capped ₹80 par hoga
        FeeBreakdown bikeFee = calculator.calculateFee(VehicleType.BIKE, SlotType.SMALL, baseTime, exit10h);
        assertEquals(80.0, bikeFee.totalFee(), "Bike ka maximum charge ₹80/day hona chahiye");

        // Car: 10 * ₹30 = ₹300, par capped ₹250 par hoga
        FeeBreakdown carFee = calculator.calculateFee(VehicleType.CAR, SlotType.REGULAR, baseTime, exit10h);
        assertEquals(250.0, carFee.totalFee(), "Car ka maximum charge ₹250/day hona chahiye");

        // SUV: 10 * ₹50 = ₹500, par capped ₹400 par hoga
        FeeBreakdown suvFee = calculator.calculateFee(VehicleType.SUV, SlotType.LARGE, baseTime, exit10h);
        assertEquals(400.0, suvFee.totalFee(), "SUV ka maximum charge ₹400/day hona chahiye");

        // EV (regular slot): 10 * ₹40 = ₹400, par capped ₹350 par hoga
        FeeBreakdown evFee = calculator.calculateFee(VehicleType.EV, SlotType.REGULAR, baseTime, exit10h);
        assertEquals(350.0, evFee.totalFee(), "EV parking charge ₹350/day par cap hona chahiye");
    }

    @Test
    @DisplayName("4. EV Charging fee sirf EV_CHARGING slot me hi lagni chahiye")
    void testEvChargingFee() {
        LocalDateTime exit3h = baseTime.plusHours(3);

        // Case A: EV_CHARGING slot me -> Parking (3*40=120) + Charging (60 + 3*15=105) = ₹225
        FeeBreakdown feeWithCharging = calculator.calculateFee(VehicleType.EV, SlotType.EV_CHARGING, baseTime, exit3h);
        assertTrue(feeWithCharging.isChargingApplied());
        assertEquals(120.0, feeWithCharging.parkingFee());
        assertEquals(105.0, feeWithCharging.chargingFee());
        assertEquals(225.0, feeWithCharging.totalFee());

        // Case B: Regular fallback slot me -> Parking (3*40=120) + Charging (₹0) = ₹120
        FeeBreakdown feeWithoutCharging = calculator.calculateFee(VehicleType.EV, SlotType.REGULAR, baseTime, exit3h);
        assertFalse(feeWithoutCharging.isChargingApplied());
        assertEquals(120.0, feeWithoutCharging.parkingFee());
        assertEquals(0.0, feeWithoutCharging.chargingFee());
        assertEquals(120.0, feeWithoutCharging.totalFee());
    }

    @Test
    @DisplayName("5. Multi-day stay (24 ghante se zyada) me daily cap har din par apply hona chahiye")
    void testMultiDayStay() {
        // Car 26 ghante ruki:
        // Day 1 (24 ghante) = ₹250 (Daily Cap)
        // Day 2 (2 ghante)   = 2 * ₹30 = ₹60
        // Total = ₹250 + ₹60 = ₹310
        LocalDateTime exit26h = baseTime.plusHours(26);
        FeeBreakdown car26 = calculator.calculateFee(VehicleType.CAR, SlotType.REGULAR, baseTime, exit26h);
        assertEquals(26, car26.billableHours());
        assertEquals(310.0, car26.totalFee(), "26 ghante ke liye ₹250 + ₹60 = ₹310 hona chahiye");

        // SUV 48 ghante ruki (2 poore din): 2 * ₹400 = ₹800
        LocalDateTime exit48h = baseTime.plusHours(48);
        FeeBreakdown suv48 = calculator.calculateFee(VehicleType.SUV, SlotType.LARGE, baseTime, exit48h);
        assertEquals(800.0, suv48.totalFee(), "48 ghante ke liye 2 * ₹400 = ₹800 hona chahiye");
    }

    @Test
    @DisplayName("6. Agar exit time entry time se pehle ka ho toh exception aana chahiye")
    void testInvalidTimeOrder() {
        LocalDateTime exitBefore = baseTime.minusMinutes(10);
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateFee(VehicleType.CAR, SlotType.REGULAR, baseTime, exitBefore));
    }
}
