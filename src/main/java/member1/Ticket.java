package member1;

import member2.FeeBreakdown;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Ticket — Ek parking session ka official record (entry slip + exit receipt).
 *
 * LIFECYCLE:
 *   1. Entry par: Ticket banta hai with active = true (exitTime aur feeBreakdown abhi null hote hain).
 *   2. Exit par : completeTicket() call hota hai -> exitTime, feeBreakdown save hota hai aur active = false ho jaata hai.
 */
public class Ticket {

    private final String ticketId;          // Unique ID, jaise "TKT-1001"
    private final Vehicle vehicle;          // Kaunsi gaadi park hui
    private final ParkingSlot assignedSlot; // System ne kaunsa bay assign kiya tha
    private ParkingSlot slot;               // Gaadi actually kis bay me khadi hai
    private final LocalDateTime entryTime;  // Gaadi kab andar aayi
    private LocalDateTime exitTime;         // Gaadi kab bahar gayi (active hone par null)
    private FeeBreakdown feeBreakdown;      // Bill details (active hone par null)
    private double wrongSlotFine;           // Galat slot me park karne ka fine (default 0.0)
    private String wrongSlotReason;         // Fine lagne ka reason (agar laga ho)
    private boolean active;                 // true = gaadi andar hai, false = checkout complete

    // WHY CONSTRUCTOR: Jab gaadi park hoti hai tab naya active ticket create karne ke liye.
    public Ticket(String ticketId, Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime) {
        this.ticketId        = Objects.requireNonNull(ticketId, "Ticket ID cannot be null.");
        this.vehicle         = Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
        this.assignedSlot    = Objects.requireNonNull(slot, "Slot cannot be null.");
        this.slot            = this.assignedSlot;
        this.entryTime       = Objects.requireNonNull(entryTime, "Entry time cannot be null.");
        this.exitTime        = null;
        this.feeBreakdown    = null;
        this.wrongSlotFine   = 0.0;
        this.wrongSlotReason = null;
        this.active          = true;
    }

    // WHY THIS FUNCTION: Agar driver assigned slot ke bajay kisi aur slot me gaadi park kar de toh actual slot update karke fine lagane ke liye.
    public void reassignToWrongSlot(ParkingSlot actualSlot, double fineAmount, String reason) {
        if (!this.active) {
            throw new IllegalStateException("Cannot apply wrong-slot fine to a closed ticket.");
        }
        if (fineAmount < 0) {
            throw new IllegalArgumentException("Fine amount cannot be negative.");
        }
        this.slot            = Objects.requireNonNull(actualSlot, "Actual slot cannot be null.");
        this.wrongSlotFine   = fineAmount;
        this.wrongSlotReason = reason;
    }

    // WHY THIS FUNCTION: Checkout ke waqt ticket ko band karne, exit time aur bill store karne ke liye.
    // Guard check ensures ki ek close ticket dobara close na ho (double billing se bachav).
    public void completeTicket(LocalDateTime exitTime, FeeBreakdown feeBreakdown) {
        if (!this.active) {
            throw new IllegalStateException("Ticket " + ticketId + " is already closed.");
        }
        this.exitTime     = Objects.requireNonNull(exitTime, "Exit time cannot be null.");
        this.feeBreakdown = Objects.requireNonNull(feeBreakdown, "Fee breakdown cannot be null.");
        this.active       = false;
    }

    // WHY THIS FUNCTION: Gaadi garage me total kitni der rahi yeh nikalne ke liye (receipt aur history table me dikhate hain).
    public Duration getDuration() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end);
    }

    // WHY THESE GETTERS: Doosre classes (FeeCalculator, Receipt Printer, WebServer) safely ticket details padh sakein.
    public String getTicketId()           { return ticketId; }
    public Vehicle getVehicle()           { return vehicle; }
    public ParkingSlot getAssignedSlot()  { return assignedSlot; }
    public ParkingSlot getSlot()          { return slot; }
    public LocalDateTime getEntryTime()   { return entryTime; }
    public LocalDateTime getExitTime()    { return exitTime; }
    public FeeBreakdown getFeeBreakdown() { return feeBreakdown; }
    public double getWrongSlotFine()      { return wrongSlotFine; }
    public String getWrongSlotReason()    { return wrongSlotReason; }
    public boolean hasWrongSlotFine()     { return wrongSlotFine > 0.0; }
    public boolean isActive()             { return active; }

    // WHY THIS FUNCTION: Safely total fee (parking + EV charging + wrong-slot fine) deta hai.
    public double getTotalFee() {
        if (feeBreakdown == null) {
            return wrongSlotFine;
        }
        return feeBreakdown.totalFee() + wrongSlotFine;
    }

    // WHY OVERRIDE toString(): History list aur logs me ticket ko ek clean line me print karne ke liye.
    @Override
    public String toString() {
        String status = active ? "ACTIVE" : "COMPLETED";
        return String.format("Ticket #%s | %s | %s | In: %s | %s",
                ticketId, vehicle, slot.getId(), entryTime, status);
    }
}
