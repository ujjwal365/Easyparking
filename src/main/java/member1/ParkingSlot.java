package member1;

import java.util.Objects;

/*** ParkingSlot — Garage ke andar ek single parking bay ko represent karta hai.***/
public class ParkingSlot {

    private final int floor;
    private final int slotNumber;
    private final SlotType slotType;
    private Vehicle currentVehicle;
    private boolean underMaintenance = false;

    //har slot  ko uska floor, slot number aur slot type dene ke liye.
    public ParkingSlot(int floor, int slotNumber, SlotType slotType) {
        if (floor < 0 || slotNumber < 0) {
            throw new IllegalArgumentException("Floor and slot number cannot be negative.");
        }
        this.slotType = Objects.requireNonNull(slotType, "SlotType cannot be null.");
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.currentVehicle = null; // Shuru me slot khaali hota hai
    }

    //karta hai ki slot me koi gaadi abhi khadi hai ya nahi (SlotAllocator aur map use karte hain).
    public boolean isOccupied() {
        return currentVehicle != null;
    }

    //Check karta hai ki slot nayi gaadi ke liye ready hai ya nahi.
    public boolean isAvailable() {
        return currentVehicle == null && !underMaintenance;
    }

    // Pata karne ke liye ki yeh slot repair/maintenance me band hai ya chalu hai.
    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    // Admin kisi kharab bay ko maintenance mode ON/OFF kar sake bina grid todhe.
    public void setUnderMaintenance(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }

    //Slot me gaadi park karne ke liye.
    public void park(Vehicle vehicle) {
        if (underMaintenance) {
            throw new IllegalStateException("Slot " + getId() + " is under maintenance and cannot be used for parking!");
        }
        if (isOccupied()) {
            throw new IllegalStateException("Slot " + getId() + " is already occupied!");
        }
        this.currentVehicle = Objects.requireNonNull(vehicle, "Vehicle to park cannot be null.");
    }

    public Vehicle vacate() {
        Vehicle departed = this.currentVehicle;
        this.currentVehicle = null;
        return departed;
    }

    //batata hai ki slot kaunse floor par hai
    public int getFloor() {
        return floor;
    }

    //Floor ke andar slot ki position index deta hai.
    public int getSlotNumber() {
        return slotNumber;
    }

    //Slot ka type deta hai taaki compatibility aur EV charging check ho sake.
    public SlotType getSlotType() {
        return slotType;
    }

    //Agar koi gaadi khadi hai toh uska Vehicle object deta hai.
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    // Floor aur slot ko ek short ID string me badalta hai.
    public String getId() {
        return String.format("F%d-S%02d", floor, slotNumber);
    }

    //2 D console map me dikhane ke liye short symbol deta hai:
    //[X] = Maintenance, [B]/[C]/[S]/[E] = Gaadi khadi hai.
    public String getDisplaySymbol() {
        if (underMaintenance) {
            return "[X]";
        }
        if (isOccupied()) {
            switch (currentVehicle.getType()) {
                case BIKE: return "[B]";
                case CAR:  return "[C]";
                case SUV:  return "[S]";
                case EV:   return "[E]";
            }
        }
        return slotType.getDisplaySymbol();
    }

    // Yeh zaroori hai jab ParkingSlot ko ArrayList.contains() ya HashSet me compare karein.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSlot that = (ParkingSlot) o;
        return floor == that.floor && slotNumber == that.slotNumber;
    }

    //equals() ke saath hashCode() match hona chahiye taaki HashSet/HashMap sahi kaam kare.
    @Override
    public int hashCode() {
        return Objects.hash(floor, slotNumber);
    }

    // Debugging aur console me slot ka status cleanly print karne ke liye.
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
        return getId() + " (" + slotType.name() + ") -> " + status;
    }
}
