# 👤 MEMBER 2: Brain (Core Algorithms & Pricing Logic)

> **Student Role:** Member 2  
> **Domain:** Core Allocation Algorithm, Pricing Engine & EV Billing  
> **Project:** Smart Parking Garage (Multi-Storey Automated Car Parking System)  
> **Language:** Java 17 (Maven)

---

## 📌 Member 2 Ka Kaam Kya Hai? (Role Overview in Hinglish)

Aap is project ke **Brain (Dimag)** hain!
Aapka main responsibility garage ka intelligence likhna tha:
1. **Nearest-Slot Allocation Engine (`SlotAllocator.java`)**: Exit (Floor 0, Slot 0) ke sabse kareeb ka khaali aur compatible slot dhoondhna using distance formula: $(\text{Floor} \times 20) + \text{Slot}$.
2. **EV Fallback Logic**: Agar saare EV charging slots bhar jayein, toh automatically EV ko regular slot me bhejna aur warning notice print karna.
3. **Fee Calculation Engine (`FeeCalculator.java`)**: 30-minute free grace period, `Math.ceil` se hourly rounding, 24-hr daily caps, multi-day stays, aur EV charging flat ₹60 + ₹15/hr calculate karna.
4. **FeeBreakdown Value Object (`FeeBreakdown.java`)**: Base parking, charging aur total fee ka immutable record banana.

---

## 📁 Member 2 Ki Coding Files (Full Source Code)

Aapne is project ke liye yeh **3 core classes** banayi hain. Niche har file ka poora code diya gaya hai:

---

### 1. `FeeBreakdown.java`
**Location:** `src/main/java/member2/FeeBreakdown.java`  
**Description:** Parking fee aur charging fee ka immutable value object.

```java
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
    // Ek line me summary text banata hai
    public String getFormattedSummary() {
        if (isChargingApplied) {
            return String.format("Parking: ₹%.2f + Charging: ₹%.2f = Total: ₹%.2f",
                    parkingFee, chargingFee, totalFee);
        }
        return String.format("Parking: ₹%.2f (No Charging) = Total: ₹%.2f",
                parkingFee, totalFee);
    }
}
```

---

### 2. `SlotAllocator.java`
**Location:** `src/main/java/member2/SlotAllocator.java`  
**Description:** Exit (Floor 0, Slot 0) ke sabse kareeb ka compatible slot dhoondhne wala pure functional algorithm.

