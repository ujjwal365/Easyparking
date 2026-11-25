package member1;

import java.util.Objects;

/**
 * ParkingSlot — Garage ke andar ek single parking bay ko represent karta hai.
 *
 * Har slot me yeh 5 cheezein hoti hain:
 *   1. floor            -> Kaunse floor par hai (0, 1, 2)
 *   2. slotNumber       -> Floor par bay index (0 se 9)
 *   3. slotType         -> Bay ka size/type (SMALL, REGULAR, LARGE, EV_CHARGING)
 *   4. currentVehicle   -> Abhi kaunsi gaadi khadi hai (agar khaali hai toh null)
 *   5. underMaintenance -> Repair/cleaning ke liye slot temporary band hai ya nahi
 */
public class ParkingSlot {

    private final int floor;
    private final int slotNumber;
    private final SlotType slotType;
    private Vehicle currentVehicle;
    private boolean underMaintenance = false;

    // WHY CONSTRUCTOR: Building banate waqt har bay ko uska floor, slot number aur slot type dene ke liye.
    public ParkingSlot(int floor, int slotNumber, SlotType slotType) {
        if (floor < 0 || slotNumber < 0) {
            throw new IllegalArgumentException("Floor aur slot number negative nahi ho sakte.");
        }
        this.slotType = Objects.requireNonNull(slotType, "SlotType null nahi ho sakta.");
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.currentVehicle = null; // Shuru me slot khaali hota hai
    }

    // WHY THIS FUNCTION: Check karta hai ki slot me koi gaadi abhi khadi hai ya nahi (SlotAllocator aur map use karte hain).
    public boolean isOccupied() {
        return currentVehicle != null;
    }

    // WHY THIS FUNCTION: Check karta hai ki slot nayi gaadi ke liye ready hai ya nahi (khaali hona chahiye + maintenance off).
    public boolean isAvailable() {
        return currentVehicle == null && !underMaintenance;
    }

    // WHY THIS FUNCTION: Pata karne ke liye ki yeh slot repair/maintenance me band hai ya chalu hai.
    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    // WHY THIS FUNCTION: Admin kisi kharab bay ko maintenance mode ON/OFF kar sake bina grid todhe.
    public void setUnderMaintenance(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }

    // WHY THIS FUNCTION: Slot me gaadi park karne ke liye. Safety check bhi karta hai ki slot pehle se bhara ya maintenance me na ho.
    public void park(Vehicle vehicle) {
        if (underMaintenance) {
            throw new IllegalStateException("Slot " + getId() + " maintenance me hai, park nahi kar sakte!");
        }
        if (isOccupied()) {
            throw new IllegalStateException("Slot " + getId() + " already bhara hua hai!");
        }
        this.currentVehicle = Objects.requireNonNull(vehicle, "Park karne ke liye vehicle null nahi ho sakta.");
    }

    // WHY THIS FUNCTION: Checkout par slot ko khaali karne ke liye (currentVehicle ko null set karta hai taaki doosri gaadi aa sake).
    public Vehicle vacate() {
        Vehicle departed = this.currentVehicle;
        this.currentVehicle = null;
        return departed;
    }

    // WHY THIS FUNCTION: Batata hai ki slot kaunse floor par hai (distance formula: floor * 20 + slotNumber me use hota hai).
    public int getFloor() {
        return floor;
    }

    // WHY THIS FUNCTION: Floor ke andar slot ki position index deta hai.
    public int getSlotNumber() {
        return slotNumber;
    }

    // WHY THIS FUNCTION: Slot ka type deta hai taaki compatibility aur EV charging check ho sake.
    public SlotType getSlotType() {
        return slotType;
    }

    // WHY THIS FUNCTION: Agar koi gaadi khadi hai toh uska Vehicle object deta hai (dashboard aur search me use hota hai).
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    // WHY THIS FUNCTION: Floor aur slot ko ek short ID string me badalta hai, jaise "F0-S02" ya "F1-S08".
    public String getId() {
        return String.format("F%d-S%02d", floor, slotNumber);
    }

    // WHY THIS FUNCTION: 2D console map me dikhane ke liye short symbol deta hai:
    //   [X] = Maintenance, [B]/[C]/[S]/[E] = Gaadi khadi hai, [s]/[ ]/[L]/[⚡] = Khaali slot.
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

    // WHY OVERRIDE equals(): Do slots equal tab maane jayenge jab unka floor aur slotNumber dono same hon.
    // Yeh zaroori hai jab ParkingSlot ko ArrayList.contains() ya HashSet me compare karein.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSlot that = (ParkingSlot) o;
        return floor == that.floor && slotNumber == that.slotNumber;
    }

    // WHY OVERRIDE hashCode(): equals() ke saath hashCode() match hona chahiye taaki HashSet/HashMap sahi kaam kare.
    @Override
    public int hashCode() {
        return Objects.hash(floor, slotNumber);
    }

    // WHY OVERRIDE toString(): Debugging aur console me slot ka status cleanly print karne ke liye.
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
