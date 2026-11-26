package garage;

import member1.ParkingGarage;
import member1.ParkingSlot;
import member1.SlotType;
import member1.Vehicle;
import member1.VehicleType;
import member2.SlotAllocator;
import member3.GarageFullException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * MEMBER 3 (Face) & MEMBER 2 (Brain) - SlotAllocator Tests
 * ============================================================================
 * Yeh test suite Nearest-Slot algorithm ko test karti hai:
 * 1. Nearest Slot allocation: Exit (0,0) ke sabse kareeb ka slot mile.
 * 2. Bike SMALL slot pasand kare.
 * 3. SUV sirf LARGE slot me jaye.
 * 4. EV pehle EV_CHARGING dhoondhe.
 * 5. EV fallback: Sabhi EV bays full hone par regular slot me jaye.
 * 6. Garage full hone par GarageFullException aaye.
 * 7. Purity test: Allocator grid ko modify na kare.
 * ============================================================================
 */
class SlotAllocatorTest {

    private SlotAllocator allocator;
    private ParkingSlot[][] grid;

    @BeforeEach
    void setUp() {
        allocator = new SlotAllocator();
        grid = new ParkingSlot[3][10];
        for (int f = 0; f < 3; f++) {
            for (int s = 0; s < 10; s++) {
                SlotType type = ParkingGarage.determineSlotType(s);
                grid[f][s] = new ParkingSlot(f, s, type);
            }
        }
    }

    @Test
    @DisplayName("1. Exit (0,0) ke sabse kareeb ka slot allocate hona chahiye")
    void testNearestSlotAllocation() throws GarageFullException {
        // Floor 0 par regular slot 2 se shuru hote hain (dist = 0*20 + 2 = 2)
        Vehicle car = new Vehicle("DL01CR1001", VehicleType.CAR);
        ParkingSlot allocated = allocator.allocateSlot(grid, car);

        assertNotNull(allocated);
        assertEquals(0, allocated.getFloor(), "Floor 0 milna chahiye");
        assertEquals(2, allocated.getSlotNumber(), "Slot 2 milna chahiye");
    }

    @Test
    @DisplayName("2. Bike ko pehle SMALL slot (F0-S00) milna chahiye")
    void testBikePrefersSmallSlot() throws GarageFullException {
        Vehicle bike = new Vehicle("MH12BK9999", VehicleType.BIKE);
        ParkingSlot allocated = allocator.allocateSlot(grid, bike);

        assertEquals(SlotType.SMALL, allocated.getSlotType());
        assertEquals(0, allocated.getFloor());
        assertEquals(0, allocated.getSlotNumber());
    }

    @Test
    @DisplayName("3. SUV ko sirf LARGE slot (Slot 7) milna chahiye")
    void testSuvOnlyLargeSlot() throws GarageFullException {
        Vehicle suv = new Vehicle("KA05SV7777", VehicleType.SUV);
        ParkingSlot allocated = allocator.allocateSlot(grid, suv);

        assertEquals(SlotType.LARGE, allocated.getSlotType());
        assertEquals(7, allocated.getSlotNumber());
    }

    @Test
    @DisplayName("4. EV ko pehle EV_CHARGING slot milna chahiye")
    void testEvPrefersChargingSlot() throws GarageFullException {
        Vehicle ev = new Vehicle("MH01EV1111", VehicleType.EV);
        ParkingSlot allocated = allocator.allocateSlot(grid, ev);

        assertEquals(SlotType.EV_CHARGING, allocated.getSlotType());
        assertEquals(0, allocated.getFloor());
        assertEquals(8, allocated.getSlotNumber());
    }

    @Test
    @DisplayName("5. Saare EV charging slots full hone par EV regular slot me fallback kare")
    void testEvFallbackToRegularSlot() throws GarageFullException {
        // Teeno floors ke saare EV charging slots (slots 8 aur 9) bhar dete hain
        for (int f = 0; f < 3; f++) {
            grid[f][8].park(new Vehicle("DUMMY_EV_" + f + "_8", VehicleType.EV));
            grid[f][9].park(new Vehicle("DUMMY_EV_" + f + "_9", VehicleType.EV));
        }

        // Ab koi EV charging slot free nahi hai!
        Vehicle incomingEv = new Vehicle("DL03EV9999", VehicleType.EV);
        ParkingSlot fallbackSlot = allocator.allocateSlot(grid, incomingEv);

        assertNotNull(fallbackSlot);
        assertEquals(SlotType.REGULAR, fallbackSlot.getSlotType(), "Regular slot milna chahiye fallback me");
        assertEquals(0, fallbackSlot.getFloor());
        assertEquals(2, fallbackSlot.getSlotNumber());
    }

    @Test
    @DisplayName("6. Jab koi compatible slot na bache toh GarageFullException aana chahiye")
    void testGarageFullException() {
        // Saare LARGE slots bhar dete hain
        grid[0][7].park(new Vehicle("SUV_0", VehicleType.SUV));
        grid[1][7].park(new Vehicle("SUV_1", VehicleType.SUV));
        grid[2][7].park(new Vehicle("SUV_2", VehicleType.SUV));

        Vehicle newSuv = new Vehicle("HR26SV8888", VehicleType.SUV);
        assertThrows(GarageFullException.class, () -> allocator.allocateSlot(grid, newSuv));
    }

    @Test
    @DisplayName("7. Purity Test: SlotAllocator ko grid modify nahi karna chahiye")
    void testAllocatorPurity() throws GarageFullException {
        Vehicle car = new Vehicle("MH12CR5555", VehicleType.CAR);
        ParkingSlot returnedSlot = allocator.allocateSlot(grid, car);

        assertFalse(returnedSlot.isOccupied(), "Slot khaali hi rehna chahiye");
        assertNull(returnedSlot.getCurrentVehicle());
    }

    @Test
    @DisplayName("8. Maintenance mode wale slot ko SlotAllocator skip karna chahiye")
    void testMaintenanceSlotSkipped() throws GarageFullException {
        grid[0][2].setUnderMaintenance(true); // Closest CAR slot under maintenance

        Vehicle car = new Vehicle("MH12CR1111", VehicleType.CAR);
        ParkingSlot allocated = allocator.allocateSlot(grid, car);

        assertEquals(0, allocated.getFloor());
        assertEquals(3, allocated.getSlotNumber(), "F0-S02 maintenance me hai toh F0-S03 milna chahiye");
    }
}
