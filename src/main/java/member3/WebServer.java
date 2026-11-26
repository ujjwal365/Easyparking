package member3;

import member1.ParkingGarage;
import member1.ParkingSlot;
import member1.SlotType;
import member1.Ticket;
import member1.Vehicle;
import member1.VehicleType;
import member2.FeeBreakdown;
import member2.SlotAllocator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * MEMBER 3 (Face) - WebServer (Built-in Java HTTP Server & REST API)
 * ============================================================================
 * Yeh class Java ke built-in 'com.sun.net.httpserver.HttpServer' ka use karke
 * bina kisi external library ya Spring Boot ke ek lightweight web server chalati hai.
 *
 * REST API ENDPOINTS:
 * 1. GET  /                -> Serves the Single-Page Web Dashboard (HTML/CSS/JS)
 * 2. GET  /api/garage      -> Returns full 2D grid, floors, active count, capacity, revenue
 * 3. POST /api/park        -> Body: {"plate": "...", "type": "..."} -> Parks vehicle
 * 4. POST /api/exit        -> Body: {"plate": "..."} -> Exits vehicle & returns bill receipt
 * 5. GET  /api/find?plate= -> Returns slot location of a vehicle
 * 6. POST /api/demo        -> Runs the 10-car scripted simulation
 * 7. POST /api/reset       -> Clears all slots and resets the garage
 * 8. GET  /api/history     -> Returns completed ticket history
 * ============================================================================
 */
public class WebServer {

    public static final int DEFAULT_PORT = 8080;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private final ParkingGarage garage;
    private HttpServer server;
    private int boundPort;

    public WebServer(ParkingGarage garage) {
        this.garage = garage;
    }

    /**
     * Web server start karta hai. Port 8080 se shuru karta hai,
     * agar busy ho toh 8081..8090 tak try karta hai taaki conflict na ho.
     */
    public synchronized int start() throws IOException {
        int port = DEFAULT_PORT;
        IOException lastError = null;

        for (int p = port; p <= port + 10; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                boundPort = p;
                break;
            } catch (IOException e) {
                lastError = e;
            }
        }

        if (server == null) {
            throw new IOException("Koyi free port nahi mila (8080-8090 sab occupied hain): " + lastError.getMessage(), lastError);
        }

        // Setup Handlers
        server.createContext("/", new StaticPageHandler());
        server.createContext("/api/garage", new GarageStateApi());
        server.createContext("/api/park", new ParkVehicleApi());
        server.createContext("/api/exit", new ExitVehicleApi());
        server.createContext("/api/find", new FindVehicleApi());
        server.createContext("/api/demo", new DemoApi());
        server.createContext("/api/reset", new ResetApi());
        server.createContext("/api/history", new HistoryApi());

        // Multi-threaded executor
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        // Seed initial data so dashboard immediately displays Bike, Car, SUV, EV checkouts & live bays
        seedDefaultData();

