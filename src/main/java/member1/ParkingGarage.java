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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
ParkingGarage — Poore Smart Parking Garage ka main coordinator (Member 1 - Backbone).

 YAHAN KAUNSE DATA STRUCTURES USE KIYE HAIN AUR KYU (Viva Quick Guide):
 
 *   1. 2D Array (`ParkingSlot[][] grid`):
 *      - WHY: Garage ke floors (rows) aur slots (columns) fixed size ke hote hain (3 floors x 10 bays).
 *        `grid[floor][slot]` se kisi bhi physical bay par instant direct access milta hai.
 *
 *   2. HashMap (`HashMap<String, Ticket> activeTickets`):
 *      - WHY: Key = License Plate (e.g. "MH12AB1234"), Value = Active Ticket.
 *        Gaadi exit karte waqt, locate karte waqt, ya duplicate check karte waqt
 *        poore 30 slots me loop lagane ke bajay instant O(1) speed me ticket mil jaata hai!
 *
 *   3. ArrayList (`ArrayList<Ticket> ticketHistory`):
 *      - WHY: Completed parking tickets ko exact order (chronological history) me store karne ke liye.
 *        ArrayList ka size dynamically badhta hai, aur agar same gaadi din me 2 baar aaye toh
 *        dono baar ka alag ticket record save hota hai (ArrayList duplicates allow karta hai).
 *
 *   4. Set / HashSet (`Set<String> uniqueVisitors`):
 *      - WHY: Garage me ab tak kitni ALAG-ALAG (unique) gaadiyaan aayi hain yeh ginne ke liye.
 *        Set kabhi duplicate store nahi karta — agar ek car 3 baar bhi visit kare, Set me sirf 1 baar rahegi!
 */
public class ParkingGarage {

    public static final int DEFAULT_FLOORS = 3;
    public static final int DEFAULT_SLOTS_PER_FLOOR = 10;
    public static final double WRONG_SLOT_FINE = 850.0;

    // 1. 2D Array -> Garage ka physical layout (Floor x Slot)
    private final ParkingSlot[][] grid;

    // 2. HashMap -> Abhi andar khadi gaadiyon ka O(1) instant record (Plate -> Ticket)
    private final HashMap<String, Ticket> activeTickets;

    // 3. ArrayList -> Checkout ho chuki gaadiyon ka ordered audit history
    private final ArrayList<Ticket> ticketHistory;

    // 4. Set (HashSet) -> Ab tak visit karne wali unique number plates (no duplicates!)
    private final Set<String> uniqueVisitors;

    // Member 2 helpers (Slot allocation algorithm aur Fee billing rules)
    private final SlotAllocator slotAllocator;
    private final FeeCalculator feeCalculator;

    // Simple counters
    private int ticketCounter = 1000;
    private double totalRevenue = 0.0;
    private int peakOccupancy = 0;

    // ===================== Constructors =====================

    // WHY DEFAULT CONSTRUCTOR: Bina size bataye standard 3 floors x 10 slots = 30 bays ka garage banane ke liye.
    public ParkingGarage() {
        this(DEFAULT_FLOORS, DEFAULT_SLOTS_PER_FLOOR);
    }

    // WHY PARAMETERIZED CONSTRUCTOR: Testing ya custom size (e.g. 5 floors x 20 slots) ke garage banane ke liye.
    public ParkingGarage(int totalFloors, int slotsPerFloor) {
        if (totalFloors <= 0 || slotsPerFloor <= 0) {
            throw new IllegalArgumentException("Floors and slots must be positive numbers.");
        }
        this.grid = new ParkingSlot[totalFloors][slotsPerFloor];
        this.activeTickets = new HashMap<>();
        this.ticketHistory = new ArrayList<>();
        this.uniqueVisitors = new HashSet<>();
        this.slotAllocator = new SlotAllocator();
        this.feeCalculator = new FeeCalculator();

        buildGrid(totalFloors, slotsPerFloor);
    }

    // WHY THIS FUNCTION: 2D array ke har cell [floor][slot] me ek naya ParkingSlot object create karne ke liye.
    private void buildGrid(int floors, int slotsPerFloor) {
        for (int f = 0; f < floors; f++) {
            for (int s = 0; s < slotsPerFloor; s++) {
                SlotType type = determineSlotType(s);
                grid[f][s] = new ParkingSlot(f, s, type);
            }
        }
    }

