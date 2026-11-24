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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ParkingGarage — Smart Parking Garage ka main controller.
 *
 * Data Structures:
 *   grid[][]       → 2D array (floors x slots), multi-storey layout
 *   activeTickets  → HashMap<plate, ticket> for O(1) lookup
 *   ticketHistory  → list of completed sessions (audit trail)
 *
 * Logic split:
 *   Slot allocation → Member 2 (SlotAllocator)
 *   Fee calculation → Member 2 (FeeCalculator)
 *   Validation / UI → Member 3
 */
public class ParkingGarage {

    public static final int DEFAULT_FLOORS = 3;
    public static final int DEFAULT_SLOTS_PER_FLOOR = 10;

    // --- Storage ---
    private final ParkingSlot[][] grid;
    private final Map<String, Ticket> activeTickets;
    private final List<Ticket> ticketHistory;

    // --- Helpers (Member 2) ---
    private final SlotAllocator slotAllocator;
    private final FeeCalculator feeCalculator;

    // --- Counters ---
    private final AtomicInteger ticketCounter = new AtomicInteger(1000);
    private double totalRevenue = 0.0;
    private int peakOccupancy = 0;    // ek saath max gaadiyaan (high watermark)

    // ===================== Constructors =====================

    // Default: 3 floors x 10 slots = 30 bays
    public ParkingGarage() {
        this(DEFAULT_FLOORS, DEFAULT_SLOTS_PER_FLOOR);
    }

    // Custom size
    public ParkingGarage(int totalFloors, int slotsPerFloor) {
        if (totalFloors <= 0 || slotsPerFloor <= 0) {
            throw new IllegalArgumentException("Floors aur slots positive hone chahiye.");
        }
        this.grid = new ParkingSlot[totalFloors][slotsPerFloor];
        this.activeTickets = new HashMap<>();
        this.ticketHistory = new ArrayList<>();
        this.slotAllocator = new SlotAllocator();
        this.feeCalculator = new FeeCalculator();
        buildGrid(totalFloors, slotsPerFloor);
    }

    // Grid banao — har slot ko uska type assign karo
    private void buildGrid(int floors, int slotsPerFloor) {
        for (int f = 0; f < floors; f++) {
            for (int s = 0; s < slotsPerFloor; s++) {
                grid[f][s] = new ParkingSlot(f, s, determineSlotType(s));
            }
        }
    }

    /**
     * Slot index se type decide karo:
     *   0-1 → SMALL (bikes)
     *   2-6 → REGULAR (cars)
     *   7   → LARGE (SUVs)
     *   8-9 → EV_CHARGING
     */
    public static SlotType determineSlotType(int slotIndex) {
        if (slotIndex <= 1)                      return SlotType.SMALL;
        if (slotIndex >= 2 && slotIndex <= 6)    return SlotType.REGULAR;
        if (slotIndex == 7)                      return SlotType.LARGE;
        return SlotType.EV_CHARGING;
    }

    // ===================== Park / Exit / Find =====================

    public Ticket parkVehicle(Vehicle vehicle) throws DuplicateVehicleException, GarageFullException {
        return parkVehicle(vehicle, LocalDateTime.now());
    }

    /**
     * Gaadi park karo — 4 simple steps:
     *  1. Check — plate pehle se toh nahi hai?
     *  2. Find  — nearest compatible slot dhoondo (Member 2)
     *  3. Park  — slot me gaadi daalo
     *  4. Ticket — receipt banao, map me store karo
     */
    public synchronized Ticket parkVehicle(Vehicle vehicle, LocalDateTime entryTime)
            throws DuplicateVehicleException, GarageFullException {

        Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");
        String plate = vehicle.getLicensePlate();

        // 1. Duplicate check
        if (activeTickets.containsKey(plate)) {
            Ticket existing = activeTickets.get(plate);
            throw new DuplicateVehicleException(
                    plate + " already slot " + existing.getSlot().getId() + " me parked hai!");
        }

        // 2. Best slot dhoondo
        ParkingSlot bestSlot = slotAllocator.allocateSlot(grid, vehicle);

        // 3. Slot me park karo
        bestSlot.park(vehicle);

        // 4. Ticket banao aur map me daalo
        String ticketId = "TKT-" + ticketCounter.incrementAndGet();
        Ticket ticket = new Ticket(ticketId, vehicle, bestSlot, entryTime);
        activeTickets.put(plate, ticket);

        // Peak update — agar abhi sabse zyada gaadiyaan hain toh record karo
        if (activeTickets.size() > peakOccupancy) {
            peakOccupancy = activeTickets.size();
        }

        return ticket;
    }

    public Ticket exitVehicle(String licensePlate) throws VehicleNotFoundException {
        return exitVehicle(licensePlate, LocalDateTime.now());
    }

