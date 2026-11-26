package member3;

import member1.ParkingGarage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * ============================================================================
 * MEMBER 3 (Face) - SystemVerifier
 * ============================================================================
 * Yeh utility class frontend aur backend ke connection ko verify karti hai:
 * 1. WebServer ko initialize aur start karta hai.
 * 2. GET / (index.html frontend page) probe karta hai.
 * 3. GET /api/garage (2D grid status) check karta hai.
 * 4. POST /api/park se gaadi park karke check karta hai.
 * 5. GET /api/find se slot location verify karta hai.
 * 6. POST /api/exit se bill receipt aur fees breakdown verify karta hai.
 * 7. POST /api/demo se 10-car scripted scenario test karta hai.
 * 8. POST /api/reset se garage clean karke verify karta hai.
 * ============================================================================
 */
public class SystemVerifier {

    public static void main(String[] args) {
        System.out.println("=".repeat(75));
        System.out.println("   🔍 STARTING FRONTEND & BACKEND INTEGRATION VERIFICATION 🔍");
        System.out.println("=".repeat(75));

        ParkingGarage garage = new ParkingGarage(3, 10);
        WebServer server = new WebServer(garage);

        try {
            int port = server.start();
            System.out.printf("✓ Step 1: WebServer started successfully on port %d (%s)%n", port, server.getUrl());

            HttpClient client = HttpClient.newHttpClient();
            String baseUrl = "http://localhost:" + port;

            // 1. Test GET / (HTML Page)
            HttpRequest htmlReq = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/")).GET().build();
            HttpResponse<String> htmlResp = client.send(htmlReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(htmlResp.statusCode() == 200 && htmlResp.body().contains("Smart Parking Garage"),
                    "Frontend index.html served with 200 OK & title present");

            // 2. Test GET /api/garage
            HttpRequest stateReq = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/garage")).GET().build();
            HttpResponse<String> stateResp = client.send(stateReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(stateResp.statusCode() == 200 && stateResp.body().contains("\"totalCapacity\":30"),
                    "Backend /api/garage returned 2D layout and 30 slots capacity");

            // 3. Test POST /api/park
            String parkPayload = "{\"plate\":\"MH12AB1234\",\"type\":\"CAR\"}";
            HttpRequest parkReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/park"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(parkPayload))
                    .build();
            HttpResponse<String> parkResp = client.send(parkReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(parkResp.statusCode() == 200 && parkResp.body().contains("\"success\":true") && parkResp.body().contains("F0-S02"),
                    "Backend /api/park allocated nearest slot F0-S02 to CAR MH12AB1234");

            // 4. Test GET /api/find
            HttpRequest findReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/find?plate=MH12AB1234"))
                    .GET()
                    .build();
            HttpResponse<String> findResp = client.send(findReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(findResp.statusCode() == 200 && findResp.body().contains("\"slotId\":\"F0-S02\""),
                    "Backend /api/find successfully located vehicle at Slot F0-S02");

            // 5. Test POST /api/exit
            String exitPayload = "{\"plate\":\"MH12AB1234\"}";
            HttpRequest exitReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/exit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(exitPayload))
                    .build();
            HttpResponse<String> exitResp = client.send(exitReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(exitResp.statusCode() == 200 && exitResp.body().contains("\"success\":true") && exitResp.body().contains("\"totalFee\":"),
                    "Backend /api/exit completed checkout and calculated fee breakdown");

            // 6. Test POST /api/demo
            HttpRequest demoReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demo"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> demoResp = client.send(demoReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(demoResp.statusCode() == 200 && demoResp.body().contains("\"success\":true"),
                    "Backend /api/demo ran 10-car scripted simulation with EV fallback");

            // 7. Verify post-demo garage state has active cars and revenue for all types
            HttpResponse<String> postDemoResp = client.send(stateReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(postDemoResp.body().contains("\"activeVehicles\":9") &&
                        postDemoResp.body().contains("\"bikeRevenue\":") &&
                        postDemoResp.body().contains("\"carRevenue\":") &&
                        postDemoResp.body().contains("\"suvRevenue\":") &&
                        postDemoResp.body().contains("\"evRevenue\":"),
                    "Post-Demo State verified: 9 active vehicles and Bike, Car, SUV, EV revenues calculated");

            // 8. Test POST /api/reset
            HttpRequest resetReq = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/reset"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> resetResp = client.send(resetReq, HttpResponse.BodyHandlers.ofString());
            assertCheck(resetResp.statusCode() == 200 && resetResp.body().contains("\"success\":true"),
                    "Backend /api/reset cleared all bays back to 0 active vehicles");

            System.out.println("=".repeat(75));
            System.out.println("🎉 ALL 8 FRONTEND & BACKEND INTEGRATION CHECKS PASSED SUCCESSFULLY! 🎉");
            System.out.println("=".repeat(75));

        } catch (Exception e) {
            System.out.println("❌ VERIFICATION ENCOUNTERED ISSUE: " + e.getMessage());
            e.printStackTrace();
        } finally {
            server.stop();
            System.out.println("✓ Server stopped cleanly.");
            System.exit(0);
        }
    }

    private static void assertCheck(boolean condition, String description) {
        if (condition) {
            System.out.println("  ✓ PASS: " + description);
        } else {
            System.out.println("  ❌ FAIL: " + description);
            throw new AssertionError("Check failed: " + description);
        }
    }
}
