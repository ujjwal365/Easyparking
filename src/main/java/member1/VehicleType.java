package member1;

import java.util.Collections;
import java.util.List;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - VehicleType Enum
 * ============================================================================
 * Yeh enum gaadiyon ke types ko define karta hai aur compatibility rules ko
 * EK HI JAGAH (centralized) rakhta hai taaki project clean rahe.
 * 
 * COMPATIBILITY RULES:
 * - BIKE: SMALL slot pasand karti hai, par zaroorat padne par REGULAR ya LARGE me bhi ja sakti hai.
 * - CAR : REGULAR pasand karti hai, LARGE me bhi fit ho sakti hai.
 * - SUV : Size me badi hoti hai, isliye sirf LARGE slot me hi park hogi.
 * - EV  : Pehle EV_CHARGING dhoondhti hai. Agar full ho toh REGULAR/LARGE fallback me jayegi.
 * ============================================================================
 */
public enum VehicleType {

    // Bike: Preferred SMALL, compatible: SMALL, REGULAR, LARGE
    BIKE("Motorcycle / Bike",
         SlotType.SMALL,
         List.of(SlotType.SMALL, SlotType.REGULAR, SlotType.LARGE)),

    // Car: Preferred REGULAR, compatible: REGULAR, LARGE
    CAR("Sedan / Hatchback Car",
        SlotType.REGULAR,
        List.of(SlotType.REGULAR, SlotType.LARGE)),

    // SUV: Badi gaadi, sirf LARGE slot me aayegi
    SUV("SUV / Large Vehicle",
        SlotType.LARGE,
        List.of(SlotType.LARGE)),

    // Electric Vehicle: Pehle EV_CHARGING, na mile toh Regular/Large fallback
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

    // Check karta hai ki kya yeh gaadi diye gaye slot type me fit ho sakti hai
    public boolean isCompatibleWith(SlotType slotType) {
        if (slotType == null) {
            return false;
        }
        return compatibleSlots.contains(slotType);
    }

    // Check karta hai ki kya gaadi preferred slot ke bajaye kisi doosre fallback slot me hai
    public boolean isFallback(SlotType slotType) {
        return slotType != preferredSlotType;
    }
}

