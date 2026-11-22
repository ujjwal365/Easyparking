# 👤 MEMBER 3: Face (UI, Validation, Exceptions, Tests & Demo)

> **Student Role:** Member 3  
> **Domain:** Console UI, Input Sanitization, Custom Exceptions, JUnit 5 Testing & Scripted Demo  
> **Project:** Smart Parking Garage (Multi-Storey Automated Car Parking System)  
> **Language:** Java 17 (Maven)

---

## 📌 Member 3 Ka Kaam Kya Hai? (Role Overview in Hinglish)

Aap is project ke **Face (Chehra)** hain!
Aapka main responsibility garage ka user interface, web dashboard, error handling aur testing design karna tha:
1. **Interactive Console UI (`GaragePrinter.java`)**: 2D building map draw karna jisme floor summary, colorized symbols `[s]`, `[ ]`, `[L]`, `[⚡]`, `[B]`, `[C]`, `[S]`, `[E]` aur professional checkout receipt banti hai.
2. **Interactive Live Web Dashboard (`WebServer.java` & `index.html`)**: Java ke built-in `HttpServer` se zero-dependency web server aur modern single-page dashboard banana jisme live 2D grid, park/exit modals, plate locate animation, aur instant 10-car demo button hain.
3. **Main Application Loop (`Main.java`)**: 1 se 8 tak ka interactive menu, browser auto-launch, aur automated 10-car demo script run karna.
4. **Regex Input Validation (`InputValidator.java`)**: Official Indian number plate regex `^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$` check karna aur safe integer parsing karna taaki app kabhi crash na ho.
5. **Custom Exception Hierarchy**: Domain-specific exceptions create karna (`DuplicateVehicleException`, `VehicleNotFoundException`, `GarageFullException`, `InvalidInputException`).
6. **Quality Assurance (JUnit 5 Tests)**: Saare 20 unit tests likhna aur 100% test coverage ensure karna.

---

## 📁 Member 3 Ki Coding Files (Full Source Code)

Aapne is project ke liye yeh **12 core classes, web assets aur test suites** banayi hain. Niche har file ka poora code diya gaya hai:

---

### 1. `InputValidator.java`
**Location:** `src/main/java/member3/InputValidator.java`  
**Description:** Indian license plate regex aur safe number parsing.

```java
package member3;

import member3.InvalidInputException;
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

    // Indian vehicle registration number format ka regex pattern
    public static final String INDIAN_PLATE_REGEX = "^[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}$";
    private static final Pattern PLATE_PATTERN = Pattern.compile(INDIAN_PLATE_REGEX);

    private InputValidator() {
        // Utility class hai, iska object banana mana hai
    }

    // Number plate ko clean aur validate karta hai
    public static String validateAndNormalizePlate(String rawPlate) throws InvalidInputException {
        if (rawPlate == null || rawPlate.trim().isEmpty()) {
            throw new InvalidInputException("License plate khaali nahi ho sakti!");
        }

        // Spaces aur hyphens hata kar Capital letters banate hain
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

    // Menu ke numbers (1-7) ko safely parse karta hai. Galat string par crash nahi hota
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

    // Vehicle type selection (1: BIKE, 2: CAR, 3: SUV, 4: EV) ko parse karta hai
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
```

---

### 2. Custom Exception Classes
**Location:** `src/main/java/member3/`  
**Description:** 4 domain-specific custom exceptions.

```java
// GarageFullException.java
package member3;

public class GarageFullException extends Exception {
    public GarageFullException(String message) {
        super(message);
    }
}

// VehicleNotFoundException.java
package member3;

public class VehicleNotFoundException extends Exception {
    public VehicleNotFoundException(String message) {
        super(message);
    }
}

// DuplicateVehicleException.java
package member3;

public class DuplicateVehicleException extends Exception {
    public DuplicateVehicleException(String message) {
        super(message);
    }
}

// InvalidInputException.java
package member3;

public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
```

---

### 3. `GaragePrinter.java`
**Location:** `src/main/java/member3/GaragePrinter.java`  
**Description:** 2D live map, colorized symbols, column alignment, aur receipt printer.

