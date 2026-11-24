package member1;

import member2.FeeBreakdown;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Ticket — Parking session ka record (receipt jaisa).
 *
 * Jab gaadi ENTER karti hai → Ticket banta hai (ACTIVE)
 * Jab gaadi EXIT karti hai  → Ticket complete hota hai (fee + exit time set)
 */
public class Ticket {

    private final String ticketId;         // unique ID, e.g. "TKT-1001"
    private final Vehicle vehicle;         // kaunsi gaadi
    private final ParkingSlot slot;        // kaunse slot me parked
    private final LocalDateTime entryTime; // kab aayi
    private LocalDateTime exitTime;        // kab gayi (null jab tak andar hai)
    private FeeBreakdown feeBreakdown;     // bill ka breakdown (null jab tak active)
    private boolean active;                // true = gaadi andar hai, false = checkout ho gayi

    public Ticket(String ticketId, Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime) {
        this.ticketId  = Objects.requireNonNull(ticketId, "Ticket ID null nahi ho sakta");
        this.vehicle   = Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        this.slot      = Objects.requireNonNull(slot, "Slot null nahi ho sakta");
        this.entryTime = Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");
        this.exitTime  = null;
        this.feeBreakdown = null;
        this.active = true;
    }

    // Checkout — ticket band karo aur bill lagao
    public void completeTicket(LocalDateTime exitTime, FeeBreakdown feeBreakdown) {
        if (!this.active) {
            throw new IllegalStateException("Ticket " + ticketId + " pehle se close hai.");
        }
        this.exitTime      = Objects.requireNonNull(exitTime, "Exit time null nahi ho sakta");
        this.feeBreakdown  = Objects.requireNonNull(feeBreakdown, "Fee breakdown null nahi ho sakta");
        this.active = false;
    }

    // Gaadi kitni der ruki — entry se exit tak (ya abhi tak agar still active)
    public Duration getDuration() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end);
    }

    // --- Getters ---

    public String getTicketId()           { return ticketId; }
    public Vehicle getVehicle()           { return vehicle; }
    public ParkingSlot getSlot()          { return slot; }
    public LocalDateTime getEntryTime()   { return entryTime; }
    public LocalDateTime getExitTime()    { return exitTime; }
    public FeeBreakdown getFeeBreakdown() { return feeBreakdown; }
    public boolean isActive()             { return active; }

    // Total bill (0 agar abhi checkout nahi hua)
    public double getTotalFee() {
        return feeBreakdown != null ? feeBreakdown.totalFee() : 0.0;
    }

    @Override
    public String toString() {
        return String.format("Ticket #%s | %s | %s | In: %s | %s",
                ticketId, vehicle, slot.getId(), entryTime,
                active ? "ACTIVE" : "COMPLETED");
    }
}
