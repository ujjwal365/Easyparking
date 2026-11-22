package member1;

import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - ParkingSlot Class
 * ============================================================================
 * Yeh class garage ke ek single parking space (bay) ko represent karti hai.
 * Har slot ke paas:
 * - floor number hota hai (Level 0, 1, 2)
 * - slot number hota hai (0 se 9 tak)
 * - slot type hota hai (SMALL, REGULAR, LARGE, EV_CHARGING)
 * - currentVehicle hota hai (agar gaadi khadi hai toh uska object, varna null)
 * ============================================================================
 */
public class ParkingSlot {

    private final int floor;
    private final int slotNumber;
    private final SlotType slotType;
    private Vehicle currentVehicle;

    public ParkingSlot(int floor, int slotNumber, SlotType slotType) {
        if (floor < 0 || slotNumber < 0) {
            throw new IllegalArgumentException("Floor aur slot number negative nahi ho sakte.");
        }
        if (slotType == null) {
            throw new IllegalArgumentException("SlotType null nahi ho sakta.");
        }
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.currentVehicle = null;
    }

    public boolean isOccupied() {
        return currentVehicle != null;
    }

    public void park(Vehicle vehicle) {
        if (isOccupied()) {
            throw new IllegalStateException("Slot " + getId() + " pehle se bhara hua hai! Gaadi: " + currentVehicle);
        }
        this.currentVehicle = Objects.requireNonNull(vehicle, "Park karne ke liye vehicle zaroori hai");
    }

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

    public String getId() {
        return String.format("F%d-S%02d", floor, slotNumber);
    }

    public String getDisplaySymbol() {
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
        return String.format("%s (%s) -> %s", getId(), slotType.name(),
                isOccupied() ? "Occupied by " + currentVehicle : "Free");
    }
}