```java
package member3;

import member1.ParkingGarage;
import member2.FeeBreakdown;
import member1.ParkingSlot;
import member1.SlotType;
import member1.Ticket;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GaragePrinter {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";
    private static final String RED    = "\u001B[31m";
    private static final String MAGENTA= "\u001B[35m";

    private static boolean useAnsiColors = true;

    public static void setUseAnsiColors(boolean enabled) {
        useAnsiColors = enabled;
    }

    public static void printGarage(ParkingGarage garage) {
        if (garage == null) return;

        ParkingSlot[][] grid = garage.getGrid();
        int floors = grid.length;
        int slotsPerFloor = grid[0].length;

        System.out.println();
        printHeader("SMART PARKING GARAGE - LIVE 2D FLOOR MAP");
        System.out.printf("  Total Floors: %d | Bays per Floor: %d | Total Capacity: %d | Active: %d | Free: %d%n",
                floors, slotsPerFloor, garage.getTotalCapacity(),
                garage.getActiveVehicleCount(), (garage.getTotalCapacity() - garage.getActiveVehicleCount()));
        System.out.println("  Exit Location: [Floor 0, Slot 0] (Ground Level Ramp)");
        System.out.println("=".repeat(75));

        for (int f = floors - 1; f >= 0; f--) {
            int floorOccupied = 0;
            StringBuilder slotNumbers = new StringBuilder();
            StringBuilder slotBoxes   = new StringBuilder();

            slotNumbers.append(String.format("  Floor %d   | Slot: ", f));
            slotBoxes.append(String.format("  [Level %d] | Bay:  ", f));

            for (int s = 0; s < slotsPerFloor; s++) {
                ParkingSlot slot = grid[f][s];
                if (slot.isOccupied()) {
                    floorOccupied++;
                }

                slotNumbers.append(String.format("[%02d] ", s));

                String rawSymbol = slot.getDisplaySymbol();
                String coloredSymbol = colorizeSlotSymbol(slot, rawSymbol);
                slotBoxes.append(coloredSymbol).append("  ");
            }

            double occupancyRate = ((double) floorOccupied / slotsPerFloor) * 100.0;
            System.out.println(slotNumbers);
            System.out.println(slotBoxes);
            System.out.printf("           └─> Floor %d Summary: %d/%d occupied (%.1f%% full)%n",
                    f, floorOccupied, slotsPerFloor, occupancyRate);
            System.out.println("-".repeat(75));
        }

        printLegend();
    }

    public static void printLegend() {
        System.out.println(color(BOLD, "  LEGEND (प्रतीक सूची):"));
        System.out.println("  FREE SLOTS:   [s] Small (Bike)   [ ] Regular (Car)   [L] Large (SUV)   [⚡] EV Charging");
        System.out.println("  PARKED:       [B] Bike (₹10/h)   [C] Car (₹30/h)     [S] SUV (₹50/h)   [E] Electric (₹40/h)");
        System.out.println("  EXIT POINT:   Floor 0, Slot 0 (Distance penalty = floor * 20 + slotIndex)");
        System.out.println("=".repeat(75));
    }

    public static void printReceipt(Ticket ticket) {
        if (ticket == null || ticket.getFeeBreakdown() == null) {
            System.out.println("[ERROR] Receipt print nahi ho sakti: ticket incomplete hai.");
            return;
        }

        FeeBreakdown fee = ticket.getFeeBreakdown();
        Duration duration = ticket.getDuration();
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SMART PARKING GARAGE RECEIPT                       ║");
        System.out.println("║                         PARKING CHECKOUT                              ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Ticket Number   : %-48s ║%n", ticket.getTicketId());
        System.out.printf("║  License Plate   : %-48s ║%n", ticket.getVehicle().getLicensePlate());
        System.out.printf("║  Vehicle Type    : %-48s ║%n", ticket.getVehicle().getType().getDescription());
        System.out.printf("║  Allocated Slot  : %-48s ║%n",
                ticket.getSlot().getId() + " (" + ticket.getSlot().getSlotType().getDisplayName() + ")");
        System.out.println("╟───────────────────────────────────────────────────────────────────────╢");
        System.out.printf("║  Entry Timestamp : %-48s ║%n", ticket.getEntryTime().format(TIME_FMT));
        System.out.printf("║  Exit Timestamp  : %-48s ║%n", ticket.getExitTime().format(TIME_FMT));
        System.out.printf("║  Actual Stay     : %-48s ║%n", String.format("%d hours, %d minutes (%d total mins)",
                hours, minutes, fee.totalMinutes()));
        System.out.printf("║  Billable Hours  : %-48s ║%n", (fee.totalMinutes() <= 30) ? "0 hrs (Free 30m grace)" : fee.billableHours() + " hr(s) [rounded up]");
        System.out.println("╟───────────────────────────────────────────────────────────────────────╢");
        System.out.println("║                          FEE BREAKDOWN                                ║");
        System.out.printf("║  Base Parking Fee: ₹%-47.2f ║%n", fee.parkingFee());

        if (fee.isChargingApplied()) {
            System.out.printf("║  EV Charging Fee : ₹%-47.2f ║%n", fee.chargingFee());
            System.out.printf("║    (Connection: ₹60 flat + ₹15/hr * %d hrs)                            ║%n", fee.billableHours());
        } else if (ticket.getVehicle().getType().name().equals("EV")) {
            System.out.println("║  EV Charging Fee : ₹0.00 (Parked in Regular/Large slot - no charging) ║");
        }

        System.out.println("╟───────────────────────────────────────────────────────────────────────╢");
        System.out.printf("║  TOTAL AMOUNT PAID : %-47s ║%n", color(BOLD + GREEN, String.format("₹%.2f INR", fee.totalFee())));
        System.out.println("║  Status            : PAID & COMPLETED                                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    public static void printRevenueAndHistory(ParkingGarage garage) {
        if (garage == null) return;

        List<Ticket> history = garage.getHistory();
        System.out.println();
        printHeader("FINANCIAL SUMMARY & TICKET HISTORY");
        System.out.printf("  Cumulative Revenue Collected : ₹%.2f INR%n", garage.getTotalRevenue());
        System.out.printf("  Completed Parking Sessions   : %d vehicles%n", history.size());
        System.out.printf("  Active Parked Vehicles       : %d vehicles%n", garage.getActiveVehicleCount());
        System.out.println("=".repeat(75));

        if (history.isEmpty()) {
            System.out.println("  Abhi tak koi checkout session complete nahi hua hai.");
        } else {
            System.out.printf("  %-10s | %-12s | %-6s | %-8s | %-8s | %-10s%n",
                    "Ticket ID", "Plate", "Type", "Slot", "Duration", "Fee Paid");
            System.out.println("  " + "-".repeat(68));
            for (Ticket t : history) {
                Duration d = Duration.between(t.getEntryTime(), t.getExitTime());
                String durStr = String.format("%dh %02dm", d.toHours(), d.toMinutesPart());
                System.out.printf("  %-10s | %-12s | %-6s | %-8s | %-8s | ₹%-9.2f%n",
                        t.getTicketId(),
                        t.getVehicle().getLicensePlate(),
                        t.getVehicle().getType().name(),
                        t.getSlot().getId(),
                        durStr,
                        t.getTotalFee());
            }
        }
        System.out.println("=".repeat(75));
    }

    private static String colorizeSlotSymbol(ParkingSlot slot, String raw) {
        if (!useAnsiColors) return raw;

        if (slot.isOccupied()) {
            return switch (slot.getCurrentVehicle().getType()) {
                case BIKE -> color(CYAN, raw);
                case CAR  -> color(YELLOW, raw);
                case SUV  -> color(MAGENTA, raw);
                case EV   -> color(GREEN, raw);
            };
        } else {
            return switch (slot.getSlotType()) {
                case EV_CHARGING -> color(GREEN, raw);
                case SMALL       -> color(BLUE, raw);
                case LARGE       -> color(MAGENTA, raw);
                case REGULAR     -> raw;
            };
        }
    }

    private static String color(String ansiCode, String text) {
        if (!useAnsiColors) return text;
        return ansiCode + text + RESET;
    }

    private static void printHeader(String title) {
        System.out.println("╔" + "═".repeat(73) + "╗");
        int padding = (73 - title.length()) / 2;
        String line = " ".repeat(padding) + title + " ".repeat(73 - padding - title.length());
        System.out.println("║" + line + "║");
        System.out.println("╚" + "═".repeat(73) + "╝");
    }
}
```