        return boundPort;
    }

    /**
     * Seeds initial garage state with completed checkouts for Bike, Car, SUV, EV
     * and active parked vehicles so the dashboard is immediately populated on startup/refresh.
     */
    public synchronized void seedDefaultData() {
        garage.reset();
        LocalDateTime now = LocalDateTime.now();

        Vehicle[] demoVehicles = new Vehicle[]{
                new Vehicle("MH12BK0001", VehicleType.BIKE), // Will checkout (2h stay)
                new Vehicle("DL01CR2002", VehicleType.CAR),  // Will checkout (3h stay)
                new Vehicle("KA05SV3003", VehicleType.SUV),  // Will checkout (26h stay: Daily cap)
                new Vehicle("MH01EV1001", VehicleType.EV),   // Will checkout (4h stay: EV Charging fee)
                new Vehicle("MH01EV1002", VehicleType.EV),
                new Vehicle("KA01EV2001", VehicleType.EV),
                new Vehicle("KA01EV2002", VehicleType.EV),
                new Vehicle("DL04EV3001", VehicleType.EV),
                new Vehicle("DL04EV3002", VehicleType.EV),
                new Vehicle("HR26EV4001", VehicleType.EV),   // EV Fallback to regular slot!
                new Vehicle("MH14BK2020", VehicleType.BIKE), // Stays active
                new Vehicle("DL08CR3030", VehicleType.CAR),  // Stays active
                new Vehicle("KA02SV4040", VehicleType.SUV)   // Stays active
        };

        for (int i = 0; i < demoVehicles.length; i++) {
            Vehicle v = demoVehicles[i];
            LocalDateTime entryTime;
            if (i == 0) entryTime = now.minusHours(2);
            else if (i == 1) entryTime = now.minusHours(3);
            else if (i == 2) entryTime = now.minusHours(26);
            else if (i == 3) entryTime = now.minusHours(4);
            else entryTime = now.minusMinutes((i + 1) * 15L);

            try {
                garage.parkVehicle(v, entryTime);
            } catch (Exception ignored) {
            }
        }

        // Checkouts for ALL 4 VEHICLE TYPES (Bike, Car, SUV, EV)
        try { garage.exitVehicle("MH12BK0001", now); } catch (Exception ignored) {}
        try { garage.exitVehicle("DL01CR2002", now); } catch (Exception ignored) {}
        try { garage.exitVehicle("KA05SV3003", now); } catch (Exception ignored) {}
        try { garage.exitVehicle("MH01EV1001", now); } catch (Exception ignored) {}
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public int getPort() {
        return boundPort;
    }

    public String getUrl() {
        return "http://localhost:" + boundPort;
    }

    /**
     * Browser automatically open karne ka helper method.
     */
    public void openInBrowser() {
        try {
            String url = getUrl();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Windows fallback
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            }
        } catch (Exception e) {
            System.out.println("[NOTE] Browser open nahi ho paya: " + e.getMessage() + ". Kripya manually kholein: " + getUrl());
        }
    }

    // ========================================================================
    // HTTP HANDLERS (Clean and Minimal REST API)
    // ========================================================================

    /**
     * Serves 'index.html' single-page dashboard.
     */
    private class StaticPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange, 204, "");
                return;
            }

            byte[] htmlBytes = loadIndexHtml();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.getResponseHeaders().set("Pragma", "no-cache");
            sendResponse(exchange, 200, htmlBytes);
        }

        private byte[] loadIndexHtml() {
            try {
                Path diskPath = Path.of("src/main/resources/web/index.html");
                if (Files.exists(diskPath)) {
                    return Files.readAllBytes(diskPath);
                }
                InputStream in = WebServer.class.getResourceAsStream("/web/index.html");
                if (in != null) {
                    try (in) {
                        return in.readAllBytes();
                    }
                }
            } catch (Exception ignored) {
            }
            return "<html><body><h2>Smart Parking Garage Dashboard loading...</h2></body></html>".getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * GET /api/garage -> returns complete 2D layout and counts in JSON.
     */
    private class GarageStateApi implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            ParkingSlot[][] grid = garage.getGrid();
            int totalFloors = grid.length;
            int slotsPerFloor = grid[0].length;

            // Revenue breakdown by vehicle type
            double bikeRev = 0.0, carRev = 0.0, suvRev = 0.0, evRev = 0.0;
            for (Ticket t : garage.getHistory()) {
                double f = (t.getFeeBreakdown() != null) ? t.getFeeBreakdown().totalFee() : 0.0;
                switch (t.getVehicle().getType()) {
                    case BIKE -> bikeRev += f;
                    case CAR  -> carRev += f;
                    case SUV  -> suvRev += f;
                    case EV   -> evRev += f;
                }
            }

            // Active vehicle count by type
            int activeBikes = 0, activeCars = 0, activeSuvs = 0, activeEvs = 0;
            for (Ticket t : garage.getActiveTickets().values()) {
                switch (t.getVehicle().getType()) {
                    case BIKE -> activeBikes++;
                    case CAR  -> activeCars++;
                    case SUV  -> activeSuvs++;
                    case EV   -> activeEvs++;
                }
            }

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"totalFloors\":").append(totalFloors).append(",");
            json.append("\"slotsPerFloor\":").append(slotsPerFloor).append(",");
            json.append("\"totalCapacity\":").append(garage.getTotalCapacity()).append(",");
            json.append("\"activeVehicles\":").append(garage.getActiveVehicleCount()).append(",");
            json.append("\"freeSlots\":").append(garage.getAvailableSlotsCount()).append(",");
            json.append("\"totalRevenue\":").append(String.format(Locale.US, "%.2f", garage.getTotalRevenue())).append(",");
            json.append("\"bikeRevenue\":").append(String.format(Locale.US, "%.2f", bikeRev)).append(",");
            json.append("\"carRevenue\":").append(String.format(Locale.US, "%.2f", carRev)).append(",");
            json.append("\"suvRevenue\":").append(String.format(Locale.US, "%.2f", suvRev)).append(",");
            json.append("\"evRevenue\":").append(String.format(Locale.US, "%.2f", evRev)).append(",");
            json.append("\"activeBikes\":").append(activeBikes).append(",");
            json.append("\"activeCars\":").append(activeCars).append(",");
            json.append("\"activeSuvs\":").append(activeSuvs).append(",");
            json.append("\"activeEvs\":").append(activeEvs).append(",");
            json.append("\"floors\":[");

            for (int f = totalFloors - 1; f >= 0; f--) {
                int occupiedCount = 0;
                StringBuilder slotsJson = new StringBuilder();

                for (int s = 0; s < slotsPerFloor; s++) {
                    ParkingSlot slot = grid[f][s];
                    if (slot.isOccupied()) occupiedCount++;

                    if (s > 0) slotsJson.append(",");
                    slotsJson.append("{");
                    slotsJson.append("\"id\":\"").append(slot.getId()).append("\",");
                    slotsJson.append("\"floor\":").append(slot.getFloor()).append(",");
                    slotsJson.append("\"slotNumber\":").append(slot.getSlotNumber()).append(",");
                    slotsJson.append("\"slotType\":\"").append(slot.getSlotType().name()).append("\",");
                    slotsJson.append("\"slotTypeName\":\"").append(escape(slot.getSlotType().getDisplayName())).append("\",");
                    slotsJson.append("\"symbol\":\"").append(escape(slot.getSlotType().getDisplaySymbol())).append("\",");
                    slotsJson.append("\"occupied\":").append(slot.isOccupied()).append(",");
                    slotsJson.append("\"distance\":").append(SlotAllocator.computeDistance(slot.getFloor(), slot.getSlotNumber())).append(",");

                    if (slot.isOccupied()) {
                        Vehicle v = slot.getCurrentVehicle();
                        slotsJson.append("\"plate\":\"").append(v.getLicensePlate()).append("\",");
                        slotsJson.append("\"vehicleType\":\"").append(v.getType().name()).append("\"");
                    } else {
                        slotsJson.append("\"plate\":null,\"vehicleType\":null");
                    }
                    slotsJson.append("}");
                }

                double rate = ((double) occupiedCount / slotsPerFloor) * 100.0;

                if (f < totalFloors - 1) json.append(",");
                json.append("{");
                json.append("\"floor\":").append(f).append(",");
                json.append("\"occupiedCount\":").append(occupiedCount).append(",");
                json.append("\"occupancyRate\":").append(String.format(Locale.US, "%.1f", rate)).append(",");
                json.append("\"slots\":[").append(slotsJson).append("]");
                json.append("}");
            }

            json.append("]}");
            sendJson(exchange, 200, json.toString());
        }
    }

    /**
     * POST /api/park -> body: {"plate": "...", "type": "..."}
     */
    private class ParkVehicleApi implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String rawPlate = extractJsonValue(body, "plate");
            String rawType = extractJsonValue(body, "type");

            try {
                String cleanPlate = InputValidator.validateAndNormalizePlate(rawPlate);
                if (rawType == null || rawType.trim().isEmpty()) {
                    throw new InvalidInputException("Vehicle type select karna zaroori hai (BIKE, CAR, SUV, EV).");
                }
                VehicleType vType = VehicleType.valueOf(rawType.trim().toUpperCase());
                Vehicle vehicle = new Vehicle(cleanPlate, vType);

                Ticket ticket = garage.parkVehicle(vehicle);

                boolean isFallback = (vType == VehicleType.EV && ticket.getSlot().getSlotType() != SlotType.EV_CHARGING);
                String notice = isFallback ? "No EV slot free, assigned regular slot (no charging)." : null;

                String res = String.format(Locale.US,
                        "{\"success\":true,\"message\":\"Gaadi successfully park ho gayi!\",\"ticketId\":\"%s\",\"plate\":\"%s\",\"vehicleType\":\"%s\",\"slotId\":\"%s\",\"slotType\":\"%s\",\"floor\":%d,\"notice\":%s}",
                        ticket.getTicketId(),
                        cleanPlate,
                        vType.name(),
                        ticket.getSlot().getId(),
                        ticket.getSlot().getSlotType().getDisplayName(),
                        ticket.getSlot().getFloor(),
                        notice != null ? "\"" + escape(notice) + "\"" : "null");

                sendJson(exchange, 200, res);

            } catch (Exception e) {
                String err = String.format("{\"success\":false,\"error\":\"%s\"}", escape(e.getMessage()));
                sendJson(exchange, 400, err);
            }
        }
    }

    /**
     * POST /api/exit -> body: {"plate": "..."}
     */
    private class ExitVehicleApi implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"success\":false,\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String rawPlate = extractJsonValue(body, "plate");

            try {
                String cleanPlate = InputValidator.validateAndNormalizePlate(rawPlate);
                Ticket ticket = garage.exitVehicle(cleanPlate);
                FeeBreakdown fee = ticket.getFeeBreakdown();

                String res = String.format(Locale.US,
                        "{\"success\":true,\"ticketId\":\"%s\",\"plate\":\"%s\",\"vehicleType\":\"%s\",\"slotId\":\"%s\",\"slotType\":\"%s\",\"entryTime\":\"%s\",\"exitTime\":\"%s\",\"totalMinutes\":%d,\"billableHours\":%d,\"parkingFee\":%.2f,\"chargingFee\":%.2f,\"totalFee\":%.2f,\"chargingApplied\":%b}",
                        ticket.getTicketId(),
                        ticket.getVehicle().getLicensePlate(),
                        ticket.getVehicle().getType().name(),
                        ticket.getSlot().getId(),
                        ticket.getSlot().getSlotType().getDisplayName(),
                        ticket.getEntryTime().format(TIME_FMT),
                        ticket.getExitTime().format(TIME_FMT),
                        fee.totalMinutes(),
                        fee.billableHours(),
                        fee.parkingFee(),
                        fee.chargingFee(),
                        fee.totalFee(),
                        fee.isChargingApplied());

                sendJson(exchange, 200, res);

            } catch (Exception e) {
                String err = String.format("{\"success\":false,\"error\":\"%s\"}", escape(e.getMessage()));
                sendJson(exchange, 400, err);
            }
        }
    }

    /**
     * GET /api/find?plate=MH12AB1234
     */
    private class FindVehicleApi implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String query = exchange.getRequestURI().getQuery();
            String plate = getQueryParam(query, "plate");

            try {
                String cleanPlate = InputValidator.validateAndNormalizePlate(plate);
                ParkingSlot slot = garage.findVehicle(cleanPlate);

                String res = String.format(Locale.US,
                        "{\"success\":true,\"plate\":\"%s\",\"slotId\":\"%s\",\"floor\":%d,\"slotNumber\":%d,\"slotType\":\"%s\",\"slotTypeName\":\"%s\"}",
                        cleanPlate,
                        slot.getId(),
                        slot.getFloor(),
                        slot.getSlotNumber(),
                        slot.getSlotType().name(),
                        escape(slot.getSlotType().getDisplayName()));

                sendJson(exchange, 200, res);

            } catch (Exception e) {
                String err = String.format("{\"success\":false,\"error\":\"%s\"}", escape(e.getMessage()));
                sendJson(exchange, 404, err);
            }
        }
    }

    /**
     * POST /api/demo -> runs full simulation: parks mixed vehicles of ALL types,
     * checks out BIKE, CAR, SUV and EV to demonstrate time and revenue for each type.
     */
    private class DemoApi implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            try {
                garage.reset();
                LocalDateTime now = LocalDateTime.now();

                Vehicle[] demoVehicles = new Vehicle[]{
                        new Vehicle("MH12BK0001", VehicleType.BIKE), // Will checkout (2h stay)
                        new Vehicle("DL01CR2002", VehicleType.CAR),  // Will checkout (3h stay)
                        new Vehicle("KA05SV3003", VehicleType.SUV),  // Will checkout (26h stay: Daily cap)
                        new Vehicle("MH01EV1001", VehicleType.EV),   // Will checkout (4h stay: EV Charging fee)
                        new Vehicle("MH01EV1002", VehicleType.EV),
                        new Vehicle("KA01EV2001", VehicleType.EV),
                        new Vehicle("KA01EV2002", VehicleType.EV),
                        new Vehicle("DL04EV3001", VehicleType.EV),
                        new Vehicle("DL04EV3002", VehicleType.EV),
                        new Vehicle("HR26EV4001", VehicleType.EV),   // EV Fallback to regular slot!
                        new Vehicle("MH14BK2020", VehicleType.BIKE), // Stays active
                        new Vehicle("DL08CR3030", VehicleType.CAR),  // Stays active
                        new Vehicle("KA02SV4040", VehicleType.SUV)   // Stays active
                };

                for (int i = 0; i < demoVehicles.length; i++) {
                    Vehicle v = demoVehicles[i];
                    LocalDateTime entryTime;
                    if (i == 0) entryTime = now.minusHours(2);
                    else if (i == 1) entryTime = now.minusHours(3);
                    else if (i == 2) entryTime = now.minusHours(26);
                    else if (i == 3) entryTime = now.minusHours(4);
                    else entryTime = now.minusMinutes((i + 1) * 15L);

                    try {
                        garage.parkVehicle(v, entryTime);
                    } catch (Exception ignored) {
                    }
                }

                // Checkouts for ALL 4 VEHICLE TYPES (Bike, Car, SUV, EV)
                try { garage.exitVehicle("MH12BK0001", now); } catch (Exception ignored) {}
                try { garage.exitVehicle("DL01CR2002", now); } catch (Exception ignored) {}
                try { garage.exitVehicle("KA05SV3003", now); } catch (Exception ignored) {}
                try { garage.exitVehicle("MH01EV1001", now); } catch (Exception ignored) {}

                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Automated Demo executed! All 4 vehicle types (Bike, Car, SUV, EV) parked and checked out with duration and revenue calculations.\"}");

            } catch (Exception e) {
                sendJson(exchange, 500, "{\"success\":false,\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    /**
     * POST /api/reset -> clears all slots and resets the garage
     */
    private class ResetApi implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;
            garage.reset();
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Garage reset successfully! All bays are now empty.\"}");
        }
    }

    /**
     * GET /api/history -> returns list of completed tickets with full breakdown
     */
    private class HistoryApi implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            List<Ticket> history = garage.getHistory();
            StringBuilder json = new StringBuilder("[");

            for (int i = 0; i < history.size(); i++) {
                Ticket t = history.get(i);
                FeeBreakdown fee = t.getFeeBreakdown();
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"ticketId\":\"").append(t.getTicketId()).append("\",");
                json.append("\"plate\":\"").append(t.getVehicle().getLicensePlate()).append("\",");
                json.append("\"vehicleType\":\"").append(t.getVehicle().getType().name()).append("\",");
                json.append("\"slotId\":\"").append(t.getSlot().getId()).append("\",");
                json.append("\"entryTime\":\"").append(t.getEntryTime().format(TIME_FMT)).append("\",");
                json.append("\"exitTime\":\"").append(t.getExitTime() != null ? t.getExitTime().format(TIME_FMT) : "").append("\",");
                json.append("\"duration\":\"").append(formatDuration(t.getDuration())).append("\",");
                json.append("\"totalMinutes\":").append(fee != null ? fee.totalMinutes() : 0).append(",");
                // Output billableHours, parkingFee, etc. as STRINGS so JS never treats 0 as falsy
                json.append("\"billableHours\":\"").append(fee != null ? fee.billableHours() : 0).append("\",");
                json.append("\"parkingFee\":\"").append(fee != null ? String.format(Locale.US, "%.2f", fee.parkingFee()) : "0.00").append("\",");
                json.append("\"chargingFee\":\"").append(fee != null ? String.format(Locale.US, "%.2f", fee.chargingFee()) : "0.00").append("\",");
                json.append("\"totalFee\":\"").append(fee != null ? String.format(Locale.US, "%.2f", fee.totalFee()) : "0.00").append("\",");
                json.append("\"fee\":\"").append(fee != null ? String.format(Locale.US, "%.2f", fee.totalFee()) : "0.00").append("\",");
                json.append("\"chargingApplied\":").append(fee != null && fee.isChargingApplied());
                json.append("}");
            }
            json.append("]");

            sendJson(exchange, 200, json.toString());
        }

        private String formatDuration(java.time.Duration d) {
            long hours = d.toHours();
            long minutes = d.toMinutesPart();
            return String.format("%dh %02dm", hours, minutes);
        }
    }

    // ========================================================================
    // UTILITY METHODS (Zero External Libraries)
    // ========================================================================

    private boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendCors(exchange, 204, "");
            return true;
        }
        return false;
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        sendCors(exchange, status, bytes);
    }

    private void sendResponse(HttpExchange exchange, int status, byte[] data) throws IOException {
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private void sendCors(HttpExchange exchange, int status, String text) throws IOException {
        sendCors(exchange, status, text.getBytes(StandardCharsets.UTF_8));
    }

    private void sendCors(HttpExchange exchange, int status, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(status, data.length > 0 ? data.length : -1);
        if (data.length > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }

    private String extractJsonValue(String json, String key) {
        if (json == null) return null;
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equalsIgnoreCase(param)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
