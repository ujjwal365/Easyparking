package member3;

/**
 * ============================================================================
 * MEMBER 3 (Face) - GarageFullException
 * ============================================================================
 * Yeh exception tab aati hai jab kisi gaadi ke liye garage me koi bhi compatible
 * khaali slot nahi bachta (Garage Housefull ho gaya!).
 * ============================================================================
 */
public class GarageFullException extends Exception {
    public GarageFullException(String message) {
        super(message);
    }
}