    //Slot number ke hisaab se har bay ka type decide karne ke liye:
    //   Index 0, 1 -> SMALL (Bikes)
    //   Index 2..6 -> REGULAR (Cars)
    //   Index 7    -> LARGE (SUVs)
    //   Index 8, 9 -> EV_CHARGING (Electric Vehicles)
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

    //Plate se spaces hatane aur CAPITAL letters karne ke liye,
    // taaki exitVehicle() aur findVehicle() me same code repeat na ho aur HashMap key hamesha match kare.
    private String normalizePlate(String licensePlate) {
        if (licensePlate == null) {
            throw new IllegalArgumentException("License plate cannot be null.");
        }
        return licensePlate.replaceAll("\\s+", "").toUpperCase();
    }
    //Real parking ke waqt automatic current time (LocalDateTime.now()) use karne ke liye.
    public Ticket parkVehicle(Vehicle vehicle) throws DuplicateVehicleException, GarageFullException {
        return parkVehicle(vehicle, LocalDateTime.now());
    }

    // Gaadi ko garage me park karne ka poora 4-step workflow chalata hai
    // (`synchronized` isliye hai taaki do threads ek saath same slot allocate na kar dein).
    public synchronized Ticket parkVehicle(Vehicle vehicle, LocalDateTime entryTime)
            throws DuplicateVehicleException, GarageFullException {

        Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
        Objects.requireNonNull(entryTime, "Entry time cannot be null.");
        String plate = vehicle.getLicensePlate();

        // Step 1: HashMap se O(1) duplicate check — kya yeh gaadi pehle se andar hai?
        if (activeTickets.containsKey(plate)) {
            Ticket existing = activeTickets.get(plate);
            throw new DuplicateVehicleException(
                    plate + " is already parked in slot " + existing.getSlot().getId() + "!");
        }

        // Step 2: Member 2 ke SlotAllocator se exit gate ke sabse kareeb compatible free slot dhoondhna
        ParkingSlot bestSlot = slotAllocator.allocateSlot(grid, vehicle);

        // Step 3: Us bay me gaadi park karke slot ko occupied mark karna
        bestSlot.park(vehicle);

        // Step 4: Naya Ticket banana, HashMap me save karna, aur Set me unique visitor record karna
        ticketCounter++;
        String ticketId = "TKT-" + ticketCounter;
        Ticket ticket = new Ticket(ticketId, vehicle, bestSlot, entryTime);

        activeTickets.put(plate, ticket);   // HashMap: instant lookup ke liye
        uniqueVisitors.add(plate);          // HashSet: duplicate plates apne aap ignore ho jayengi

        // Step 5: Peak occupancy record update karna (agar abhi tak sabse zyada gaadiyaan andar hain)
        if (activeTickets.size() > peakOccupancy) {
            peakOccupancy = activeTickets.size();
        }

        return ticket;
    }

    //Real exit ke waqt automatic current time use karne ke liye.
    public Ticket exitVehicle(String licensePlate) throws VehicleNotFoundException {
        return exitVehicle(licensePlate, LocalDateTime.now());
    }

    //Gaadi bahar nikalne ka 4-step checkout flow — bill banana, slot khali karna,
    // HashMap se hatana aur ArrayList history me save karna.
    public synchronized Ticket exitVehicle(String licensePlate, LocalDateTime exitTime)
            throws VehicleNotFoundException {

        String plate = normalizePlate(licensePlate);

        // Step 1: HashMap se O(1) speed me active ticket nikalna
        Ticket ticket = activeTickets.get(plate);
        if (ticket == null) {
            throw new VehicleNotFoundException("Vehicle '" + plate + "' not found in garage!");
        }

        // Step 2: Member 2 ke FeeCalculator se parking + EV charging bill nikalna
        FeeBreakdown fee = feeCalculator.calculateFee(ticket, exitTime);

        // Step 3: Physical slot ko khali (vacate) karna taaki agli gaadi park ho sake
        ticket.getSlot().vacate();

        // Step 4: Ticket band karna, HashMap se hatana, ArrayList history me daalna, aur revenue (fee + fine) jodna
        ticket.completeTicket(exitTime, fee);
        activeTickets.remove(plate);
        ticketHistory.add(ticket);
        totalRevenue += ticket.getTotalFee();

        return ticket;
    }

