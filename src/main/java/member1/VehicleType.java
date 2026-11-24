package member1;

import java.util.Collections;
import java.util.List;

/**
 * VehicleType — Gaadi ke 4 types aur unki slot compatibility.
 *
 * BIKE → pehle SMALL me daalo, nahi mile toh REGULAR / LARGE me bhi fit
 * CAR  → pehle REGULAR me, nahi mile toh LARGE me
 * SUV  → sirf LARGE me aayegi
 * EV   → pehle EV_CHARGING, nahi mile toh REGULAR / LARGE (fallback)
 */
public enum VehicleType {

    BIKE("Motorcycle / Bike",
         SlotType.SMALL,
         List.of(SlotType.SMALL, SlotType.REGULAR, SlotType.LARGE)),

    CAR("Sedan / Hatchback Car",
        SlotType.REGULAR,
        List.of(SlotType.REGULAR, SlotType.LARGE)),

    SUV("SUV / Large Vehicle",
        SlotType.LARGE,
        List.of(SlotType.LARGE)),

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

    public String getDescription()         { return description; }
    public SlotType getPreferredSlotType()  { return preferredSlotType; }
    public List<SlotType> getCompatibleSlots() { return Collections.unmodifiableList(compatibleSlots); }

    // Kya yeh slot type is gaadi ke liye chalega?
    public boolean isCompatibleWith(SlotType slotType) {
        return slotType != null && compatibleSlots.contains(slotType);
    }

    // Kya gaadi apne preferred slot ke bajay kisi aur (fallback) me hai?
    public boolean isFallback(SlotType slotType) {
        return slotType != preferredSlotType;
    }
}
