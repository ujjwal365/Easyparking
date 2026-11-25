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
    private final ParkingSlot slot;         // Kaunse bay me park hui
    private final LocalDateTime entryTime;  // Gaadi kab andar aayi
    private LocalDateTime exitTime;         // Gaadi kab bahar gayi (active hone par null)
    private FeeBreakdown feeBreakdown;      // Bill details (active hone par null)
    private boolean active;                 // true = gaadi andar hai, false = checkout complete

    // WHY CONSTRUCTOR: Jab gaadi park hoti hai tab naya active ticket create karne ke liye.
    public Ticket(String ticketId, Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime) {
        this.ticketId     = Objects.requireNonNull(ticketId, "Ticket ID null nahi ho sakta");
        this.vehicle      = Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        this.slot         = Objects.requireNonNull(slot, "Slot null nahi ho sakta");
        this.entryTime    = Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");
        this.exitTime     = null;
        this.feeBreakdown = null;
        this.active       = true;
    }

    // WHY THIS FUNCTION: Checkout ke waqt ticket ko band karne, exit time aur bill store karne ke liye.
    // Guard check ensures ki ek close ticket dobara close na ho (double billing se bachav).
    public void completeTicket(LocalDateTime exitTime, FeeBreakdown feeBreakdown) {
        if (!this.active) {
            throw new IllegalStateException("Ticket " + ticketId + " pehle se close ho chuka hai.");
        }
        this.exitTime     = Objects.requireNonNull(exitTime, "Exit time null nahi ho sakta");
        this.feeBreakdown = Objects.requireNonNull(feeBreakdown, "Fee breakdown null nahi ho sakta");
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
    public ParkingSlot getSlot()          { return slot; }
    public LocalDateTime getEntryTime()   { return entryTime; }
    public LocalDateTime getExitTime()    { return exitTime; }
    public FeeBreakdown getFeeBreakdown() { return feeBreakdown; }
    public boolean isActive()             { return active; }

    // WHY THIS FUNCTION: Safely total fee deta hai; agar gaadi abhi exit nahi hui toh crash (NullPointer) ke bajay 0.0 deta hai.
    public double getTotalFee() {
        if (feeBreakdown == null) {
            return 0.0;
        }
        return feeBreakdown.totalFee();
    }

    // WHY OVERRIDE toString(): History list aur logs me ticket ko ek clean line me print karne ke liye.
    @Override
    public String toString() {
        String status = active ? "ACTIVE" : "COMPLETED";
        return String.format("Ticket #%s | %s | %s | In: %s | %s",
                ticketId, vehicle, slot.getId(), entryTime, status);
    }
}
