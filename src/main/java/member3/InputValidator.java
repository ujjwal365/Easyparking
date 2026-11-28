package member3;

import member1.VehicleType;

import java.util.regex.Pattern;


public final class InputValidator {

    public static final String INDIAN_PLATE_REGEX = "^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$";
    private static final Pattern PLATE_PATTERN = Pattern.compile(INDIAN_PLATE_REGEX);

    private InputValidator() {
    }

    public static String validateAndNormalizePlate(String rawPlate) throws InvalidInputException {
        if (rawPlate == null || rawPlate.trim().isEmpty()) {
            throw new InvalidInputException("License plate cannot be empty!");
        }

        String cleaned = rawPlate.replaceAll("[\\s\\-]+", "").toUpperCase();

        if (!PLATE_PATTERN.matcher(cleaned).matches()) {
            throw new InvalidInputException(
                    "Invalid Indian Number Plate format: '" + rawPlate + "'!\n" +
                    "Expected format: 2 State letters + 2 RTO digits + 1-2 Series letters + 4 digits.\n" +
                    "Example: MH12AB1234, DL01A5678, KA05MB9999");
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
            throw new InvalidInputException("Please enter a number between " + min + " and " + max + ".");
        }
        try {
            int choice = Integer.parseInt(input.trim());
            if (choice < min || choice > max) {
                throw new InvalidInputException("Option " + choice + " is out of range! Choose between " + min + " and " + max + ".");
            }
            return choice;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("'" + input.trim() + "' is not a valid number. Please enter digits only.");
        }
    }

    public static VehicleType parseVehicleType(int choice) throws InvalidInputException {
        return switch (choice) {
            case 1 -> VehicleType.BIKE;
            case 2 -> VehicleType.CAR;
            case 3 -> VehicleType.SUV;
            case 4 -> VehicleType.EV;
            default -> throw new InvalidInputException("Invalid vehicle choice: " + choice + ". Choose between 1 and 4.");
        };
    }
}

