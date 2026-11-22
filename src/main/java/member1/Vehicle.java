package member1;

import java.util.Objects;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - Vehicle Class
 * ============================================================================
 * Yeh class kisi bhi gaadi ka blueprint hai.
 * Isme 2 cheezein hoti hain:
 * 1. licensePlate: Gaadi ka number plate (uppercase aur bina space ke)
 * 2. type: Gaadi ka type (BIKE, CAR, SUV, EV)
 *
 * Encapsulation: Variables private final hain taaki data safe rahe.
 * ============================================================================
 */
public class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    // Constructor: Number plate aur type validate karke store karta hai
    public Vehicle(String licensePlate, VehicleType type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate khaali nahi ho sakti!");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type null nahi ho sakta!");
        }
        // Trim karke Capital letters me convert karte hain taaki match karne me dikkat na aaye
        this.licensePlate = licensePlate.trim().toUpperCase();
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

