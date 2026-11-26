package member2;

import member1.ParkingSlot;
import member1.SlotType;
import member1.Vehicle;
import member1.VehicleType;
import member3.GarageFullException;

import java.util.List;

/**
 * ============================================================================
 * MEMBER 2 (Brain) - SlotAllocator Class
 * ============================================================================
 * Yeh class parking slot allocate karne ka mukhya ALGORITHM (dimag) hai.
 * 
 * DESIGN PRINCIPLES:
 * 1. PURE FUNCTION: Yeh sirf grid aur vehicle check karta hai. Grid me koi change nahi karta.
 * 2. NEAREST-SLOT FORMULA:
 *    Distance = (floor * FLOOR_PENALTY) + slotIndex
 *    jaha FLOOR_PENALTY = 20 hai, aur exit point (Floor 0, Slot 0) par hai.
 *    Ramp chadhkar upar jane me mehnat aur time lagta hai, isliye floor penalty 20 rakhi hai.
 * 3. FALLBACK LOGIC:
 *    - EV: Pehle EV_CHARGING slot dhoondhta hai. Agar sab bhare hon toh REGULAR/LARGE slot
 *          me bhejta hai aur notice deta hai: "No EV slot free, assigned regular slot (no charging)".
 *    - Bike: Pehle SMALL slot prefer karti hai.
 *    - Car : REGULAR slot prefer karti hai.
 *    - SUV : Sirf LARGE slot me hi fit hoti hai.
 * 
 * COMPLEXITY:
 * - Time Complexity: O(F * S) -> 3 floors x 10 slots = max 30 checks (super fast).
 * - Viva Alternative: 10,000 slots ke liye PriorityQueue (Min-Heap) use kar sakte hain
 *   jisse O(1) peek aur O(log N) poll time milta hai.
 * ============================================================================
 */
public class SlotAllocator {

    public static final int FLOOR_PENALTY = 20;

    public ParkingSlot allocateSlot(ParkingSlot[][] grid, Vehicle vehicle) throws GarageFullException {
        if (grid == null || vehicle == null) {
            throw new IllegalArgumentException("Grid aur Vehicle null nahi ho sakte.");
        }

        VehicleType vType = vehicle.getType();

        // --------------------------------------------------------------------
        // CASE 1: Electric Vehicle (EV)
        // --------------------------------------------------------------------
        if (vType == VehicleType.EV) {
            ParkingSlot evSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.EV_CHARGING));
            if (evSlot != null) {
                return evSlot;
            }

            // Fallback: regular/large slot
            ParkingSlot fallbackSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.REGULAR, SlotType.LARGE));
            if (fallbackSlot != null) {
                System.out.println("[NOTICE] No EV slot free, assigned regular slot (no charging).");
                return fallbackSlot;
            }

            throw new GarageFullException("Garage Full! EV " + vehicle.getLicensePlate() + " ke liye koi slot nahi bacha.");
        }

        // --------------------------------------------------------------------
        // CASE 2: Bike (Prefers SMALL, falls back to REGULAR/LARGE)
        // --------------------------------------------------------------------
        if (vType == VehicleType.BIKE) {
            ParkingSlot smallSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.SMALL));
            if (smallSlot != null) {
                return smallSlot;
            }
            ParkingSlot fallbackSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.REGULAR, SlotType.LARGE));
            if (fallbackSlot != null) {
                return fallbackSlot;
            }
            throw new GarageFullException("Garage Full! Bike " + vehicle.getLicensePlate() + " ke liye slot nahi mila.");
        }

        // --------------------------------------------------------------------
        // CASE 3: Car (Prefers REGULAR, falls back to LARGE)
        // --------------------------------------------------------------------
        if (vType == VehicleType.CAR) {
            ParkingSlot regSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.REGULAR));
            if (regSlot != null) {
                return regSlot;
            }
            ParkingSlot largeSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.LARGE));
            if (largeSlot != null) {
                return largeSlot;
            }
            throw new GarageFullException("Garage Full! Car " + vehicle.getLicensePlate() + " ke liye slot nahi mila.");
        }

        // --------------------------------------------------------------------
        // CASE 4: SUV (Sirf LARGE slot)
        // --------------------------------------------------------------------
        if (vType == VehicleType.SUV) {
            ParkingSlot largeSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.LARGE));
            if (largeSlot != null) {
                return largeSlot;
            }
            throw new GarageFullException("Garage Full! SUV " + vehicle.getLicensePlate() + " ke liye LARGE slot nahi bacha.");
        }

        ParkingSlot candidate = findBestFreeSlotOfTypes(grid, vType.getCompatibleSlots());
        if (candidate != null) {
            return candidate;
        }

        throw new GarageFullException("Garage Full! Gaadi ke liye slot uplabdh nahi: " + vehicle);
    }

    private ParkingSlot findBestFreeSlotOfTypes(ParkingSlot[][] grid, List<SlotType> allowedTypes) {
        ParkingSlot bestSlot = null;
        int minDistance = Integer.MAX_VALUE;

        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                ParkingSlot slot = grid[f][s];
                if (slot.isAvailable() && allowedTypes.contains(slot.getSlotType())) {
                    int dist = computeDistance(slot.getFloor(), slot.getSlotNumber());
                    if (dist < minDistance) {
                        minDistance = dist;
                        bestSlot = slot;
                    }
                }
            }
        }

        return bestSlot;
    }

    public static int computeDistance(int floor, int slotIndex) {
        return (floor * FLOOR_PENALTY) + slotIndex;
    }
}

