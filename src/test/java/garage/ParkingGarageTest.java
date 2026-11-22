package garage;

import member1.ParkingGarage;
import member1.ParkingSlot;
import member1.Ticket;
import member1.Vehicle;
import member1.VehicleType;
import member3.DuplicateVehicleException;
import member3.GarageFullException;
import member3.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * MEMBER 3 (Face) & MEMBER 1 (Backbone) - ParkingGarage Tests
 * ============================================================================
 * Yeh test class ParkingGarage ke complete lifecycle ko test karti hai:
 * 1. Gaadi park karna (slot update, active ticket, count badhna)
 * 2. Duplicate plate par DuplicateVehicleException aana
 * 3. Plate se gaadi dhoondhna
 * 4. Missing gaadi par VehicleNotFoundException aana
 * 5. Exit par slot khaali hona, ticket close hona, revenue update hona
 * 6. Total revenue ka sahi hisaab jodna
 * ============================================================================
 */
class ParkingGarageTest {

    private ParkingGarage garage;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        garage = new ParkingGarage(3, 10);
        baseTime = LocalDateTime.of(2026, 11, 17, 10, 0, 0);
    }

    @Test
    @DisplayName("1. Gaadi park karne par slot occupied hona chahiye aur ticket active banna chahiye")
    void testParkVehicleSuccess() throws Exception {
        Vehicle car = new Vehicle("MH12AB1234", VehicleType.CAR);
        Ticket ticket = garage.parkVehicle(car, baseTime);

        assertNotNull(ticket);
        assertEquals("MH12AB1234", ticket.getVehicle().getLicensePlate());
        assertTrue(ticket.isActive());
        assertTrue(ticket.getSlot().isOccupied());
        assertEquals(car, ticket.getSlot().getCurrentVehicle());
        assertEquals(1, garage.getActiveVehicleCount());
    }

    @Test
    @DisplayName("2. Same number plate dobara park karne par DuplicateVehicleException aana chahiye")
    void testDuplicateVehicleException() throws Exception {
        Vehicle car1 = new Vehicle("MH12AB1234", VehicleType.CAR);
        garage.parkVehicle(car1, baseTime);

        Vehicle car2 = new Vehicle("MH12AB1234", VehicleType.CAR);
        assertThrows(DuplicateVehicleException.class, () -> garage.parkVehicle(car2, baseTime));
    }

    @Test
    @DisplayName("3. License plate se gaadi sahi slot par milni chahiye")
    void testFindVehicleSuccess() throws Exception {
        Vehicle suv = new Vehicle("KA05MB9999", VehicleType.SUV);
        Ticket ticket = garage.parkVehicle(suv, baseTime);

        ParkingSlot foundSlot = garage.findVehicle("KA05MB9999");
        assertEquals(ticket.getSlot(), foundSlot);
        assertEquals(0, foundSlot.getFloor());
        assertEquals(7, foundSlot.getSlotNumber());
    }

    @Test
    @DisplayName("4. Jo gaadi garage me nahi hai use dhoondhne par VehicleNotFoundException aana chahiye")
    void testFindVehicleNotFound() {
        assertThrows(VehicleNotFoundException.class, () -> garage.findVehicle("DL01XX9999"));
    }

    @Test
    @DisplayName("5. Exit karne par slot khaali hona chahiye aur bill generate hona chahiye")
    void testExitVehicleSuccess() throws Exception {
        Vehicle car = new Vehicle("DL01A5678", VehicleType.CAR);
        garage.parkVehicle(car, baseTime);

        ParkingSlot slotBefore = garage.findVehicle("DL01A5678");
        assertTrue(slotBefore.isOccupied());

        // 2 ghante baad checkout
        LocalDateTime exitTime = baseTime.plusHours(2);
        Ticket completedTicket = garage.exitVehicle("DL01A5678", exitTime);

        // Ticket verify karo
        assertFalse(completedTicket.isActive());
        assertEquals(exitTime, completedTicket.getExitTime());
        assertEquals(60.0, completedTicket.getTotalFee(), "2 ghante ka charge 2 * ₹30 = ₹60 hona chahiye");

        // Slot khaali ho jana chahiye
        assertFalse(slotBefore.isOccupied());
        assertNull(slotBefore.getCurrentVehicle());

        // History aur revenue verify karo
        assertEquals(0, garage.getActiveVehicleCount());
        assertEquals(1, garage.getHistory().size());
        assertEquals(60.0, garage.getTotalRevenue());
    }

    @Test
    @DisplayName("6. Missing gaadi ko exit karne par VehicleNotFoundException aana chahiye")
    void testExitVehicleNotFound() {
        assertThrows(VehicleNotFoundException.class, () -> garage.exitVehicle("DL01XX9999"));
    }

    @Test
    @DisplayName("7. Ek se zyada checkouts ka total revenue sahi judna chahiye")
    void testCumulativeRevenue() throws Exception {
        Vehicle bike = new Vehicle("MH12BK1111", VehicleType.BIKE);
        Vehicle car = new Vehicle("DL01CR2222", VehicleType.CAR);

        garage.parkVehicle(bike, baseTime);
        garage.parkVehicle(car, baseTime);

        // Bike 1 ghante baad gayi: 1 * ₹10 = ₹10
        garage.exitVehicle("MH12BK1111", baseTime.plusHours(1));
        assertEquals(10.0, garage.getTotalRevenue());

        // Car 3 ghante baad gayi: 3 * ₹30 = ₹90
        garage.exitVehicle("DL01CR2222", baseTime.plusHours(3));
        assertEquals(100.0, garage.getTotalRevenue(), "Total revenue ₹10 + ₹90 = ₹100 hona chahiye");
    }
}
