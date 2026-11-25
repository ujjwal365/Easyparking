package member1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * VehicleType — Gaadi ke 4 types aur har gaadi kis slot me fit hogi uske rules.
 *
 * COLLECTIONS USED HERE & WHY:
 *   1. ArrayList (compatibleSlots):
 *      - Order matter karta hai! Jaise BIKE pehle SMALL check karegi, phir REGULAR, phir LARGE.
 *      - ArrayList elements ko isi priority order me store karta hai.
 *   2. HashSet / Set (compatibleSlotSet):
 *      - Set kabhi duplicates allow nahi karta aur O(1) instant speed se check karta hai
 *        ki koi slot type compatible hai ya nahi (isCompatibleWith function me).
 *
 * COMPATIBILITY RULES:
 *   BIKE -> Preferred: SMALL       | Allowed: SMALL, REGULAR, LARGE
 *   CAR  -> Preferred: REGULAR     | Allowed: REGULAR, LARGE
 *   SUV  -> Preferred: LARGE       | Allowed: LARGE (badi gaadi sirf large me hi jayegi)
 *   EV   -> Preferred: EV_CHARGING | Allowed: EV_CHARGING, REGULAR, LARGE (charging full ho toh fallback)
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

    // ArrayList: Priority order preserve karta hai (preferred slot pehle, fallback baad me)
    private final ArrayList<SlotType> compatibleSlots;

    // Set (HashSet): Unique allowed slot types ka fast O(1) lookup karne ke liye
    private final Set<SlotType> compatibleSlotSet;

    // WHY CONSTRUCTOR: Har vehicle type ke saath uske rules (naam, best slot, allowed list + set) ek saath bind karne ke liye.
    VehicleType(String description, SlotType preferredSlotType, List<SlotType> allowedSlots) {
        this.description = description;
        this.preferredSlotType = preferredSlotType;
        this.compatibleSlots = new ArrayList<>(allowedSlots);
        this.compatibleSlotSet = new HashSet<>(allowedSlots);
    }

    // WHY THIS FUNCTION: Receipt aur dashboard par gaadi ka full naam dikhane ke liye (e.g. "Sedan / Hatchback Car").
    public String getDescription() {
        return description;
    }

    // WHY THIS FUNCTION: Batata hai ki is gaadi ke liye sabse best (ideal) slot kaunsa hai.
    public SlotType getPreferredSlotType() {
        return preferredSlotType;
    }

    // WHY THIS FUNCTION: Member 2 (SlotAllocator) ko priority order me allowed slots deta hai.
    // unmodifiableList isliye return karte hain taaki bahar se koi rule list ko change na kar paaye.
    public List<SlotType> getCompatibleSlots() {
        return Collections.unmodifiableList(compatibleSlots);
    }

    // WHY THIS FUNCTION: Fast check karne ke liye ki yeh gaadi diye gaye slotType me park ho sakti hai ya nahi.
    // Yahan HashSet (Set) use hua hai kyunki Set.contains() instant O(1) check deta hai.
    public boolean isCompatibleWith(SlotType slotType) {
        if (slotType == null) {
            return false;
        }
        return compatibleSlotSet.contains(slotType);
    }

    // WHY THIS FUNCTION: Check karne ke liye ki gaadi apni pasand ke slot me hai ya fallback slot me (e.g. EV in Regular -> no charging fee).
    public boolean isFallback(SlotType slotType) {
        return slotType != preferredSlotType;
    }
}
