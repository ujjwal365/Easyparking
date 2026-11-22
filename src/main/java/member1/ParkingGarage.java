package member1;

import member2.FeeBreakdown;
import member2.FeeCalculator;
import member2.SlotAllocator;
import member3.DuplicateVehicleException;
import member3.GarageFullException;
import member3.VehicleNotFoundException;

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
 * Yeh class poore smart parking garage ka central state coordinator hai.
 *
 * CORE DATA STRUCTURES:
 * 1. 2D Array (grid)        : Multi-storey floors & slots (3 Floors x 10 Slots).
 * 2. HashMap (activeTickets): License plate se active gaadiyon ka O(1) lookup.
 * 3. ArrayList (ticketHistory): Purani completed sessions ka audit record.
 *
 * SEPARATION OF CONCERNS (Clean Architecture):
 * - Slot Allocation logic  -> Member 2 ke SlotAllocator me hai.
 * - Fee Calculation logic  -> Member 2 ke FeeCalculator me hai.
 * - Validation & UI logic  -> Member 3 ke classes me hai.
 * ============================================================================
 */
public class ParkingGarage {

    // Default Building Configuration
    public static final int DEFAULT_FLOORS = 3;
    public static final int DEFAULT_SLOTS_PER_FLOOR = 10;

    // 1. Garage Storage
    private final ParkingSlot[][] grid;
    private final Map<String, Ticket> activeTickets;
    private final List<Ticket> ticketHistory;

    // 2. Member 2 Helpers (Allocations & Pricing)
    private final SlotAllocator slotAllocator;
    private final FeeCalculator feeCalculator;

    // 3. System Counters
    private final AtomicInteger ticketCounter = new AtomicInteger(1000);
    private double totalRevenue = 0.0;

    /**
     * Default constructor: 3 floors x 10 bays = 30 total bays.
     */
    public ParkingGarage() {
        this(DEFAULT_FLOORS, DEFAULT_SLOTS_PER_FLOOR);
    }

    /**
     * Configurable constructor: custom floors aur bays per floor ke liye.
     */
    public ParkingGarage(int totalFloors, int slotsPerFloor) {
        if (totalFloors <= 0 || slotsPerFloor <= 0) {
            throw new IllegalArgumentException("Floors aur slots positive numbers hone chahiye.");
        }

        this.grid = new ParkingSlot[totalFloors][slotsPerFloor];
        this.activeTickets = new HashMap<>();
        this.ticketHistory = new ArrayList<>();
        this.slotAllocator = new SlotAllocator();
        this.feeCalculator = new FeeCalculator();

        initializeGrid(totalFloors, slotsPerFloor);
    }

    /**
     * Floors aur slots ka 2D grid setup karta hai.
     */
    private void initializeGrid(int totalFloors, int slotsPerFloor) {
        for (int floor = 0; floor < totalFloors; floor++) {
            for (int slot = 0; slot < slotsPerFloor; slot++) {
                SlotType type = determineSlotType(slot);
                grid[floor][slot] = new ParkingSlot(floor, slot, type);
            }
        }
    }

    /**
     * Slot index ke hisaab se SlotType decide karta hai:
     * - 0, 1 -> SMALL (Bikes)
     * - 2..6 -> REGULAR (Cars)
     * - 7    -> LARGE (SUVs)
     * - 8, 9 -> EV_CHARGING (Electric Vehicles)
     */
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

    // ========================================================================
    // 🚗 CORE OPERATIONS: PARK, EXIT & FIND (4 STEP LOGIC)
    // ========================================================================

    public Ticket parkVehicle(Vehicle vehicle) throws DuplicateVehicleException, GarageFullException {
        return parkVehicle(vehicle, LocalDateTime.now());
    }

