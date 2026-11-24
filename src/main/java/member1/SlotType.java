package member1;

/**
 * SlotType — Parking bay ke 4 types.
 *
 * SMALL       → Bikes ke liye
 * REGULAR     → Cars ke liye
 * LARGE       → SUVs ke liye
 * EV_CHARGING → Electric vehicles ke liye (charging wala)
 */
public enum SlotType {

    SMALL("Small Slot (Bikes)", "[s]"),
    REGULAR("Regular Slot (Cars)", "[ ]"),
    LARGE("Large Slot (SUVs)", "[L]"),
    EV_CHARGING("EV Charging Slot", "[⚡]");

    private final String displayName;
    private final String displaySymbol;

    SlotType(String displayName, String displaySymbol) {
        this.displayName = displayName;
        this.displaySymbol = displaySymbol;
    }

    public String getDisplayName()   { return displayName; }
    public String getDisplaySymbol() { return displaySymbol; }

    // Quick check — kya yeh EV charging slot hai?
    public boolean isEVCharging() { return this == EV_CHARGING; }

    @Override
    public String toString() { return displayName + " " + displaySymbol; }
}