```java
package member2;

import member3.GarageFullException;
import member1.ParkingSlot;
import member1.SlotType;
import member1.Vehicle;
import member1.VehicleType;

import java.util.List;

/**
 * ============================================================================
 * MEMBER 2 (Brain) - SlotAllocator Class
 * ============================================================================
 * Yeh class parking slot allocate karne ka mukhya ALGORITHM (dimag) hai.
 * 
 * DESIGN PRINCIPLES:
 * 1. PURE FUNCTION: Yeh sirf grid aur vehicle check karta hai. Grid me koi change nahi karta.
 * 2. NEAREST-SLOT FORMULA:
 *    Distance = (floor * FLOOR_PENALTY) + slotIndex
 *    jaha FLOOR_PENALTY = 20 hai, aur exit point (Floor 0, Slot 0) par hai.
 *    Ramp chadhkar upar jane me mehnat aur time lagta hai, isliye floor penalty 20 rakhi hai.
 * 3. FALLBACK LOGIC:
 *    - EV: Pehle EV_CHARGING slot dhoondhta hai. Agar sab bhare hon toh REGULAR/LARGE slot
 *          me bhejta hai aur notice deta hai: "No EV slot free, assigned regular slot (no charging)".
 *    - Bike: Pehle SMALL slot prefer karti hai.
 *    - Car : REGULAR slot prefer karti hai.
 *    - SUV : Sirf LARGE slot me hi fit hoti hai.
 * 
 * COMPLEXITY:
 * - Time Complexity: O(F * S) -> 3 floors x 10 slots = max 30 checks (super fast).
 * - Viva Alternative: 10,000 slots ke liye PriorityQueue (Min-Heap) use kar sakte hain
 *   jisse O(1) peek aur O(log N) poll time milta hai.
 * ============================================================================
 */
public class SlotAllocator {

    // Har floor chadhne par 20 horizontal slots jitna penalty lagta hai
    public static final int FLOOR_PENALTY = 20;

    // Nearest free and compatible slot dhoondhne ka main method
    public ParkingSlot allocateSlot(ParkingSlot[][] grid, Vehicle vehicle) throws GarageFullException {
        if (grid == null || vehicle == null) {
            throw new IllegalArgumentException("Grid aur Vehicle null nahi ho sakte.");
        }

        VehicleType vType = vehicle.getType();

        // --------------------------------------------------------------------
        // CASE 1: Electric Vehicle (EV)
        // Pehle EV_CHARGING slot dhoondho. Agar na mile toh regular slot me fallback karo.
        // --------------------------------------------------------------------
        if (vType == VehicleType.EV) {
            ParkingSlot evSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.EV_CHARGING));
            if (evSlot != null) {
                return evSlot;
            }

            // Fallback: Saare EV charging slots full hain, isliye regular/large slot denge
            ParkingSlot fallbackSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.REGULAR, SlotType.LARGE));
            if (fallbackSlot != null) {
                System.out.println("[NOTICE] No EV slot free, assigned regular slot (no charging).");
                return fallbackSlot;
            }

            throw new GarageFullException("Garage Full! EV " + vehicle.getLicensePlate() + " ke liye koi slot nahi bacha.");
        }

        // --------------------------------------------------------------------
        // CASE 2: Bike (Pehle SMALL pasand karegi, na mile toh REGULAR/LARGE)
        // --------------------------------------------------------------------
        if (vType == VehicleType.BIKE) {
            ParkingSlot smallSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.SMALL));
            if (smallSlot != null) {
                return smallSlot;
            }
            ParkingSlot fallbackSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.REGULAR, SlotType.LARGE));
            if (fallbackSlot != null) {
                return fallbackSlot;
            }
            throw new GarageFullException("Garage Full! Bike " + vehicle.getLicensePlate() + " ke liye slot nahi mila.");
        }

        // --------------------------------------------------------------------
        // CASE 3: Car (Pehle REGULAR pasand karegi, na mile toh LARGE)
        // --------------------------------------------------------------------
        if (vType == VehicleType.CAR) {
            ParkingSlot regSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.REGULAR));
            if (regSlot != null) {
                return regSlot;
            }
            ParkingSlot largeSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.LARGE));
            if (largeSlot != null) {
                return largeSlot;
            }
            throw new GarageFullException("Garage Full! Car " + vehicle.getLicensePlate() + " ke liye slot nahi mila.");
        }

        // --------------------------------------------------------------------
        // CASE 4: SUV (Sirf aur sirf LARGE slot me jayegi)
        // --------------------------------------------------------------------
        if (vType == VehicleType.SUV) {
            ParkingSlot largeSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.LARGE));
            if (largeSlot != null) {
                return largeSlot;
            }
            throw new GarageFullException("Garage Full! SUV " + vehicle.getLicensePlate() + " ke liye LARGE slot nahi bacha.");
        }

        // Generic fallback check
        ParkingSlot candidate = findBestFreeSlotOfTypes(grid, vType.getCompatibleSlots());
        if (candidate != null) {
            return candidate;
        }

        throw new GarageFullException("Garage Full! Gaadi ke liye slot uplabdh nahi: " + vehicle);
    }

    // Grid ko scan karke sabse kam distance wala khaali slot dhoondhta hai
    private ParkingSlot findBestFreeSlotOfTypes(ParkingSlot[][] grid, List<SlotType> allowedTypes) {
        ParkingSlot bestSlot = null;
        int minDistance = Integer.MAX_VALUE;

        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                ParkingSlot slot = grid[f][s];
                // Slot khaali hona chahiye aur allowedTypes list me aana chahiye
                if (!slot.isOccupied() && allowedTypes.contains(slot.getSlotType())) {
                    int dist = computeDistance(slot.getFloor(), slot.getSlotNumber());
                    if (dist < minDistance) {
                        minDistance = dist;
                        bestSlot = slot;
                    }
                }
            }
        }

        return bestSlot;
    }

    // Distance calculation formula: (Floor * 20) + SlotNumber
    public static int computeDistance(int floor, int slotIndex) {
        return (floor * FLOOR_PENALTY) + slotIndex;
    }
}
```

---

### 3. `FeeCalculator.java`
**Location:** `src/main/java/member2/FeeCalculator.java`  
**Description:** Parking rates, 30m grace period, Math.ceil hourly rounding, 24-hr daily caps, aur EV charging billing engine.

