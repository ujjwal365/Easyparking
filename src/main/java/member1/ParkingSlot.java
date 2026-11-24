package member1;

import java.util.Objects;

/**
 * ParkingSlot — Garage ka ek single parking bay.
 *
 * Har slot ke paas yeh info hoti hai:
 *   floor          → kaunse floor pe hai (0, 1, 2)
 *   slotNumber     → us floor pe number (0-9)
 *   slotType       → SMALL / REGULAR / LARGE / EV_CHARGING
 *   currentVehicle → kaunsi gaadi khadi hai (null = khali)
 *   underMaintenance → slot repair me hai toh true (parking band)
 */
public class ParkingSlot {

    private final int floor;
    private final int slotNumber;
    private final SlotType slotType;
    private Vehicle currentVehicle;
    private boolean underMaintenance = false;   // maintenance mode (default OFF)

    public ParkingSlot(int floor, int slotNumber, SlotType slotType) {
        if (floor < 0 || slotNumber < 0) {
            throw new IllegalArgumentException("Floor aur slot number negative nahi ho sakte.");
        }
        this.slotType = Objects.requireNonNull(slotType, "SlotType null nahi ho sakta.");
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.currentVehicle = null;
    }

    // --- Status checks ---

    // Kya slot me gaadi khadi hai?
    public boolean isOccupied() {
        return currentVehicle != null;
    }

    // Kya slot parking ke liye available hai? (khali + maintenance off)
    public boolean isAvailable() {
        return currentVehicle == null && !underMaintenance;
    }

    // --- Maintenance mode ---

    public boolean isUnderMaintenance()                   { return underMaintenance; }
    public void setUnderMaintenance(boolean maintenance)  { this.underMaintenance = maintenance; }

    // --- Park & Vacate ---

    // Gaadi park karo (agar slot already bhara toh error)
    public void park(Vehicle vehicle) {
        if (isOccupied()) {
            throw new IllegalStateException("Slot " + getId() + " already bhara hai!");
        }
        this.currentVehicle = Objects.requireNonNull(vehicle, "Vehicle null nahi ho sakta");
    }

    // Slot khali karo, jo gaadi nikli woh return karo
    public Vehicle vacate() {
        Vehicle departed = this.currentVehicle;
        this.currentVehicle = null;
        return departed;
    }

    // --- Getters ---

    public int getFloor()              { return floor; }
    public int getSlotNumber()         { return slotNumber; }
    public SlotType getSlotType()      { return slotType; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }

    // Slot ID: "F0-S02", "F1-S07"
    public String getId() {
        return String.format("F%d-S%02d", floor, slotNumber);
    }

    // Grid display: [X]=maintenance, [B/C/S/E]=occupied, [s]/[ ]/[L]/[⚡]=free
    public String getDisplaySymbol() {
        if (underMaintenance) return "[X]";
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

    // --- equals / hashCode / toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSlot that = (ParkingSlot) o;
        return floor == that.floor && slotNumber == that.slotNumber;
    }

    @Override
    public int hashCode() { return Objects.hash(floor, slotNumber); }

    @Override
    public String toString() {
        String status = underMaintenance ? "MAINTENANCE"
                      : isOccupied()     ? "Occupied by " + currentVehicle
                      :                    "Free";
        return getId() + " (" + slotType.name() + ") -> " + status;
    }
}
