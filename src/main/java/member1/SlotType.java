package member1;

/**
 * SlotType — Garage ke 4 fixed parking bay types ko define karta hai.
 *
 * WHY ENUM (String ki jagah Enum kyu use kiya?):
 *   Agar hum String use karte ("small", "Small", "smal"), toh spelling mistake se bug aa sakta tha.
 *   Enum sirf 4 fixed valid options allow karta hai: SMALL, REGULAR, LARGE, EV_CHARGING.
 */
public enum SlotType {

    SMALL("Small Slot (Bikes)", "[s]"),
    REGULAR("Regular Slot (Cars)", "[ ]"),
    LARGE("Large Slot (SUVs)", "[L]"),
    EV_CHARGING("EV Charging Slot", "[⚡]");

    private final String displayName;
    private final String displaySymbol;

    // WHY CONSTRUCTOR: Har slot type ke saath uska readable naam aur map symbol fixed set karne ke liye.
    SlotType(String displayName, String displaySymbol) {
        this.displayName = displayName;
        this.displaySymbol = displaySymbol;
    }

    // WHY THIS FUNCTION: UI, web dashboard aur printed receipt par readable naam dikhane ke liye (e.g. "Small Slot (Bikes)").
    public String getDisplayName() {
        return displayName;
    }

    // WHY THIS FUNCTION: Console 2D floor map par chhota symbol dikhane ke liye (e.g. "[s]", "[L]", "[⚡]").
    public String getDisplaySymbol() {
        return displaySymbol;
    }

    // WHY THIS FUNCTION: Easily check karne ke liye ki yeh slot EV charge kar sakta hai ya nahi (EV fee rule me kaam aata hai).
    public boolean isEVCharging() {
        return this == EV_CHARGING;
    }

    // WHY THIS FUNCTION: Jab SlotType ko direct print karein toh clean name + symbol dikhe.
    @Override
    public String toString() {
        return displayName + " " + displaySymbol;
    }
}
