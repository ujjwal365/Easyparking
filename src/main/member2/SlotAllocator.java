package member2;

import member1.ParkingSlot;
import member1.SlotType;
import member1.Vehicle;
import member1.VehicleType;
import member3.GarageFullException;

import java.util.List;

public class SlotAllocator {

    public static final int FLOOR_PENALTY = 20;

    public ParkingSlot allocateSlot(ParkingSlot[][] grid, Vehicle vehicle) throws GarageFullException {
        if (grid == null || vehicle == null) {
            throw new IllegalArgumentException("Grid aur Vehicle null nahi ho sakte.");
        }

        VehicleType vType = vehicle.getType();

        if (vType == VehicleType.EV) {
            ParkingSlot evSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.EV_CHARGING));
            if (evSlot != null) {
                return evSlot;
            }
            ParkingSlot fallbackSlot = findBestFreeSlotOfTypes(grid, List.of(SlotType.REGULAR, SlotType.LARGE));
            if (fallbackSlot != null) {
                System.out.println("[NOTICE] No EV slot free, assigned regular slot (no charging).");
                return fallbackSlot;
            }

            throw new GarageFullException("Garage Full! EV " + vehicle.getLicensePlate() + " ke liye koi slot nahi bacha.");
        }

    
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
                if (!slot.isOccupied() && allowedTypes.contains(slot.getSlotType())) {
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

