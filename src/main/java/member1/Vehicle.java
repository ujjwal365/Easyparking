package member1;

import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - Vehicle Class
 * ============================================================================
 * Har incoming gaadi ka data model (POJO):
 * 1. licensePlate : Indian number plate (e.g. MH12AB1234)
 * 2. type         : VehicleType (BIKE, CAR, SUV, EV)
 *
 * Immutable Design: Fields final hain taaki data change na ho sake (Thread-Safe).
 * ============================================================================
 */
public class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate khaali nahi ho sakti!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type null nahi ho sakta!");
        }

        // Spaces remove karke uppercase normalize karte hain taaki lookup me breakdown na ho
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
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(licensePlate, vehicle.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", licensePlate, type.name());
    }
}