    /**
     * Gaadi park karne ka 4-Step logical flow:
     * Step 1: Duplicate check  -> Kya gaadi pehle se andar hai?
     * Step 2: Slot Allocation  -> Member 2 ke algorithm se nearest slot dhoondo
     * Step 3: Bay Occupancy    -> Slot ko occupied mark karo
     * Step 4: Ticket Creation  -> Active ticket generate karke HashMap me daalo
     */
    public synchronized Ticket parkVehicle(Vehicle vehicle, LocalDateTime entryTime)
            throws DuplicateVehicleException, GarageFullException {
        Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");

        String plate = vehicle.getLicensePlate();

        // Step 1: Duplicate Vehicle Check
        if (activeTickets.containsKey(plate)) {
            Ticket existingTicket = activeTickets.get(plate);
            throw new DuplicateVehicleException(
                    "Duplicate Vehicle! Gaadi " + plate + " pehle se slot " +
                    existingTicket.getSlot().getId() + " me parked hai!");
        }

        // Step 2: Allocate Best Slot (Member 2 Brain)
        ParkingSlot bestSlot = slotAllocator.allocateSlot(grid, vehicle);

        // Step 3: Occupy the Slot
        bestSlot.park(vehicle);

        // Step 4: Issue Ticket & Store in O(1) Active Map
        String ticketId = "TKT-" + ticketCounter.incrementAndGet();
        Ticket ticket = new Ticket(ticketId, vehicle, bestSlot, entryTime);
        activeTickets.put(plate, ticket);

        return ticket;
    }

    public Ticket exitVehicle(String licensePlate) throws VehicleNotFoundException {
        return exitVehicle(licensePlate, LocalDateTime.now());
    }

    /**
     * Gaadi checkout karne ka 4-Step logical flow:
     * Step 1: Ticket Lookup    -> Plate se active ticket dhoondo (O(1))
     * Step 2: Calculate Fee    -> Member 2 ke pricing calculator se bill banao
     * Step 3: Vacate Bay       -> Slot ko free karo taaki nayi gaadi aa sake
     * Step 4: Complete Record  -> Ticket close karo, history me add karo, revenue jodo
     */
    public synchronized Ticket exitVehicle(String licensePlate, LocalDateTime exitTime)
            throws VehicleNotFoundException {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate null nahi ho sakti");
        }
        String cleanPlate = licensePlate.replaceAll("\\s+", "").toUpperCase();

        // Step 1: O(1) Lookup in Active Tickets
        Ticket ticket = activeTickets.get(cleanPlate);
        if (ticket == null) {
            throw new VehicleNotFoundException(
                    "Gaadi '" + cleanPlate + "' garage ke andar parked nahi mili!");
        }

        // Step 2: Calculate Fee Breakdown (Member 2 Pricing Engine)
        FeeBreakdown fee = feeCalculator.calculateFee(ticket, exitTime);

        // Step 3: Vacate the Parking Bay
        ParkingSlot slot = ticket.getSlot();
        slot.vacate();

        // Step 4: Close Ticket & Update Financial Ledger
        ticket.completeTicket(exitTime, fee);
        activeTickets.remove(cleanPlate);
        ticketHistory.add(ticket);
        totalRevenue += fee.totalFee();

        return ticket;
    }

    /**
     * Valet Retrieval: Plate se instant O(1) slot location batata hai.
     */
    public ParkingSlot findVehicle(String licensePlate) throws VehicleNotFoundException {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate null nahi ho sakti");
        }
        String cleanPlate = licensePlate.replaceAll("\\s+", "").toUpperCase();
        Ticket ticket = activeTickets.get(cleanPlate);
        if (ticket == null) {
            throw new VehicleNotFoundException("Gaadi '" + cleanPlate + "' garage me nahi hai!");
        }
        return ticket.getSlot();
    }

    /**
     * Garage ko completely reset / clear karta hai.
     */
    public synchronized void reset() {
        activeTickets.clear();
        ticketHistory.clear();
        totalRevenue = 0.0;
        initializeGrid(grid.length, grid[0].length);
    }

    // ========================================================================
    // 📊 INFORMATIVE GETTERS & METRICS (Viva Questions)
    // ========================================================================

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
