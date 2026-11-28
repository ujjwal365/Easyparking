package member1;

import java.util.Objects;
public class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    //Gaadi enter hote waqt plate aur type validate karke clean format me store karne ke liye.
    public Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be empty!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null!");
        }
        this.licensePlate = licensePlate.replaceAll("\\s+", "").toUpperCase();
        this.type = type;
    }
    public String getLicensePlate() {
        return licensePlate;
    }
    public VehicleType getType() {
        return type;
    }
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

    //Console logs, errors aur tickets me gaadi ko readable format me print karne ke liye.
    @Override
    public String toString() {
        return licensePlate + " [" + type.name() + "]";
    }
}