    /**
     * Gaadi bahar nikalo — 4 simple steps:
     *  1. Find  — plate se ticket dhoondo
     *  2. Bill  — fee calculate karo (Member 2)
     *  3. Free  — slot khali karo
     *  4. Close — ticket band karo, history me daalo, revenue jodo
     */
    public synchronized Ticket exitVehicle(String licensePlate, LocalDateTime exitTime)
            throws VehicleNotFoundException {

        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate null nahi ho sakti");
        }
        String plate = licensePlate.replaceAll("\\s+", "").toUpperCase();

        // 1. Ticket dhoondo
        Ticket ticket = activeTickets.get(plate);
        if (ticket == null) {
            throw new VehicleNotFoundException("Gaadi '" + plate + "' garage me nahi mili!");
        }

        // 2. Bill calculate karo
        FeeBreakdown fee = feeCalculator.calculateFee(ticket, exitTime);

        // 3. Slot khali karo
        ticket.getSlot().vacate();

        // 4. Ticket close karo + revenue update
        ticket.completeTicket(exitTime, fee);
        activeTickets.remove(plate);
        ticketHistory.add(ticket);
        totalRevenue += fee.totalFee();

        return ticket;
    }

    // Plate se gaadi ka slot dhoondo
    public ParkingSlot findVehicle(String licensePlate) throws VehicleNotFoundException {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate null nahi ho sakti");
        }
        String plate = licensePlate.replaceAll("\\s+", "").toUpperCase();
        Ticket ticket = activeTickets.get(plate);
        if (ticket == null) {
            throw new VehicleNotFoundException("Gaadi '" + plate + "' garage me nahi hai!");
        }
        return ticket.getSlot();
    }

    // Garage completely reset karo (sab clear)
    public synchronized void reset() {
        activeTickets.clear();
        ticketHistory.clear();
        totalRevenue = 0.0;
        peakOccupancy = 0;
        buildGrid(grid.length, grid[0].length);
    }

    // ===================== Basic Getters =====================

    public ParkingSlot[][] getGrid()         { return grid; }
    public int getTotalFloors()              { return grid.length; }
    public int getSlotsPerFloor()            { return grid[0].length; }
    public int getTotalCapacity()            { return grid.length * grid[0].length; }
    public int getActiveVehicleCount()       { return activeTickets.size(); }
    public int getAvailableSlotsCount()      { return getTotalCapacity() - getActiveVehicleCount(); }
    public boolean isFull()                  { return getActiveVehicleCount() >= getTotalCapacity(); }
    public boolean isEmpty()                 { return activeTickets.isEmpty(); }
    public double getTotalRevenue()          { return totalRevenue; }
    public List<Ticket> getHistory()         { return Collections.unmodifiableList(ticketHistory); }
    public Map<String, Ticket> getActiveTickets() { return Collections.unmodifiableMap(activeTickets); }
    public SlotAllocator getSlotAllocator()  { return slotAllocator; }
    public FeeCalculator getFeeCalculator()  { return feeCalculator; }

    // ===================== Analytics =====================

    // Ek floor pe kitni gaadiyaan parked hain?
    public int getOccupiedCountByFloor(int floor) {
        if (floor < 0 || floor >= grid.length) {
            throw new IllegalArgumentException("Invalid floor: " + floor);
        }
        int count = 0;
        for (ParkingSlot slot : grid[floor]) {
            if (slot.isOccupied()) count++;
        }
        return count;
    }

    // Ek vehicle type ki kitni gaadiyaan andar hain? (e.g. kitni bikes parked)
    public int getActiveCountByType(VehicleType type) {
        Objects.requireNonNull(type, "VehicleType null nahi ho sakta");
        int count = 0;
        for (Ticket t : activeTickets.values()) {
            if (t.getVehicle().getType() == type) count++;
        }
        return count;
    }

    // Garage kitna % bhara hai? (0.0 to 100.0)
    public double getOccupancyRate() {
        int total = getTotalCapacity();
        return (total == 0) ? 0.0 : (double) activeTickets.size() / total * 100.0;
    }

    // ===================== Ticket Lookup =====================

    // Ticket ID se dhoondo (e.g. "TKT-1001") — Optional return
    public Optional<Ticket> findTicketById(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) return Optional.empty();
        return activeTickets.values().stream()
                .filter(t -> t.getTicketId().equals(ticketId))
                .findFirst();
    }

    // ===================== Peak Tracker =====================

    // Garage me ek saath max kitni gaadiyaan thi? (lifetime record)
    public int getPeakOccupancy() { return peakOccupancy; }

    // ===================== Summary Snapshot =====================

    // Ek call me garage ki poori summary mil jaaye (dashboard ke liye)
    public GarageSummary getSummarySnapshot() {
        return new GarageSummary(
                getTotalCapacity(),
                getActiveVehicleCount(),
                getAvailableSlotsCount(),
                getOccupancyRate(),
                totalRevenue,
                ticketHistory.size(),
                peakOccupancy
        );
    }
}
