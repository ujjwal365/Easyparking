package member1;

import member2.FeeBreakdown;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
public class Ticket {

    private final String ticketId;          
    private final Vehicle vehicle;          
    private final ParkingSlot assignedSlot; 
    private ParkingSlot slot;               
    private final LocalDateTime entryTime;  
    private LocalDateTime exitTime;         
    private FeeBreakdown feeBreakdown;      
    private double wrongSlotFine;           
    private String wrongSlotReason;        

    //Jab gaadi park hoti hai tab naya active ticket create karne ke liye.
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

   
    public void completeTicket(LocalDateTime exitTime, FeeBreakdown feeBreakdown) {
        if (!this.active) {
            throw new IllegalStateException("Ticket " + ticketId + " is already closed.");
        }
        this.exitTime     = Objects.requireNonNull(exitTime, "Exit time cannot be null.");
        this.feeBreakdown = Objects.requireNonNull(feeBreakdown, "Fee breakdown cannot be null.");
        this.active       = false;
    }

    
    public Duration getDuration() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end);
    }

    //Doosre classes safely ticket details padh sakein.
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

    //Safely total fee (parking + EV charging + wrong-slot fine) deta hai.
    public double getTotalFee() {
        if (feeBreakdown == null) {
            return wrongSlotFine;
        }
        return feeBreakdown.totalFee() + wrongSlotFine;
    }

    //History list aur logs me ticket ko ek clean line me print karne ke liye.
    @Override
    public String toString() {
        String status = active ? "ACTIVE" : "COMPLETED";
        return String.format("Ticket #%s | %s | %s | In: %s | %s",
                ticketId, vehicle, slot.getId(), entryTime, status);
    }
}
