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
    private final WebServer webServer;

    public Main() {
        this.garage = new ParkingGarage();
        this.scanner = new Scanner(System.in);
        this.webServer = new WebServer(this.garage);
    }

    public static void main(String[] args) {
        Main app = new Main();

        if (args.length > 0) {
            if ("--demo".equalsIgnoreCase(args[0])) {
                app.runScriptedDemo();
                return;
            } else if ("--web".equalsIgnoreCase(args[0])) {
                app.startWebOnly();
                return;
            } else if ("--verify".equalsIgnoreCase(args[0])) {
                SystemVerifier.main(args);
                return;
            }
        }

        app.ensureWebServerStarted();
        app.runMenuLoop();
    }

    private void ensureWebServerStarted() {
        try {
            if (this.webServer.getPort() == 0) {
                this.webServer.start();
            }
        } catch (Exception e) {
            System.err.println("[WEB SERVER WARNING] Could not start dashboard: " + e.getMessage());
        }
    }

    public void startWebOnly() {
        ensureWebServerStarted();
        if (webServer.getPort() == 0) {
            System.err.println("[ERROR] Could not start web dashboard. Please check port availability and permissions.");
            return;
        }
        System.out.println("=".repeat(75));
        System.out.println("          🌐 SMART PARKING GARAGE - WEB DASHBOARD MODE 🌐");
        System.out.println("=".repeat(75));
        System.out.printf("  Live Web Dashboard: %s%n", webServer.getUrl());
        System.out.println("  Opening in your browser automatically...");
        System.out.println("  Press 'Ctrl + C' in the console to stop the server.");
        System.out.println("=".repeat(75));
        webServer.openInBrowser();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }

    public void runMenuLoop() {
        System.out.println("=".repeat(75));
        System.out.println("      WELCOME TO SMART PARKING GARAGE MANAGEMENT SYSTEM");
        System.out.println("                 [Java 17 Academic Project]");
        System.out.println("=".repeat(75));
        if (webServer.getPort() > 0) {
            System.out.printf("  🌐 Live Web Dashboard: %s (Open via Option 7)%n", webServer.getUrl());
            System.out.println("=".repeat(75));
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("👉 Enter your choice (1-8): ");
            String input = scanner.nextLine();

            try {
                int choice = InputValidator.parseMenuChoice(input, 1, 8);
                switch (choice) {
                    case 1 -> handleParkVehicle();
                    case 2 -> handleExitVehicle();
                    case 3 -> handleFindVehicle();
                    case 4 -> handleShowGarage();
                    case 5 -> handleShowRevenue();
                    case 6 -> runScriptedDemo();
                    case 7 -> {
                        System.out.println("\n🌐 Opening Live Web Dashboard in browser: " + webServer.getUrl());
                        webServer.openInBrowser();
                    }
                    case 8 -> {
                        System.out.println("\n👋 Thank you for using Smart Parking Garage! Safe travels!");
                        webServer.stop();
                        running = false;
                    }
                }
            } catch (InvalidInputException e) {
                System.out.println("\n[INPUT ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\n[SYSTEM ERROR] " + e.getMessage());
            }

            if (running) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("┌───────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                     GARAGE MAIN CONTROL MENU                          │");
        System.out.println("├───────────────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. 🚗 Park Vehicle                                                   │");
        System.out.println("│  2. 📤 Exit Vehicle & Generate Bill                                   │");
        System.out.println("│  3. 🔍 Find Vehicle Slot                                              │");
        System.out.println("│  4. 🗺️  Show Garage Map (2D Floor Layout)                             │");
        System.out.println("│  5. 💰 Show Revenue & History                                         │");
        System.out.println("│  6. 🎬 Run Scripted Demo (10 Vehicles)                                │");
        System.out.println("│  7. 🌐 Open Web Dashboard                                             │");
        System.out.println("│  8. ❌ Quit Application                                               │");
        System.out.println("└───────────────────────────────────────────────────────────────────────┘");
    }

    private void handleParkVehicle() {
        System.out.println("\n--- PARK INCOMING VEHICLE ---");
        try {
            System.out.print("Enter Indian License Plate (e.g. MH12AB1234, DL01A5678): ");
            String rawPlate = scanner.nextLine();
            String plate = InputValidator.validateAndNormalizePlate(rawPlate);

            System.out.println("Select Vehicle Type:");
            System.out.println("  1. BIKE (Two-Wheeler - ₹10/hr)");
            System.out.println("  2. CAR  (Sedan/Hatchback - ₹30/hr)");
            System.out.println("  3. SUV  (Large Vehicle - ₹50/hr)");
            System.out.println("  4. EV   (Electric Vehicle - ₹40/hr + Charging)");
            System.out.print("Enter choice (1-4): ");
            int typeChoice = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 4);
            VehicleType vType = InputValidator.parseVehicleType(typeChoice);

            Vehicle vehicle = new Vehicle(plate, vType);
            Ticket ticket = garage.parkVehicle(vehicle);

            System.out.println("\n[SUCCESS] Vehicle parked successfully!");
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
            System.out.print("Enter license plate of exiting vehicle: ");
            String rawPlate = scanner.nextLine();
            String plate = InputValidator.validateAndNormalizePlate(rawPlate);

            Ticket ticket = garage.exitVehicle(plate);
            System.out.println("\n[SUCCESS] Checkout completed!");
            GaragePrinter.printReceipt(ticket);

        } catch (InvalidInputException | VehicleNotFoundException e) {
            System.out.println("\n[CHECKOUT FAILED] " + e.getMessage());
        }
    }

    private void handleFindVehicle() {
        System.out.println("\n--- FIND VEHICLE LOCATION ---");
        try {
            System.out.print("Enter license plate to locate: ");
            String rawPlate = scanner.nextLine();
            String plate = InputValidator.validateAndNormalizePlate(rawPlate);

            ParkingSlot slot = garage.findVehicle(plate);
            System.out.println("\n[FOUND] Vehicle located at:");
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
        System.out.println("Starting live demo with 10 vehicles...\n");

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

        LocalDateTime entryBike = now.minusHours(2);
        LocalDateTime entryCar = now.minusHours(3);
        LocalDateTime entrySuv = now.minusHours(26);
        LocalDateTime entryEv = now.minusHours(4);

        System.out.println("--- STEP 1: PARKING 10 MIXED VEHICLES ---");
        for (int i = 0; i < demoVehicles.length; i++) {
            Vehicle v = demoVehicles[i];
            try {
                LocalDateTime entryTime;
                if (i == 0) {
                    entryTime = entryBike;
                } else if (i == 1) {
                    entryTime = entryCar;
                } else if (i == 2) {
                    entryTime = entrySuv;
                } else if (i == 3) {
                    entryTime = entryEv;
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
            System.out.printf("  ✓ Vehicle %s found at Slot %s (%s, Floor %d)%n",
                    searchPlate, slot.getId(), slot.getSlotType().getDisplayName(), slot.getFloor());
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("\n--- STEP 4: CHECKOUT BIKE (2-HOUR STAY) ---");
        try {
            Ticket exitBike = garage.exitVehicle("MH12BK0001", now);
            GaragePrinter.printReceipt(exitBike);
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("\n--- STEP 5: CHECKOUT CAR (3-HOUR STAY) ---");
        try {
            Ticket exitCar = garage.exitVehicle("DL01CR2002", now);
            GaragePrinter.printReceipt(exitCar);
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("\n--- STEP 6: CHECKOUT SUV (26-HOUR STAY: DAILY CAP DEMO) ---");
        try {
            Ticket exitSuv = garage.exitVehicle("KA05SV3003", now);
            GaragePrinter.printReceipt(exitSuv);
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("\n--- STEP 7: CHECKOUT EV (4-HOUR STAY: EV CHARGING FEE DEMO) ---");
        try {
            Ticket exitEv = garage.exitVehicle("MH01EV1001", now);
            GaragePrinter.printReceipt(exitEv);
        } catch (VehicleNotFoundException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        System.out.println("--- STEP 8: FINAL GARAGE STATE & REVENUE REPORT ---");
        GaragePrinter.printGarage(garage);
        GaragePrinter.printRevenueAndHistory(garage);

        System.out.println("\n✅ SCRIPTED DEMO COMPLETED SUCCESSFULLY!");
        System.out.println("=".repeat(75));
    }
}

