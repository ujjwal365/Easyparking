package member3;

import member1.VehicleType;

import java.util.regex.Pattern;

/**
 * ============================================================================
 * MEMBER 3 (Face) - InputValidator Utility Class
 * ============================================================================
 * Yeh class user ke inputs ko validate aur clean karti hai taaki program
 * galat input par CRASH NA HO.
 * 
 * 1. Indian License Plate Regex: ^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$
 *    (Jaise: MH12AB1234, DL01A5678, KA05MB9999)
 * 2. Plate Normalizer: Space aur hyphens hatakar letters CAPITAL karta hai.
 * 3. Safe Integer Parser: Text input ko number me convert karta hai bina crash huye.
 * ============================================================================
 */
public final class InputValidator {

    public static final String INDIAN_PLATE_REGEX = "^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$";
    private static final Pattern PLATE_PATTERN = Pattern.compile(INDIAN_PLATE_REGEX);

    private InputValidator() {
    }

    public static String validateAndNormalizePlate(String rawPlate) throws InvalidInputException {
        if (rawPlate == null || rawPlate.trim().isEmpty()) {
            throw new InvalidInputException("License plate khaali nahi ho sakti!");
        }

        String cleaned = rawPlate.replaceAll("[\\s\\-]+", "").toUpperCase();

        if (!PLATE_PATTERN.matcher(cleaned).matches()) {
            throw new InvalidInputException(
                    "Galat Indian Number Plate format: '" + rawPlate + "'!\n" +
                    "Sahi format: 2 State letters + 2 RTO digits + 1-2 Series letters + 4 digits.\n" +
                    "Udaharan: MH12AB1234, DL01A5678, KA05MB9999");
        }

        return cleaned;
    }

    public static boolean isValidPlate(String plate) {
        if (plate == null) return false;
        String cleaned = plate.replaceAll("[\\s\\-]+", "").toUpperCase();
        return PLATE_PATTERN.matcher(cleaned).matches();
    }

    public static int parseMenuChoice(String input, int min, int max) throws InvalidInputException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidInputException("Kripya " + min + " se " + max + " ke beech koi number daalein.");
        }
        try {
            int choice = Integer.parseInt(input.trim());
            if (choice < min || choice > max) {
                throw new InvalidInputException("Option " + choice + " range se bahar hai! " + min + " se " + max + " ke beech chunein.");
            }
            return choice;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("'" + input.trim() + "' valid number nahi hai. Kripya sirf digits daalein.");
        }
    }

    public static VehicleType parseVehicleType(int choice) throws InvalidInputException {
        return switch (choice) {
            case 1 -> VehicleType.BIKE;
            case 2 -> VehicleType.CAR;
            case 3 -> VehicleType.SUV;
            case 4 -> VehicleType.EV;
            default -> throw new InvalidInputException("Galat vehicle choice: " + choice + ". 1 se 4 ke beech chunein.");
        };
    }
}

