package member1;

import java.util.Objects;

/**
 * Vehicle — Ek single gaadi ka data model (blueprint).
 *
 * DESIGN DECISION (Immutable Class):
 *   Dono fields (`licensePlate` aur `type`) `final` hain, yaani ek baar gaadi ka object ban gaya
 *   toh uski number plate koi change nahi kar sakta. Isse HashMap aur HashSet me data safe rehta hai.
 */
public class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    // WHY CONSTRUCTOR: Gaadi enter hote waqt plate aur type validate karke clean format me store karne ke liye.
    public Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate khaali nahi ho sakti!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type null nahi ho sakta!");
        }

        // Spaces hata kar CAPITAL letters me save karte hain (e.g. "mh 12 ab 1234" -> "MH12AB1234")
        // Taaki HashMap aur Set me lookup karte waqt case/space mismatch na ho.
        this.licensePlate = licensePlate.replaceAll("\\s+", "").toUpperCase();
        this.type = type;
    }

    // WHY THIS FUNCTION: Gaadi ki normalized number plate read karne ke liye (HashMap key ke roop me use hoti hai).
    public String getLicensePlate() {
        return licensePlate;
    }

    // WHY THIS FUNCTION: Gaadi ka type (BIKE, CAR, SUV, EV) janne ke liye — fee aur slot allocation me use hota hai.
    public VehicleType getType() {
        return type;
    }

    // WHY OVERRIDE equals(): Java ke HashSet aur HashMap yeh check karne ke liye equals() call karte hain
    // ki do Vehicle objects same hain ya nahi. Humne rule rakha: agar licensePlate same hai toh gaadi same hai.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle other = (Vehicle) o;
        return Objects.equals(licensePlate, other.licensePlate);
    }

    // WHY OVERRIDE hashCode(): Jab bhi equals() override karte hain, hashCode() bhi zaroori hota hai!
    // HashMap aur HashSet object ko sahi bucket me rakhne ke liye hashCode() use karte hain.
    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    // WHY OVERRIDE toString(): Console logs, errors aur tickets me gaadi ko readable format me print karne ke liye.
    @Override
    public String toString() {
        return licensePlate + " [" + type.name() + "]";
    }
}
