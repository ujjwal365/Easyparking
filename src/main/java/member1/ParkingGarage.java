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

    public static final int DEFAULT_FLOORS = 3;
    public static final int DEFAULT_SLOTS_PER_FLOOR = 10;

    private final ParkingSlot[][] grid;
    private final Map<String, Ticket> activeTickets;
    private final List<Ticket> ticketHistory;

    private final SlotAllocator slotAllocator;
    private final FeeCalculator feeCalculator;

    private final AtomicInteger ticketCounter = new AtomicInteger(1000);
    private double totalRevenue = 0.0;

    public ParkingGarage() {
        this(DEFAULT_FLOORS, DEFAULT_SLOTS_PER_FLOOR);
    }

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

    private void initializeGrid(int totalFloors, int slotsPerFloor) {
        for (int floor = 0; floor < totalFloors; floor++) {
            for (int slot = 0; slot < slotsPerFloor; slot++) {
                SlotType type = determineSlotType(slot);
                grid[floor][slot] = new ParkingSlot(floor, slot, type);
            }
        }
    }

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

    public Ticket parkVehicle(Vehicle vehicle) throws DuplicateVehicleException, GarageFullException {
        return parkVehicle(vehicle, LocalDateTime.now());
    }

    public synchronized Ticket parkVehicle(Vehicle vehicle, LocalDateTime entryTime)
            throws DuplicateVehicleException, GarageFullException {
        Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");

        String plate = vehicle.getLicensePlate();

        // 1. Check duplicate vehicle
        if (activeTickets.containsKey(plate)) {
            Ticket existingTicket = activeTickets.get(plate);
            throw new DuplicateVehicleException(
                    "Duplicate Vehicle! Gaadi " + plate + " pehle se slot " +
                    existingTicket.getSlot().getId() + " me parked hai!");
        }

        // 2. Member 2 ke SlotAllocator se best slot lo
        ParkingSlot bestSlot = slotAllocator.allocateSlot(grid, vehicle);

        // 3. Slot occupy karo
        bestSlot.park(vehicle);

        // 4. Ticket create karke activeTickets map me daalo
        String ticketId = "TKT-" + ticketCounter.incrementAndGet();
        Ticket ticket = new Ticket(ticketId, vehicle, bestSlot, entryTime);
        activeTickets.put(plate, ticket);

        return ticket;
    }

    public Ticket exitVehicle(String licensePlate) throws VehicleNotFoundException {
        return exitVehicle(licensePlate, LocalDateTime.now());
    }

    public synchronized Ticket exitVehicle(String licensePlate, LocalDateTime exitTime) throws VehicleNotFoundException {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate null nahi ho sakti");
        }
        String cleanPlate = licensePlate.trim().toUpperCase();

        Ticket ticket = activeTickets.get(cleanPlate);
        if (ticket == null) {
            throw new VehicleNotFoundException(
                    "Gaadi '" + cleanPlate + "' garage ke andar parked nahi mili!");
        }

        // Member 2 ke FeeCalculator se fees calculate karo
        FeeBreakdown fee = feeCalculator.calculateFee(ticket, exitTime);

        // Slot vacate karo
        ParkingSlot slot = ticket.getSlot();
        slot.vacate();

        ticket.completeTicket(exitTime, fee);
        activeTickets.remove(cleanPlate);
        ticketHistory.add(ticket);

        totalRevenue += fee.totalFee();

        return ticket;
    }

    public synchronized void reset() {
        activeTickets.clear();
        ticketHistory.clear();
        totalRevenue = 0.0;
        initializeGrid(grid.length, grid[0].length);
    }

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