---

### 4. `Main.java`
**Location:** `src/main/java/member3/Main.java`  
**Description:** Console interactive menu loop aur Option 6 automated 10-car demo script.

```java
package member3;

import member1.ParkingGarage;
import member3.DuplicateVehicleException;
import member3.GarageFullException;
import member3.InvalidInputException;
import member3.VehicleNotFoundException;
import member1.ParkingSlot;
import member1.Ticket;
import member1.Vehicle;
import member1.VehicleType;
import member3.InputValidator;

import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {

    private final ParkingGarage garage;
    private final Scanner scanner;

    public Main() {
        this.garage = new ParkingGarage();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Main app = new Main();

        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            app.runScriptedDemo();
            return;
        }

        app.runMenuLoop();
    }

    public void runMenuLoop() {
        System.out.println("=".repeat(75));
        System.out.println("      WELCOME TO SMART PARKING GARAGE MANAGEMENT SYSTEM");
        System.out.println("                 [Java 17 Academic Project]");
        System.out.println("=".repeat(75));

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("👉 Apna choice dalein (1-7): ");
            String input = scanner.nextLine();

            try {
                int choice = InputValidator.parseMenuChoice(input, 1, 7);
                switch (choice) {
                    case 1 -> handleParkVehicle();
                    case 2 -> handleExitVehicle();
                    case 3 -> handleFindVehicle();
                    case 4 -> handleShowGarage();
                    case 5 -> handleShowRevenue();
                    case 6 -> runScriptedDemo();
                    case 7 -> {
                        System.out.println("\n👋 Smart Parking Garage use karne ke liye dhanyawad! Shubh yatra!");
                        running = false;
                    }
                }
            } catch (InvalidInputException e) {
                System.out.println("\n[INPUT ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[SYSTEM ERROR] " + e.getMessage());
            }

            if (running) {
                System.out.println("\nAage badhne ke liye ENTER dabayein...");
                scanner.nextLine();
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("┌───────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                     GARAGE MAIN CONTROL MENU                          │");
        System.out.├───────────────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. 🚗 Park Vehicle            (गाड़ी पार्क करें)                      │");
        System.out.println("│  2. 📤 Exit Vehicle            (गाड़ी निकालें एवं बिल प्राप्त करें)    │");
        System.out.println("│  3. 🔍 Find Vehicle Slot       (गाड़ी की लोकेशन खोजें)                 │");
        System.out.println("│  4. 🗺️  Show Garage Map         (पूरा 2D फ्लोर मैप देखें)               │");
        System.out.println("│  5. 💰 Show Revenue & History  (कुल कमाई एवं इतिहास देखें)             │");
        System.out.println("│  6. 🎬 Run Scripted Demo       (स्वचालित 10-गाड़ियों का डेमो चलाएं)    │");
        System.out.println("│  7. ❌ Quit Application        (बाहर निकलें)                           │");
        System.out.println("└───────────────────────────────────────────────────────────────────────┘");
    }

    private void handleParkVehicle() {
        System.out.println("\n--- PARK INCOMING VEHICLE ---");
        try {
            System.out.print("Indian License Plate daalein (Jaise: MH12AB1234, DL01A5678): ");
            String rawPlate = scanner.nextLine();
            String plate = InputValidator.validateAndNormalizePlate(rawPlate);

            System.out.println("Vehicle Type chunein:");
            System.out.println("  1. BIKE (Two-Wheeler - ₹10/hr)");
            System.out.println("  2. CAR  (Sedan/Hatchback - ₹30/hr)");
            System.out.println("  3. SUV  (Large Vehicle - ₹50/hr)");
            System.out.println("  4. EV   (Electric Vehicle - ₹40/hr + Charging)");
            System.out.print("Choice daalein (1-4): ");
            int typeChoice = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 4);
            VehicleType vType = InputValidator.parseVehicleType(typeChoice);

            Vehicle vehicle = new Vehicle(plate, vType);
            Ticket ticket = garage.parkVehicle(vehicle);

            System.out.println("\n[SUCCESS] Gaadi safaltapoorvak park ho gayi!");
            System.out.printf("  Ticket ID   : %s%n", ticket.getTicketId());
            System.out.printf("  Vehicle     : %s%n", vehicle);
            System.out.printf("  Assigned Bay: %s (%s)%n",
                    ticket.getSlot().getId(), ticket.getSlot().getSlotType().getDisplayName());
            System.out.printf("  Entry Time  : %s%n", ticket.getEntryTime());

        } catch (InvalidInputException | DuplicateVehicleException | GarageFullException e) {
            System.out.println("\n[PARKING FAILED] " + e.getMessage());
        }
    }

    private void handleExitVehicle() {
        System.out.println("\n--- EXIT VEHICLE & SETTLE FEE ---");
        try {
            System.out.print("Nikalne wali gaadi ka number plate daalein: ");
            String rawPlate = scanner.nextLine();
            String plate = InputValidator.validateAndNormalizePlate(rawPlate);

            Ticket ticket = garage.exitVehicle(plate);
            System.out.println("\n[SUCCESS] Checkout complete ho gaya!");
            GaragePrinter.printReceipt(ticket);

        } catch (InvalidInputException | VehicleNotFoundException e) {
            System.out.println("\n[CHECKOUT FAILED] " + e.getMessage());
        }
    }

    private void handleFindVehicle() {
        System.out.println("\n--- FIND VEHICLE LOCATION ---");
        try {
            System.out.print("Khojne ke liye gaadi ka number daalein: ");
            String rawPlate = scanner.nextLine();
            String plate = InputValidator.validateAndNormalizePlate(rawPlate);

            ParkingSlot slot = garage.findVehicle(plate);
            System.out.println("\n[FOUND] Gaadi mil gayi! Yaha khadi hai:");
            System.out.printf("  Slot ID       : %s%n", slot.getId());
            System.out.printf("  Floor Level   : Floor %d%n", slot.getFloor());
            System.out.printf("  Bay Index     : Slot %d%n", slot.getSlotNumber());
            System.out.printf("  Slot Category : %s%n", slot.getSlotType().getDisplayName());

        } catch (InvalidInputException | VehicleNotFoundException e) {
            System.out.println("\n[SEARCH FAILED] " + e.getMessage());
        }
    }

    private void handleShowGarage() {
        GaragePrinter.printGarage(garage);
    }

    private void handleShowRevenue() {
        GaragePrinter.printRevenueAndHistory(garage);
    }

    public void runScriptedDemo() {
        System.out.println("\n" + "=".repeat(75));
        System.out.println("               🚀 RUNNING SCRIPTED AUTOMATED DEMO 🚀");
        System.out.println("=".repeat(75));
        System.out.println("10 gaadiyon ke sath live demo shuru ho raha hai...\n");

        LocalDateTime now = LocalDateTime.now();

        Vehicle[] demoVehicles = new Vehicle[] {
                new Vehicle("MH12BK0001", VehicleType.BIKE),
                new Vehicle("DL01CR2002", VehicleType.CAR),
                new Vehicle("KA05SV3003", VehicleType.SUV),
                new Vehicle("MH01EV1001", VehicleType.EV),
                new Vehicle("MH01EV1002", VehicleType.EV),
                new Vehicle("KA01EV2001", VehicleType.EV),
                new Vehicle("KA01EV2002", VehicleType.EV),
                new Vehicle("DL04EV3001", VehicleType.EV),
                new Vehicle("DL04EV3002", VehicleType.EV),
                new Vehicle("HR26EV4001", VehicleType.EV)
        };

        LocalDateTime entry3HoursAgo = now.minusHours(3);
        LocalDateTime entry26HoursAgo = now.minusHours(26);

        System.out.println("--- STEP 1: PARKING 10 MIXED VEHICLES ---");
        for (int i = 0; i < demoVehicles.length; i++) {
            Vehicle v = demoVehicles[i];
            try {
                LocalDateTime entryTime;
                if (i == 1) {
                    entryTime = entry3HoursAgo;
                } else if (i == 2) {
                    entryTime = entry26HoursAgo;
                } else {
                    entryTime = now.minusMinutes((i + 1) * 15L);
                }

                Ticket ticket = garage.parkVehicle(v, entryTime);
                System.out.printf("  ✓ Parked #%02d: %-12s (%-4s) -> Assigned: %-7s (%s)%n",
                        (i + 1), v.getLicensePlate(), v.getType().name(),
                        ticket.getSlot().getId(), ticket.getSlot().getSlotType().getDisplayName());

            } catch (Exception e) {
                System.out.println("  ✗ Failed to park " + v + ": " + e.getMessage());
            }
        }

        System.out.println("\n--- STEP 2: LIVE GARAGE MAP AFTER PARKING ---");
        GaragePrinter.printGarage(garage);

        System.out.println("\n--- STEP 3: FINDING VEHICLE BY LICENSE PLATE ---");
        String searchPlate = "DL01CR2002";
        try {
            ParkingSlot slot = garage.findVehicle(searchPlate);
            System.out.printf("  ✓ Gaadi %s mil gayi: Slot %s par hai (%s, Floor %d)%n",
                    searchPlate, slot.getId(), slot.getSlotType().getDisplayName(), slot.getFloor());
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("\n--- STEP 4: CHECKOUT VEHICLE 1 (3-HOUR STAY) ---");
        try {
            Ticket exitTicket1 = garage.exitVehicle("DL01CR2002", now);
            GaragePrinter.printReceipt(exitTicket1);
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("--- STEP 5: CHECKOUT VEHICLE 2 (26-HOUR STAY: DAILY CAP DEMO) ---");
        try {
            Ticket exitTicket2 = garage.exitVehicle("KA05SV3003", now);
            GaragePrinter.printReceipt(exitTicket2);
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("--- STEP 6: FINAL GARAGE STATE & REVENUE REPORT ---");
        GaragePrinter.printGarage(garage);
        GaragePrinter.printRevenueAndHistory(garage);

        System.out.println("\n✅ SCRIPTED DEMO COMPLETED SUCCESSFULLY!");
        System.out.println("=".repeat(75));
    }
}
```

