package member1;

import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - ParkingSlot Class
 * ============================================================================
 * Garage ke single parking slot (bay) ka blueprint:
 * - floor        : Kaunse floor par hai (0, 1, 2)
 * - slotNumber   : Floor par kaunsa number hai (0 se 9)
 * - slotType     : Slot ka type (SMALL, REGULAR, LARGE, EV_CHARGING)
 * - currentVehicle: Abhi kaunsi gaadi khadi hai (null if empty)
 * ============================================================================
 */
public class ParkingSlot {

    private final int floor;
    private final int slotNumber;
    private final SlotType slotType;
    private Vehicle currentVehicle;

    // ── Feature 3: Maintenance Mode Flag ──
    // Jab slot maintenance me ho, toh usme gaadi park nahi honi chahiye
    private boolean underMaintenance = false;

    public ParkingSlot(int floor, int slotNumber, SlotType slotType) {
        if (floor < 0 || slotNumber < 0) {
            throw new IllegalArgumentException("Floor aur slot number negative nahi ho sakte.");
        }
        this.slotType = Objects.requireNonNull(slotType, "SlotType null nahi ho sakta.");
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.currentVehicle = null;
    }

    /**
     * Kya slot me abhi koi gaadi khadi hai?
     */
    public boolean isOccupied() {
        return currentVehicle != null;
    }

    /**
     * Kya slot khali hai aur park karne ke liye tayyar hai?
     * Slot tab available hai jab: koi gaadi na ho AND maintenance mode off ho.
     */
    public boolean isAvailable() {
        return currentVehicle == null && !underMaintenance;
    }

    // ── Feature 3: Maintenance Mode Getter/Setter ──
    /**
     * Kya slot maintenance under hai? (e.g., repair, cleaning, broken)
     */
    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    /**
     * Slot ko maintenance mode ON/OFF karne ke liye.
     * ON karne se slot unavailable ho jaayega parking ke liye.
     */
    public void setUnderMaintenance(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }

    /**
     * Slot me gaadi park karta hai. Agar slot pehle se bhara ho toh error deta hai.
     */
    public void park(Vehicle vehicle) {
        if (isOccupied()) {
            throw new IllegalStateException("Slot " + getId() + " pehle se bhara hua hai! Current: " + currentVehicle);
        }
        this.currentVehicle = Objects.requireNonNull(vehicle, "Park karne ke liye vehicle zaroori hai");
    }

    /**
     * Slot ko khali karta hai aur jo gaadi nikli use return karta hai.
     */
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

    /**
     * Slot ID format: "F0-S02", "F1-S07"
     */
    public String getId() {
        return String.format("F%d-S%02d", floor, slotNumber);
    }

    /**
     * 2D ASCII Grid me print karne ke liye display symbol.
     */
    public String getDisplaySymbol() {
        // Maintenance mode me ho toh [X] dikhao
        if (underMaintenance) {
            return "[X]";
        }
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
        String status;
        if (underMaintenance) {
            status = "MAINTENANCE";
        } else if (isOccupied()) {
            status = "Occupied by " + currentVehicle;
        } else {
            status = "Free";
        }
        return String.format("%s (%s) -> %s", getId(), slotType.name(), status);
    }
}
