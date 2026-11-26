package member3;

import member1.ParkingGarage;
import member1.ParkingSlot;
import member1.Ticket;
import member1.Vehicle;
import member1.VehicleType;

import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * ============================================================================
 * MEMBER 3 (Face) - Main Console Application & Interactive Menu Loop
 * ============================================================================
 * Yeh program ka main entry point hai jo console par interactive menu chalata hai.
 *
 * IMPORTANT DESIGN RULE:
 * Is class me koi bhi business logic nahi hai!
 * Main class sirf user se input leti hai aur ParkingGarage ko call karti hai.
 *
 * MENU OPTIONS:
 * 1. Gaadi park karein (Plate aur vehicle type input leta hai)
 * 2. Gaadi exit karein (Checkout karta hai aur receipt print karta hai)
 * 3. Gaadi dhoondhein (Number plate se floor aur slot batata hai)
 * 4. Garage map dekhein (2D floor map print karta hai)
 * 5. Revenue aur history dekhein (Kul kamai aur purane tickets)
 * 6. Automated demo chalayein (10 gaadiyon ka full demo + EV fallback + receipts)
 * 7. Bahar niklein
 * ============================================================================
 */
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
        System.out.println("├───────────────────────────────────────────────────────────────────────┤");
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

