package member3;

import member1.ParkingGarage;
import member1.ParkingSlot;
import member1.SlotType;
import member1.Ticket;
import member2.FeeBreakdown;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================================
 * MEMBER 3 (Face) - GaragePrinter UI Visualizer
 * ============================================================================
 * Yeh class console par sundar visuals print karne ka kaam karti hai:
 * 1. Multi-storey 2D Grid drawing (Level 2, Level 1, Level 0 with slot numbers).
 * 2. Visual symbols:
 *    - Khaali Slots : [s] Small, [ ] Regular, [L] Large, [⚡] EV
 *    - Bhare Slots  : [B] Bike,  [C] Car,     [S] SUV,   [E] EV
 * 3. ANSI Colors (Terminal ko colorize karta hai, safe column padding ke sath).
 * 4. Neat Bill/Receipt (Detailed parking aur charging charges).
 * 5. Total revenue aur purane tickets ka audit table.
 * ============================================================================
 */
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
                garage.getActiveVehicleCount(), garage.getAvailableSlotsCount());
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

