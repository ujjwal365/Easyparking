package member1;

import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - VehicleType Enum
 * ============================================================================
 * Centralized Vehicle Type & Compatibility Rules:
 *
 * 1. BIKE : Preferred = SMALL       | Compatible = SMALL, REGULAR, LARGE
 * 2. CAR  : Preferred = REGULAR     | Compatible = REGULAR, LARGE
 * 3. SUV  : Preferred = LARGE       | Compatible = LARGE (Sirf large me aayegi)
 * 4. EV   : Preferred = EV_CHARGING | Compatible = EV_CHARGING, REGULAR, LARGE
 *           (EV agar charging slot na mile toh regular slot me fallback karti hai)
 * ============================================================================
 */
public enum VehicleType {

    // 1. BIKE (Two-Wheeler)
    BIKE("Motorcycle / Bike",
         SlotType.SMALL,
         List.of(SlotType.SMALL, SlotType.REGULAR, SlotType.LARGE)),

    // 2. CAR (Sedan / Hatchback)
    CAR("Sedan / Hatchback Car",
        SlotType.REGULAR,
        List.of(SlotType.REGULAR, SlotType.LARGE)),

    // 3. SUV (Large Vehicle)
    SUV("SUV / Large Vehicle",
        SlotType.LARGE,
        List.of(SlotType.LARGE)),

    // 4. EV (Electric Vehicle)
    EV("Electric Vehicle (EV)",
       SlotType.EV_CHARGING,
       List.of(SlotType.EV_CHARGING, SlotType.REGULAR, SlotType.LARGE));

    private final String description;
    private final SlotType preferredSlotType;
    private final List<SlotType> compatibleSlots;

    VehicleType(String description, SlotType preferredSlotType, List<SlotType> compatibleSlots) {
        this.description = description;
        this.preferredSlotType = preferredSlotType;
        this.compatibleSlots = compatibleSlots;
    }

    public String getDescription() {
        return description;
    }

    public SlotType getPreferredSlotType() {
        return preferredSlotType;
    }

    public List<SlotType> getCompatibleSlots() {
        return Collections.unmodifiableList(compatibleSlots);
    }

    /**
     * Check karta hai ki kya yeh slot type is gaadi ke liye compatible hai.
     */
    public boolean isCompatibleWith(SlotType slotType) {
        if (slotType == null) {
            return false;
        }
        return compatibleSlots.contains(slotType);
    }

    /**
     * Check karta hai ki kya gaadi fallback slot me hai (preferred slot nahi mila).
     */
    public boolean isFallback(SlotType slotType) {
        return slotType != preferredSlotType;
    }
}
