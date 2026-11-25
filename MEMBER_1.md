# 👤 MEMBER 1: Backbone (Data Architecture & State Management)

> **Student Role:** Member 1  
> **Domain:** Data Architecture, Model Classes & Garage State Coordinator  
> **Project:** Smart Parking Garage (Multi-Storey Automated Car Parking System)  

---

##  (Role Overview in Hinglish)
1. **Model Classes**: Garage ke building blocks (Vehicle, Slot, Ticket, Enums).
2. **2D Array Grid (`ParkingSlot[3][10]`)**: 3 floors aur har floor par 10 parking slots ka layout banana.
3. **`HashMap<String, Ticket>`**: Active gaadiyon ko instant $O(1)$ speed me dhoondhna.
4. **`ArrayList<Ticket>`**: Checkout ho chuki purani gaadiyon ka audit record rakhna.
5. **State Coordinator (`ParkingGarage.java`)**: Gaadi park karna, exit karwana aur sabhi operations ko coordinate karna.

---

## 📁 Member 1

### 1. `SlotType.java`
**Location:** `src/main/java/member1/SlotType.java`  
**Description:** Garage ke 4 slot types (SMALL, REGULAR, LARGE, EV_CHARGING) ko define karta hai.

```java
package member1;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - SlotType Enum
 * ============================================================================
 * Yeh enum parking garage ke alag-alag slot types ko define karta hai.
 * Garage me 4 types ke slots hain:
 * 1. SMALL       -> Bike ke liye chhota slot [s]
 * 2. REGULAR     -> Car ke liye standard slot [ ]
 * 3. LARGE       -> SUV ke liye bada slot [L]
 * 4. EV_CHARGING -> Electric Vehicle ke liye charging wala slot [⚡]
 * ============================================================================
 */
public enum SlotType {
    // Chhoti gaadiyon (Bike) ke liye slot
    SMALL("Small Slot (Bikes)", "[s]"),

    // Aam gaadiyon (Car) ke liye normal slot
    REGULAR("Regular Slot (Cars)", "[ ]"),

    // Badi gaadiyon (SUV) ke liye bada slot
    LARGE("Large Slot (SUVs)", "[L]"),

    // Electric gaadiyon ko charge karne wala slot
    EV_CHARGING("EV Charging Slot", "[⚡]");

    private final String displayName;
    private final String displaySymbol;

    // Constructor: Display name aur console me dikhne wala symbol set karta hai
    SlotType(String displayName, String displaySymbol) {
        this.displayName = displayName;
        this.displaySymbol = displaySymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplaySymbol() {
        return displaySymbol;
    }
}
```

---

### 2. `VehicleType.java`
**Location:** `src/main/java/member1/VehicleType.java`  
**Description:** Gaadiyon ke types aur centralized compatibility rules ko define karta hai.

