package member3;

/**
 * ============================================================================
 * MEMBER 3 (Face) - DuplicateVehicleException
 * ============================================================================
 * Yeh exception tab aati hai jab koi aisi gaadi enter karne lage jiska number
 * pehle se hi garage ke andar maujood hai (Duplicate Entry!).
 * ============================================================================
 */
public class DuplicateVehicleException extends Exception {
    public DuplicateVehicleException(String message) {
        super(message);
    }
}

