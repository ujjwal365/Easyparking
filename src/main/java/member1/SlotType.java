package member1;
public enum SlotType {

    SMALL("Small Slot (Bikes)", "[s]"),
    REGULAR("Regular Slot (Cars)", "[ ]"),
    LARGE("Large Slot (SUVs)", "[L]"),
    EV_CHARGING("EV Charging Slot", "[⚡]");

    private final String displayName;
    private final String displaySymbol;

    //Har slot type ke saath uska readable naam aur map symbol fixed set karne ke liye.
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