```java
package member2;

import member2.FeeBreakdown;
import member1.SlotType;
import member1.Ticket;
import member1.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * MEMBER 2 (Brain) - FeeCalculator Class
 * ============================================================================
 * Yeh class parking fees calculate karne ka poora business logic hai.
 * 
 * BUSINESS RULES:
 * 1. Grace Period: Pehle 30 minute bilkul FREE hain (<= 30 mins -> ₹0.00).
 * 2. Hourly Rounding: 30 minute ke baad har shuru hue ghante ka poora paisa
 *    lagta hai using Math.ceil (e.g. 31 min -> 1 hr, 65 min -> 2 hrs).
 * 3. Hourly Rates:
 *    - BIKE: ₹10/hr
 *    - CAR : ₹30/hr
 *    - SUV : ₹50/hr
 *    - EV  : ₹40/hr
 * 4. 24-Hour Daily Cap (Multi-day stays par apply hota hai):
 *    - BIKE: ₹80/day
 *    - CAR : ₹250/day
 *    - SUV : ₹400/day
 *    - EV  : ₹350/day (charging chhod kar)
 * 5. EV Charging Fees:
 *    - Flat ₹60 connection fee + ₹15 per billable hour.
 *    - Yeh sirf tab lagti hai jab EV ko EV_CHARGING slot mila ho.
 *    - Agar EV regular slot me park hui hai toh charging fee ₹0 hoti hai!
 * ============================================================================
 */
public class FeeCalculator {

    // Configurable Constants (No Magic Numbers)
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

    // Ticket se fee calculate karne ka shortcut method
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

    // Main fee calculation logic
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

        // --------------------------------------------------------------------
        // RULE 1: Pehle 30 minute bilkul FREE hain
        // --------------------------------------------------------------------
        if (totalMinutes <= FREE_GRACE_PERIOD_MINUTES) {
            return new FeeBreakdown(0.0, 0.0, 0.0, totalMinutes, 0, false);
        }

        // --------------------------------------------------------------------
        // RULE 2: 30 minute se upar hote hi Math.ceil se ghanta round up hota hai
        // Example: 31 mins = 1 hr, 61 mins = 2 hrs
        // --------------------------------------------------------------------
        long billableHours = (long) Math.ceil((double) totalMinutes / 60.0);

        // --------------------------------------------------------------------
        // RULE 3 & 4: Daily Cap aur Hourly Rate calculation (Multi-day stays)
        // --------------------------------------------------------------------
        double hourlyRate = getHourlyRate(vehicleType);
        double dailyCap   = getDailyCap(vehicleType);

        long fullDays = billableHours / 24;
        long remainingHours = billableHours % 24;

        // Bache hue ghanton ka charge daily cap se zyada nahi ho sakta
        double remainingHoursFee = Math.min(remainingHours * hourlyRate, dailyCap);
        double baseParkingFee = (fullDays * dailyCap) + remainingHoursFee;

        // --------------------------------------------------------------------
        // RULE 5: EV Charging Fee sirf tab judti hai jab EV_CHARGING slot mila ho
        // --------------------------------------------------------------------
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
```

---

## ❓ Member 2 Ke Top 5 Viva Questions & Simple Hinglish Answers

### Q1: `Distance = floor * 20 + slotIndex` formula kaise kaam karta hai?
> **Answer:** Sir, garage ka exit (Floor 0, Slot 0) par hai. Har floor par 10 slots hain. Floor penalty 20 rakhi hai taaki Ground Floor ka sabse aakhri slot 9 (dist = 9) bhi Floor 1 ke pehle slot 0 (dist = 20) se chhota rahe. Isse gaadiyan pehle ground floor par park hoti hain aur ramp chadhna avoid hota hai.

### Q2: EV Fallback logic kaise trigger hota hai?
> **Answer:** Allocator pehle EV charging bays check karta hai. Agar teeno floors ke saare 6 EV slots bhare hon, toh wo fallback karke nearest regular/large slot de deta hai, notice print karta hai, aur FeeCalculator me charging fee ₹0 lagata hai.

### Q3: `SlotAllocator` pure function kyu hai?
> **Answer:** Pure function hone ka matlab hai ki yeh method grid ke slots ko modify nahi karta aur koi side-effects nahi generate karta. Yeh sirf best slot dhoondhta hai. Isse unit testing simple aur safe ho jaati hai.

### Q4: Agar garage me 10,000 slots hote toh algorithm ko kaise optimize karte?
> **Answer:** Linear scan ke bajaye hum har slot type ke liye `PriorityQueue<ParkingSlot>` (Min-Heap) use karenge jo distance ke hisaab se ordered hogi. Isse nearest slot nikalna $O(1)$ peek time me ho jayega aur update $O(\log N)$ me hoga.

### Q5: Car 26 ghante ruki toh fees kaise calculate hogi?
> **Answer:** 
> 1. Full Day: `26 / 24 = 1 day` -> Car ka Day 1 daily cap ₹250 par freeze ho jayega.
> 2. Remaining Hours: `26 % 24 = 2 hours` -> 2 * ₹30/hr = ₹60.
> 3. Total = ₹250 + ₹60 = **₹310**.