```java
package member1;

import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - VehicleType Enum
 * ============================================================================
 * Yeh enum gaadiyon ke types ko define karta hai aur compatibility rules ko
 * EK HI JAGAH (centralized) rakhta hai taaki project clean rahe.
 * 
 * COMPATIBILITY RULES:
 * - BIKE: SMALL slot pasand karti hai, par zaroorat padne par REGULAR ya LARGE me bhi ja sakti hai.
 * - CAR : REGULAR pasand karti hai, LARGE me bhi fit ho sakti hai.
 * - SUV : Size me badi hoti hai, isliye sirf LARGE slot me hi park hogi.
 * - EV  : Pehle EV_CHARGING dhoondhti hai. Agar full ho toh REGULAR/LARGE fallback me jayegi.
 * ============================================================================
 */
public enum VehicleType {

    // Bike: Preferred SMALL, compatible: SMALL, REGULAR, LARGE
    BIKE("Motorcycle / Bike",
         SlotType.SMALL,
         List.of(SlotType.SMALL, SlotType.REGULAR, SlotType.LARGE)),

    // Car: Preferred REGULAR, compatible: REGULAR, LARGE
    CAR("Sedan / Hatchback Car",
        SlotType.REGULAR,
        List.of(SlotType.REGULAR, SlotType.LARGE)),

    // SUV: Badi gaadi, sirf LARGE slot me aayegi
    SUV("SUV / Large Vehicle",
        SlotType.LARGE,
        List.of(SlotType.LARGE)),

    // Electric Vehicle: Pehle EV_CHARGING, na mile toh Regular/Large fallback
    EV("Electric Vehicle (EV)",
       SlotType.EV_CHARGING,
       List.of(SlotType.EV_CHARGING, SlotType.REGULAR, SlotType.LARGE));

    private final String description;
    private final SlotType preferredSlotType;
    private final List<SlotType> compatibleSlots;

    VehicleType(String description, SlotType preferredSlotType, List<SlotType> compatibleSlots) {
        this.description = description;
        this.preferredSlotType = preferredSlotType;
        this.compatibleSlots = compatibleSlots;
    }

    public String getDescription() {
        return description;
    }

    public SlotType getPreferredSlotType() {
        return preferredSlotType;
    }

    public List<SlotType> getCompatibleSlots() {
        return Collections.unmodifiableList(compatibleSlots);
    }

    // Check karta hai ki kya yeh gaadi diye gaye slot type me fit ho sakti hai
    public boolean isCompatibleWith(SlotType slotType) {
        if (slotType == null) {
            return false;
        }
        return compatibleSlots.contains(slotType);
    }

    // Check karta hai ki kya gaadi preferred slot ke bajaye kisi doosre fallback slot me hai
    public boolean isFallback(SlotType slotType) {
        return slotType != preferredSlotType;
    }
}
```

---

### 3. `Vehicle.java`
**Location:** `src/main/java/member1/Vehicle.java`  
**Description:** Single vehicle ka blueprint jisme License Plate aur VehicleType store hote hain.

```java
package member1;

import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - Vehicle Class
 * ============================================================================
 * Yeh class kisi bhi gaadi ka blueprint hai.
 * Isme 2 cheezein hoti hain:
 * 1. licensePlate: Gaadi ka number plate (uppercase aur bina space ke)
 * 2. type: Gaadi ka type (BIKE, CAR, SUV, EV)
 *
 * Encapsulation: Variables private final hain taaki data safe rahe.
 * ============================================================================
 */
public class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    // Constructor: Number plate aur type validate karke store karta hai
    public Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate khaali nahi ho sakti!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type null nahi ho sakta!");
        }
        // Trim karke Capital letters me convert karte hain taaki match karne me dikkat na aaye
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    // Do gaadiyan same mani jayengi agar unka number plate same ho
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(licensePlate, vehicle.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", licensePlate, type.name());
    }
}
```

---

### 4. `ParkingSlot.java`
**Location:** `src/main/java/member1/ParkingSlot.java`  
**Description:** Single parking bay jisme Floor, Slot Index, SlotType aur current vehicle ka record rehta hai.

