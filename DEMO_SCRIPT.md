# 🎬 DEMO_SCRIPT.md — 5-Minute Project Presentation Script

> **Project:** Smart Parking Garage (Multi-Storey Automated Car Parking System)  
> **Total Time:** Exactly 5 Minutes  
> **Speakers:** Member 1 (Backbone), Member 2 (Brain), Member 3 (Face)  
> **Setup:** Terminal window open at the project root directory.

---

## ⏱️ Timeline Overview

| Time | Speaker | Topic | Action on Screen |
| :--- | :--- | :--- | :--- |
| **0:00 - 1:00** | **Member 1** | Introduction, System Architecture & Data Model | Show `pom.xml`, explain 2D grid & collections |
| **1:00 - 2:15** | **Member 2** | Nearest Slot Algorithm & Fee Calculation Engine | Explain distance formula & EV billing rules |
| **2:15 - 3:30** | **Member 3** | Console UI, Regex Validation, Exception Handling | Run `mvn test` (show 20 passing unit tests) |
| **3:30 - 4:45** | **All 3** | Live Scripted Demo (Option 6 Showcase) | Run `mvn compile exec:java` or demo CLI |
| **4:45 - 5:00** | **Member 1** | Conclusion & Q&A Open | Concluding remarks to the jury |

---

## 🎤 Detailed Script & Step-by-Step Cues

---

### [0:00 - 1:00] Member 1 (Backbone): Introduction & Architecture

**Cue:** *Member 1 steps forward, project code is visible.*

> **Member 1:**  
> *"Good morning respected professors and jury members. Today, my teammates and I are thrilled to present our Java 17 project: the **Smart Parking Garage**.*  
>  
> *In modern cities, multi-storey parking garages struggle with congestion, ramp bottlenecks, and inefficient billing. Our goal was to build a clean, robust, and automated parking management system using Core Java principles.*  
>  
> *As **Member 1**, I owned the **Backbone** of the application:*  
> *1. **The 2D Array Grid (`ParkingSlot[3][10]`)**: We modeled 3 storeys with 10 parking bays per floor. Each slot is designated for a specific vehicle type—Small slots for bikes, Regular slots for cars, Large slots for SUVs, and EV Charging bays.*  
> *2. **Fast $O(1)$ Hash Mapping**: To achieve instantaneous lookups by license plate during exit or search, we used a `HashMap<String, Ticket> activeTickets`.*  
> *3. **Audit History with `ArrayList`**: An append-only list tracks all completed sessions for revenue audits.*  
> *4. **Clean OOP & Separation of Concerns**: Our data models encapsulate their state, delegating allocation logic to Member 2's algorithm and UI to Member 3.*  
>  
> *Now, I hand over to **Member 2** to explain the brains behind our slot allocation and tariff calculation."*

---

### [1:00 - 2:15] Member 2 (Brain): Algorithms & Pricing Logic

**Cue:** *Member 2 steps forward. Highlights `SlotAllocator.java` and `FeeCalculator.java`.*

> **Member 2:**  
> *"Thank you. As **Member 2**, I engineered the core business logic in `SlotAllocator` and `FeeCalculator`.*  
>  
> *First, our **Nearest-Free-Slot-To-Exit Algorithm**:*  
> *Our garage exit is located at **Floor 0, Slot 0**. When a vehicle enters, drivers prefer parking close to the exit on the ground level rather than climbing ramps.*  
> *We designed a specialized distance formula:*  
> $$\text{Distance} = (\text{Floor} \times 20) + \text{Slot Index}$$  
> *Because `FLOOR_PENALTY` is 20, the algorithm fills ground floor bays before sending any car up the ramp to Floor 1. Our allocator is a **pure function**—it scans the grid, respects vehicle compatibility, and never alters grid state.*  
>  
> *Next, our **EV Fallback Logic**:*  
> *Electric Vehicles prefer designated `EV_CHARGING` bays. But if all charging bays across all floors are occupied, the allocator seamlessly routes the EV to the nearest Regular or Large slot and emits a notice to the driver. In that fallback slot, no charging fee is levied!*  
>  
> *Finally, our **Fee Calculator**:*  
> *- **30-Minute Grace Period**: Stays under 30 minutes are 100% free.*  
> *- **Rounding Up**: Beyond 30 minutes, partial hours round up via `Math.ceil`.*  
> *- **Daily Caps**: Stays over 24 hours apply daily limits (e.g. ₹250/day for cars, ₹400/day for SUVs).*  
> *- **EV Charging**: An EV in a charging slot pays a flat ₹60 connection fee plus ₹15 per billable hour.*  
>  
> *I now pass to **Member 3** to present our UI, validation, and test suite."*

