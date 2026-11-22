package member1;

/**
 * ============================================================================
 * MEMBER 1 (Backbone) - SlotType Enum
 * ============================================================================
 * Garage ke andar parking bays (slots) ke 4 standard types hote hain:
 *
 * 1. SMALL       -> Bike / Two-Wheeler ke liye chhota slot  [s]
 * 2. REGULAR     -> Standard Cars / Sedan / Hatchback      [ ]
 * 3. LARGE       -> SUV / Badi gaadiyon ke liye bada slot  [L]
 * 4. EV_CHARGING -> Electric Vehicle with charging socket  [⚡]
 * ============================================================================
 */
public enum SlotType {

    // 1. Chhoti gaadiyon (Bikes) ke liye bay
    SMALL("Small Slot (Bikes)", "[s]"),

    // 2. Normal family cars (Sedan / Hatchback) ke liye bay
    REGULAR("Regular Slot (Cars)", "[ ]"),

    // 3. Heavy / Large vehicles (SUVs) ke liye bay
    LARGE("Large Slot (SUVs)", "[L]"),

    // 4. Electric gaadiyon ke liye dedicated charging bay
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

    public boolean isEVCharging() {
        return this == EV_CHARGING;
    }

    @Override
    public String toString() {
        return displayName + " " + displaySymbol;
    }
}