```java
package member1;

import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - ParkingSlot Class
 * ============================================================================
 * Yeh class garage ke ek single parking space (bay) ko represent karti hai.
 * Har slot ke paas:
 * - floor number hota hai (Level 0, 1, 2)
 * - slot number hota hai (0 se 9 tak)
 * - slot type hota hai (SMALL, REGULAR, LARGE, EV_CHARGING)
 * - currentVehicle hota hai (agar gaadi khadi hai toh uska object, varna null)
 * ============================================================================
 */
public class ParkingSlot {

    private final int floor;
    private final int slotNumber;
    private final SlotType slotType;
    private Vehicle currentVehicle;

    // Naya slot create karte samay shuru me koi gaadi nahi hoti (currentVehicle = null)
    public ParkingSlot(int floor, int slotNumber, SlotType slotType) {
        if (floor < 0 || slotNumber < 0) {
            throw new IllegalArgumentException("Floor aur slot number negative nahi ho sakte.");
        }
        if (slotType == null) {
            throw new IllegalArgumentException("SlotType null nahi ho sakta.");
        }
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.currentVehicle = null;
    }

    // Check karta hai ki kya slot me gaadi khadi hai ya khaali hai
    public boolean isOccupied() {
        return currentVehicle != null;
    }

    // Slot me gaadi park karta hai. Agar pehle se bhara ho toh error deta hai
    public void park(Vehicle vehicle) {
        if (isOccupied()) {
            throw new IllegalStateException("Slot " + getId() + " pehle se bhara hua hai! Gaadi: " + currentVehicle);
        }
        this.currentVehicle = Objects.requireNonNull(vehicle, "Park karne ke liye vehicle zaroori hai");
    }

    // Gaadi jane par slot ko khaali karta hai aur gayi hui gaadi return karta hai
    public Vehicle vacate() {
        Vehicle departed = this.currentVehicle;
        this.currentVehicle = null;
        return departed;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    // Sundar ID banata hai, jaise F0-S01 ya F2-S08
    public String getId() {
        return String.format("F%d-S%02d", floor, slotNumber);
    }

    // Live map par dikhane ke liye symbol return karta hai:
    // Agar bhara hai toh gaadi ka symbol [B], [C], [S], [E]
    // Agar khaali hai toh slot ka symbol [s], [ ], [L], [⚡]
    public String getDisplaySymbol() {
        if (isOccupied()) {
            return switch (currentVehicle.getType()) {
                case BIKE -> "[B]";
                case CAR  -> "[C]";
                case SUV  -> "[S]";
                case EV   -> "[E]";
            };
        }
        return slotType.getDisplaySymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSlot that = (ParkingSlot) o;
        return floor == that.floor && slotNumber == that.slotNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, slotNumber);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) -> %s", getId(), slotType.name(),
                isOccupied() ? "Occupied by " + currentVehicle : "Free");
    }
}
```

---

### 5. `Ticket.java`
**Location:** `src/main/java/member1/Ticket.java`  
**Description:** Active aur completed parking session ka data container.

```java
package member1;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - Ticket Class
 * ============================================================================
 * Yeh class parking ticket ko represent karti hai.
 * Gaadi aate samay ticket ACTIVE banta hai jisme entryTime aur slot number hota hai.
 * Gaadi jaate samay ticket COMPLETE hota hai jisme exitTime aur final fees hoti hai.
 * ============================================================================
 */
public class Ticket {

    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private FeeBreakdown feeBreakdown;
    private boolean active;

    // Naya ticket banta hai jab gaadi garage me aati hai
    public Ticket(String ticketId, Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime) {
        this.ticketId = Objects.requireNonNull(ticketId, "Ticket ID null nahi ho sakta");
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        this.slot = Objects.requireNonNull(slot, "Slot null nahi ho sakta");
        this.entryTime = Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");
        this.exitTime = null;
        this.feeBreakdown = null;
        this.active = true; // Shuru me ticket active rehta hai
    }

    // Checkout ke samay ticket close hota hai aur fee breakdown save hota hai
    public void completeTicket(LocalDateTime exitTime, FeeBreakdown feeBreakdown) {
        if (!this.active) {
            throw new IllegalStateException("Ticket " + ticketId + " pehle se close ho chuka hai.");
        }
        this.exitTime = Objects.requireNonNull(exitTime, "Exit time null nahi ho sakta");
        this.feeBreakdown = Objects.requireNonNull(feeBreakdown, "Fee breakdown null nahi ho sakta");
        this.active = false; // Ticket inactive (completed) ho gaya
    }

    // Gaadi kitni der ruki uska Duration nikalta hai
    public Duration getDuration() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end);
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public FeeBreakdown getFeeBreakdown() {
        return feeBreakdown;
    }

    public double getTotalFee() {
        return feeBreakdown != null ? feeBreakdown.totalFee() : 0.0;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return String.format("Ticket #%s | %s | %s | In: %s | Status: %s",
                ticketId, vehicle, slot.getId(), entryTime, active ? "ACTIVE" : "COMPLETED");
    }
}
```

---

### 6. `ParkingGarage.java`
**Location:** `src/main/java/member1/ParkingGarage.java`  
**Description:** Poore system ka central controller jisme 2D grid, activeTickets HashMap, aur history ArrayList rehti hai.

