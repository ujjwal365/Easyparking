package member3;

/**
 * ============================================================================
 * MEMBER 3 (Face) - InvalidInputException
 * ============================================================================
 * Yeh exception tab aati hai jab user console par galat input daalta hai
 * (jaise galat number plate format ya galat menu number).
 * ============================================================================
 */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