    // WHY THIS FUNCTION: Agar driver assigned slot ke bajay kisi aur slot (jaise Car ko SUV/EV bay) me park kar de
    // toh grid me actual slot update karke ₹150 wrong-slot violation fine lagane ke liye.
    public synchronized Ticket reportWrongSlot(String licensePlate, String actualSlotId)
            throws VehicleNotFoundException {
        String plate = normalizePlate(licensePlate);
        Ticket ticket = activeTickets.get(plate);
        if (ticket == null) {
            throw new VehicleNotFoundException("Vehicle '" + plate + "' not found in garage!");
        }

        ParkingSlot actualSlot = findSlotById(actualSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot '" + actualSlotId + "' not found!"));

        ParkingSlot currentSlot = ticket.getSlot();
        if (actualSlot.getId().equalsIgnoreCase(currentSlot.getId())) {
            throw new IllegalArgumentException("Vehicle is already parked in slot " + actualSlot.getId() + ".");
        }
        if (!actualSlot.isAvailable()) {
            throw new IllegalStateException("Slot " + actualSlot.getId() + " is not available (occupied or under maintenance).");
        }

        currentSlot.vacate();
        actualSlot.park(ticket.getVehicle());

        String reason = String.format("Parked in %s (%s) instead of assigned %s (%s)",
                actualSlot.getId(), actualSlot.getSlotType().getDisplayName(),
                ticket.getAssignedSlot().getId(), ticket.getAssignedSlot().getSlotType().getDisplayName());
        ticket.reassignToWrongSlot(actualSlot, WRONG_SLOT_FINE, reason);
        return ticket;
    }

    // WHY THIS FUNCTION: Slot ID (e.g. "F0-S08") se 2D grid me exact ParkingSlot object dhoondhne ke liye.
    public Optional<ParkingSlot> findSlotById(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return Optional.empty();
        }
        String target = slotId.trim();
        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                if (grid[f][s].getId().equalsIgnoreCase(target)) {
                    return Optional.of(grid[f][s]);
                }
            }
        }
        return Optional.empty();
    }

    //Valet lookup — number plate dalkar turant O(1) time me batata hai gaadi kis slot par hai.
    public ParkingSlot findVehicle(String licensePlate) throws VehicleNotFoundException {
        String plate = normalizePlate(licensePlate);
        Ticket ticket = activeTickets.get(plate);
        if (ticket == null) {
            throw new VehicleNotFoundException("Vehicle '" + plate + "' is not in the garage!");
        }
        return ticket.getSlot();
    }

    // Demo ya fresh start ke liye saare maps, lists, sets aur grid ko reset karna.
    public synchronized void reset() {
        activeTickets.clear();
        ticketHistory.clear();
        uniqueVisitors.clear();
        totalRevenue = 0.0;
        peakOccupancy = 0;
        buildGrid(grid.length, grid[0].length);
    }

    //UI, WebServer aur Tests safely garage ki dimensions, counts aur lists read kar sakein.
    public ParkingSlot[][] getGrid()         { return grid; }
    public int getTotalFloors()              { return grid.length; }
    public int getSlotsPerFloor()            { return grid[0].length; }
    public int getTotalCapacity()            { return grid.length * grid[0].length; }
    public int getActiveVehicleCount()       { return activeTickets.size(); }
    public int getAvailableSlotsCount()      { return getAvailableSlots().size(); }
    public boolean isFull()                  { return getAvailableSlots().isEmpty(); }
    public boolean isEmpty()                 { return activeTickets.isEmpty(); }
    public double getTotalRevenue()          { return totalRevenue; }
    public SlotAllocator getSlotAllocator()  { return slotAllocator; }
    public FeeCalculator getFeeCalculator()  { return feeCalculator; }
    public int getPeakOccupancy()            { return peakOccupancy; }

    // unmodifiableList / unmodifiableMap isliye return karte hain taaki bahar se koi history ya activeTickets delete na kar sake.
    public List<Ticket> getHistory()              { return Collections.unmodifiableList(ticketHistory); }
    public Map<String, Ticket> getActiveTickets() { return Collections.unmodifiableMap(activeTickets); }

    //   Saare available (khaali + maintenance-free) slots ki ordered list dene ke liye.
    //   ArrayList isliye use kiya kyunki khaali slots ki ginti fix nahi hoti, dynamically grow hoti hai!
    public ArrayList<ParkingSlot> getAvailableSlots() {
        ArrayList<ParkingSlot> freeSlots = new ArrayList<>();
        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                if (grid[f][s].isAvailable()) {
                    freeSlots.add(grid[f][s]);
                }
            }
        }
        return freeSlots;
    }

    // WHY THIS FUNCTION & WHY Set (HashSet):
    //   Abhi garage ke andar jitni gaadiyaan khadi hain unki unique license plates deta hai.
    //   Set isliye use kiya kyunki har active gaadi ki plate unique hoti hai (no duplicates).
    public Set<String> getActiveLicensePlates() {
        return new HashSet<>(activeTickets.keySet());
    }

    // WHY THIS FUNCTION & WHY Set:
    //   Ab tak garage me kitni alag-alag (unique) gaadiyaan park ho chuki hain unka set deta hai.
    public Set<String> getUniqueVisitors() {
        return Collections.unmodifiableSet(uniqueVisitors);
    }

    // WHY THIS FUNCTION: Unique visitors ki total ginti batata hai (e.g. 1 car 2 baar aayi toh bhi count 1 hi rahega).
    public int getUniqueVisitorCount() {
        return uniqueVisitors.size();
    }

    // WHY THIS FUNCTION & WHY Set (HashSet):
    //   Jo slots abhi maintenance mode me band hain unke IDs (e.g. "F0-S03") ek Set me return karta hai.
    public Set<String> getMaintenanceSlotIds() {
        Set<String> maintenanceIds = new HashSet<>();
        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                if (grid[f][s].isUnderMaintenance()) {
                    maintenanceIds.add(grid[f][s].getId());
                }
            }
        }
        return maintenanceIds;
    }

    // WHY THIS FUNCTION & WHY HashMap:
    //   Har VehicleType (BIKE, CAR, SUV, EV) ki kitni gaadiyaan abhi andar hain, uska summary HashMap deta hai.
    //   HashMap isliye use kiya kyunki Key = VehicleType aur Value = Count pair banana sabse clean hota hai!
    public HashMap<VehicleType, Integer> getActiveVehicleCountByType() {
        HashMap<VehicleType, Integer> counts = new HashMap<>();
        for (VehicleType type : VehicleType.values()) {
            counts.put(type, 0);
        }
        for (Ticket ticket : activeTickets.values()) {
            VehicleType type = ticket.getVehicle().getType();
            counts.put(type, counts.get(type) + 1);
        }
        return counts;
    }



    // WHY THIS FUNCTION: Floor-wise check karne ke liye ki kisi ek floor par kitni gaadiyaan parked hain.
    public int getOccupiedCountByFloor(int floor) {
        if (floor < 0 || floor >= grid.length) {
            throw new IllegalArgumentException("Invalid floor: " + floor);
        }
        int count = 0;
        for (ParkingSlot slot : grid[floor]) {
            if (slot.isOccupied()) {
                count++;
            }
        }
        return count;
    }

    // WHY THIS FUNCTION: Kisi ek vehicle type (e.g. sirf EV ya sirf BIKE) ki active count nikalne ke liye.
    public int getActiveCountByType(VehicleType type) {
        Objects.requireNonNull(type, "VehicleType cannot be null.");
        int count = 0;
        for (Ticket ticket : activeTickets.values()) {
            if (ticket.getVehicle().getType() == type) {
                count++;
            }
        }
        return count;
    }

    // WHY THIS FUNCTION: Garage kitna % bhara hua hai yeh calculate karta hai (0.0% se 100.0%).
    public double getOccupancyRate() {
        int total = getTotalCapacity();
        if (total == 0) {
            return 0.0;
        }
        return (double) activeTickets.size() / total * 100.0;
    }

    // WHY THIS FUNCTION: Agar user plate ke bajay Ticket ID (e.g. "TKT-1001") se search kare toh ticket dhoondh sake.
    public Optional<Ticket> findTicketById(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            return Optional.empty();
        }
        for (Ticket ticket : activeTickets.values()) {
            if (ticket.getTicketId().equals(ticketId)) {
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }

    // WHY THIS FUNCTION: Dashboard aur analytics ke liye saare key metrics ek hi GarageSummary record me dene ke liye.
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