```java
package member1;

import member3.DuplicateVehicleException;
import member3.GarageFullException;
import member3.VehicleNotFoundException;
import member2.FeeBreakdown;
import member1.ParkingSlot;
import member1.SlotType;
import member1.Ticket;
import member1.Vehicle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - ParkingGarage Core Class
 * ============================================================================
 * Yeh class poore smart parking garage ka mukhya coordinator aur data store hai.
 * 
 * CORE DATA STRUCTURES:
 * 1. 2D Array (grid): Multi-storey floors aur slots ko store karta hai (Default: 3 floors x 10 slots).
 * 2. HashMap (activeTickets): License plate ke zariye active gaadiyon ko turant O(1) me dhoondta hai.
 * 3. ArrayList (ticketHistory): Purani saari gaadiyon ke tickets ka record audit ke liye rakhta hai.
 * 
 * SEPARATION OF CONCERNS:
 * - Khali slot dhoondhne ka kaam Member 2 ke SlotAllocator ko diya hai.
 * - Fees hisaab karne ka kaam Member 2 ke FeeCalculator ko diya hai.
 * ============================================================================
 */
public class ParkingGarage {

    // Default configuration: 3 floors aur har floor par 10 slots
    public static final int DEFAULT_FLOORS = 3;
    public static final int DEFAULT_SLOTS_PER_FLOOR = 10;

    // 2D Array: [Floor][Slot]
    private final ParkingSlot[][] grid;

    // HashMap: LicensePlate -> Active Ticket (O(1) fast lookup)
    private final Map<String, Ticket> activeTickets;

    // ArrayList: Checkout ho chuki gaadiyon ka audit record
    private final List<Ticket> ticketHistory;

    // Helper algorithms (pure logic)
    private final SlotAllocator slotAllocator;
    private final FeeCalculator feeCalculator;

    // Har ticket ko unique number dene ke liye counter (TKT-1001, TKT-1002...)
    private final AtomicInteger ticketCounter = new AtomicInteger(1000);

    // Garage ki ab tak ki kul kamai (Total revenue)
    private double totalRevenue = 0.0;

    // Default constructor: 3x10 grid banata hai
    public ParkingGarage() {
        this(DEFAULT_FLOORS, DEFAULT_SLOTS_PER_FLOOR);
    }

    // Custom constructor: Custom floor aur slots ke sath grid banata hai
    public ParkingGarage(int totalFloors, int slotsPerFloor) {
        if (totalFloors <= 0 || slotsPerFloor <= 0) {
            throw new IllegalArgumentException("Floors aur slots per floor positive numbers hone chahiye.");
        }

        this.grid = new ParkingSlot[totalFloors][slotsPerFloor];
        this.activeTickets = new HashMap<>();
        this.ticketHistory = new ArrayList<>();
        this.slotAllocator = new SlotAllocator();
        this.feeCalculator = new FeeCalculator();

        initializeGrid(totalFloors, slotsPerFloor);
    }

    // 2D Array ko initialize karta hai:
    // Slots 0-1: SMALL, Slots 2-6: REGULAR, Slot 7: LARGE, Slots 8-9: EV_CHARGING
    private void initializeGrid(int totalFloors, int slotsPerFloor) {
        for (int floor = 0; floor < totalFloors; floor++) {
            for (int slot = 0; slot < slotsPerFloor; slot++) {
                SlotType type = determineSlotType(slot);
                grid[floor][slot] = new ParkingSlot(floor, slot, type);
            }
        }
    }

    // Slot number ke hisaab se slot type tay karta hai
    public static SlotType determineSlotType(int slotIndex) {
        if (slotIndex == 0 || slotIndex == 1) {
            return SlotType.SMALL;
        } else if (slotIndex >= 2 && slotIndex <= 6) {
            return SlotType.REGULAR;
        } else if (slotIndex == 7) {
            return SlotType.LARGE;
        } else {
            return SlotType.EV_CHARGING;
        }
    }

    // Gaadi ko current time par park karta hai
    public Ticket parkVehicle(Vehicle vehicle) throws DuplicateVehicleException, GarageFullException {
        return parkVehicle(vehicle, LocalDateTime.now());
    }

    // Overload: Demo testing me purana time pass karne ke liye kaam aata hai
    public Ticket parkVehicle(Vehicle vehicle, LocalDateTime entryTime)
            throws DuplicateVehicleException, GarageFullException {
        Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");

        String plate = vehicle.getLicensePlate();

        // 1. Check karo duplicate gaadi toh nahi hai
        if (activeTickets.containsKey(plate)) {
            Ticket existingTicket = activeTickets.get(plate);
            throw new DuplicateVehicleException(
                    "Duplicate Vehicle! Gaadi " + plate + " pehle se slot " +
                    existingTicket.getSlot().getId() + " me parked hai!");
        }

        // 2. SlotAllocator se sabse kareeb ka free slot dhoondho (pure algorithm)
        ParkingSlot bestSlot = slotAllocator.allocateSlot(grid, vehicle);

        // 3. Slot ko occupied mark karo
        bestSlot.park(vehicle);

        // 4. Ticket create karo aur activeTickets map me daalo
        String ticketId = "TKT-" + ticketCounter.incrementAndGet();
        Ticket ticket = new Ticket(ticketId, vehicle, bestSlot, entryTime);
        activeTickets.put(plate, ticket);

        return ticket;
    }

    // Current time par exit karwata hai
    public Ticket exitVehicle(String licensePlate) throws VehicleNotFoundException {
        return exitVehicle(licensePlate, LocalDateTime.now());
    }

    // Gaadi ko exit karwata hai aur bill banata hai
    public Ticket exitVehicle(String licensePlate, LocalDateTime exitTime) throws VehicleNotFoundException {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate null nahi ho sakti");
        }
        String cleanPlate = licensePlate.trim().toUpperCase();

        // 1. HashMap me active ticket dhoondho
        Ticket ticket = activeTickets.get(cleanPlate);
        if (ticket == null) {
            throw new VehicleNotFoundException(
                    "Gaadi '" + cleanPlate + "' garage ke andar parked nahi mili!");
        }

        // 2. FeeCalculator se fees calculate karo
        FeeBreakdown fee = feeCalculator.calculateFee(ticket, exitTime);

        // 3. Physical parking slot ko khaali (vacate) karo
        ParkingSlot slot = ticket.getSlot();
        slot.vacate();

        // 4. Ticket ko complete karo, active map se hatao, aur history list me daalo
        ticket.completeTicket(exitTime, fee);
        activeTickets.remove(cleanPlate);
        ticketHistory.add(ticket);

        // 5. Total revenue me paisa jodo
        totalRevenue += fee.totalFee();

        return ticket;
    }

    // Number plate se gaadi ka slot dhoondhta hai
    public ParkingSlot findVehicle(String licensePlate) throws VehicleNotFoundException {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate null nahi ho sakti");
        }
        String cleanPlate = licensePlate.trim().toUpperCase();
        Ticket ticket = activeTickets.get(cleanPlate);
        if (ticket == null) {
            throw new VehicleNotFoundException("Gaadi '" + cleanPlate + "' garage me nahi hai!");
        }
        return ticket.getSlot();
    }

    public ParkingSlot[][] getGrid() {
        return grid;
    }

    public int getTotalFloors() {
        return grid.length;
    }

    public int getSlotsPerFloor() {
        return grid[0].length;
    }

    public int getTotalCapacity() {
        return grid.length * grid[0].length;
    }

    public int getActiveVehicleCount() {
        return activeTickets.size();
    }

    public int getAvailableSlotsCount() {
        return getTotalCapacity() - getActiveVehicleCount();
    }

    public boolean isFull() {
        return getActiveVehicleCount() >= getTotalCapacity();
    }

    public boolean isEmpty() {
        return activeTickets.isEmpty();
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public List<Ticket> getHistory() {
        return Collections.unmodifiableList(ticketHistory);
    }

    public Map<String, Ticket> getActiveTickets() {
        return Collections.unmodifiableMap(activeTickets);
    }

    public SlotAllocator getSlotAllocator() {
        return slotAllocator;
    }

    public FeeCalculator getFeeCalculator() {
        return feeCalculator;
    }
}
```