---

### 5. All 3 JUnit 5 Test Suites
**Location:** `src/test/java/garage/`  
**Description:** `FeeCalculatorTest.java`, `SlotAllocatorTest.java`, `ParkingGarageTest.java` (20 tests).

*(Testing files are located in `member 3/` directory and `src/test/java/garage/`).*

---

## ❓ Member 3 Ke Top 5 Viva Questions & Simple Hinglish Answers

### Q1: Number plate ke regex pattern ka kya matlab hai?
> **Answer:** Pattern: `^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$`. 2 State letters (MH, DL), 2 RTO digits (12, 01), 1-2 series letters (A, AB), aur 4 unique digits (1234).

### Q2: User agar menu me text daale toh crash hone se kaise bachte hain?
> **Answer:** `InputValidator.parseMenuChoice()` me string ko `try-catch(NumberFormatException)` me parse karte hain. Galat input par `InvalidInputException` throw hoti hai aur program crash nahi hota.

### Q3: `Main.java` me business logic kyu nahi likhi?
> **Answer:** Separation of Concerns aur MVC pattern ke liye. UI ka kaam sirf input lena aur receipt dikhana hai. Saara core logic `ParkingGarage` ke paas hai.

### Q4: Scripted Demo me 7 EVs kyu park kiye?
> **Answer:** Default garage me 6 EV slots hain. 7th EV daal kar humne EV fallback message: `[NOTICE] No EV slot free, assigned regular slot (no charging)` ko live screen par trigger karke prove kiya.

### Q5: Aapne unit tests me kya-kya edge cases cover kiye?
> **Answer:** Exact 30 min free grace, 31st minute ka round up, Car ka ₹250 daily cap, EV fallback slot me charging fee ₹0, 26 ghante ka multi-day cap, aur duplicate plate validation.
