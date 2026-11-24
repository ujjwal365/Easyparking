package member1;

import java.util.Objects;

/**
 * Vehicle — Ek gaadi ka simple data model.
 *
 * Fields (dono final = immutable, thread-safe):
 *   licensePlate → number plate (e.g. MH12AB1234), auto uppercase + spaces removed
 *   type         → BIKE / CAR / SUV / EV
 */
public class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        // Plate blank nahi honi chahiye
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate khaali nahi ho sakti!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type null nahi ho sakta!");
        }

        // Normalize — spaces hatao, uppercase karo (taaki lookup me dikkat na ho)
        this.licensePlate = licensePlate.replaceAll("\\s+", "").toUpperCase();
        this.type = type;
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType()    { return type; }

    // Do vehicles equal hain agar unki plate same hai
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle v = (Vehicle) o;
        return Objects.equals(licensePlate, v.licensePlate);
    }

    @Override
    public int hashCode() { return Objects.hash(licensePlate); }

    @Override
    public String toString() { return licensePlate + " [" + type.name() + "]"; }
}
