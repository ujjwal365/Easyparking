package member1;

import member2.FeeBreakdown;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - Ticket Class
 * ============================================================================
 * Parking session ka official audit record:
 * - Entry par: Ticket ACTIVE banta hai (TicketId, Vehicle, Slot, EntryTime)
 * - Exit par : Ticket COMPLETE hota hai (ExitTime, FeeBreakdown, Active=false)
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

    public Ticket(String ticketId, Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime) {
        this.ticketId = Objects.requireNonNull(ticketId, "Ticket ID null nahi ho sakta");
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
        this.slot = Objects.requireNonNull(slot, "Slot null nahi ho sakta");
        this.entryTime = Objects.requireNonNull(entryTime, "Entry time null nahi ho sakta");
        this.exitTime = null;
        this.feeBreakdown = null;
        this.active = true;
    }

    /**
     * Checkout ke waqt ticket ko close karta hai aur calculated bill attach karta hai.
     */
    public void completeTicket(LocalDateTime exitTime, FeeBreakdown feeBreakdown) {
        if (!this.active) {
            throw new IllegalStateException("Ticket " + ticketId + " pehle se close ho chuka hai.");
        }
        this.exitTime = Objects.requireNonNull(exitTime, "Exit time null nahi ho sakta");
        this.feeBreakdown = Objects.requireNonNull(feeBreakdown, "Fee breakdown null nahi ho sakta");
        this.active = false;
    }

    /**
     * Gaadi garage me kitni der rahi (Duration).
     */
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