---

### [2:15 - 3:30] Member 3 (Face): UI, Validation & Automated Testing

**Cue:** *Member 3 steps forward. Opens terminal to run unit tests.*

> **Member 3:**  
> *"Thank you. As **Member 3**, I built the user-facing tier—the console UI, input validation, custom exceptions, and our automated test suite.*  
>  
> *1. **Terminal Experience (`GaragePrinter`)**: We developed an ASCII 2D visualizer that renders all three floors with clear slot indicators: `[s]` for Small, `[ ]` for Regular, `[L]` for Large, and `[⚡]` for EV bays, alongside colorized occupancy icons like `[C]`, `[B]`, `[S]`, and `[E]`.*  
> *2. **Input Validation (`InputValidator`)**: We enforce the official Indian license plate format using regex `^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$` (for plates like `MH12AB1234`). We sanitize whitespace and prevent program crashes on bad inputs.*  
> *3. **Custom Exceptions**: We created domain-specific exceptions like `DuplicateVehicleException`, `VehicleNotFoundException`, and `GarageFullException` to give friendly error feedback.*  
>  
> *Let's first run our **20 automated JUnit 5 tests** to verify zero defects."*

**Action:** *Member 3 types:*
```bash
mvn test
# or .\mvn.cmd test
```

> **Member 3 (Pointing to console):**  
> *"As you can see, all 20 tests passed in under 2 seconds! This includes boundary tests for the 30-minute grace period, hourly ceiling rounding, multi-day daily caps, nearest-slot distances, and EV fallbacks.*  
>  
> *Now, let's run our live automated demonstration!"*

---

### [3:30 - 4:45] Live Scripted Demo (Option 6 Showcase)

**Action:** *Member 3 launches the demo command:*
```bash
.\mvn.cmd compile exec:java "-Dexec.args=--demo"
```

**Cue:** *Output begins streaming. Team members explain live sections together.*

> **Member 1 (Pointing to Step 1 & 2):**  
> *"Here in Step 1, our system parks 10 mixed vehicles in real-time. Notice vehicle #10—an Electric Vehicle (`HR26EV4001`). Because all 6 EV charging bays across Floors 0, 1, and 2 were occupied by the previous 6 EVs, our algorithm automatically triggered the fallback:  
> `[NOTICE] No EV slot free, assigned regular slot (no charging).`  
> And placed it into `F0-S03` on the ground floor!"*

> **Member 2 (Pointing to Step 4 & 5):**  
> *"Now observe our checkout receipts:*  
> *- **Receipt 1 (Car `DL01CR2002`)**: Parked for 3 hours. At ₹30/hour, the base parking fee is exactly ₹90.00.*  
> *- **Receipt 2 (SUV `KA05SV3003`)**: Parked for **26 hours**. Rather than blindly multiplying 26 by ₹50 (which would be ₹1300), our daily cap logic kicks in: Day 1 is capped at ₹400, plus 2 hours on Day 2 at ₹50/hr (₹100), totaling exactly ₹500.00!"*

> **Member 3 (Pointing to Step 6):**  
> *"Finally, Step 6 displays our live post-checkout floor map showing slots `F0-S02` and `F0-S07` vacated and available, with an updated financial audit log showing ₹590.00 in cumulative revenue."*

---

### [4:45 - 5:00] Conclusion & Handover to Jury

> **Member 1:**  
> *"To summarize: Our project demonstrates pure Java 17, 2D arrays, HashMaps, ArrayLists, Enums, Time APIs, and OOP separation of concerns without relying on heavy frameworks.*  
>  
> *The entire codebase is clean, thoroughly unit-tested, and documented with bilingual Hindi and English comments.*  
>  
> *Thank you very much! We are now open for any questions."*

---

## 💡 Quick Tips for the Presenting Students:
1. **Be Confident**: Speak clearly and maintain eye contact with the professors.
2. **Stick to Your Role**: When an examiner asks about the algorithm, Member 2 should answer; when asked about HashMap vs ArrayList, Member 1 should answer; when asked about regex or tests, Member 3 should answer.
3. **Keep the Terminal Open**: Keep the demo output visible on your laptop screen so you can point to the receipts and live floor map during questions.

