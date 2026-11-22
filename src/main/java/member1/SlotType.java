package member1;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - SlotType Enum
 * ============================================================================
 * Yeh enum parking garage ke alag-alag slot types ko define karta hai.
 * Garage me 4 types ke slots hain:
 * 1. SMALL       -> Bike ke liye chhota slot [s]
 * 2. REGULAR     -> Car ke liye standard slot [ ]
 * 3. LARGE       -> SUV ke liye bada slot [L]
 * 4. EV_CHARGING -> Electric Vehicle ke liye charging wala slot [⚡]
 * ============================================================================
 */
public enum SlotType {
    // Chhoti gaadiyon (Bike) ke liye slot
    SMALL("Small Slot (Bikes)", "[s]"),

    // Aam gaadiyon (Car) ke liye normal slot
    REGULAR("Regular Slot (Cars)", "[ ]"),

    // Badi gaadiyon (SUV) ke liye bada slot
    LARGE("Large Slot (SUVs)", "[L]"),

    // Electric gaadiyon ko charge karne wala slot
    EV_CHARGING("EV Charging Slot", "[⚡]");

    private final String displayName;
    private final String displaySymbol;

    SlotType(String displayName, String displaySymbol) {
        this.displayName = displayName;
        this.displaySymbol = displaySymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplaySymbol() {
        return displaySymbol;
    }
}

