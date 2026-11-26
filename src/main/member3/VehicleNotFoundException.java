package member3;

/**
 * ============================================================================
 * MEMBER 3 (Face) - VehicleNotFoundException
 * ============================================================================
 * Yeh exception tab aati hai jab user aisi gaadi ka number daalta hai jo
 * garage me parked hi nahi hai (Vehicle Not Found!).
 * ============================================================================
 */
public class VehicleNotFoundException extends Exception {
    public VehicleNotFoundException(String message) {
        super(message);
    }
}

