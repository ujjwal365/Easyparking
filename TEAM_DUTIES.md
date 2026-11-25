# 👥 TEAM_DUTIES.md — Student Project Work Division & Viva Guide

> **Project:** Smart Parking Garage (Multi-Storey Automated Car Parking System)  
> **Target Audience:** 3 Computer Science / IT Engineering Students presenting to examiners.  
> **Structure:** Each member owns one distinct tier of the system (Backbone, Brain, and Face).

---

# 👤 MEMBER 1: Backbone (Data Architecture & State Management)

### 📁 Files Owned:
1. `src/main/java/member1/VehicleType.java`
2. `src/main/java/member1/SlotType.java`
3. `src/main/java/member1/Vehicle.java`
4. `src/main/java/member1/ParkingSlot.java`
5. `src/main/java/member1/Ticket.java`
6. `src/main/java/member1/ParkingGarage.java`

---

### 🔑 Key Concepts to Explain in Viva:
1. **2D Array Representation (`ParkingSlot[][] grid`)**:
   - Represents the physical building structure (rows = floors, columns = slots on that floor).
   - Allows direct indexing: `grid[floor][slotIndex]`.
2. **`HashMap<String, Ticket>` for $O(1)$ Lookup**:
   - When a vehicle exits or is searched by license plate, the hash map provides instant constant-time lookup without scanning the entire building.
3. **`ArrayList<Ticket>` for Dynamic Audit History**:
   - Stores an append-only, resizable record of all completed transactions for financial auditing.
4. **Encapsulation & Immutability**:
   - `Vehicle` has immutable fields (`licensePlate`, `type`).
   - `ParkingSlot` strictly controls who parks and vacates via `park()` and `vacate()`.
5. **Separation of Concerns**:
   - `ParkingGarage` is a state coordinator; it delegates slot selection to `SlotAllocator` and tariff computation to `FeeCalculator`.

---

1. **Nearest-Free-Slot-To-Exit Algorithm**:
   - Distance formula: $\text{Distance} = (\text{Floor} \times 20) + \text{SlotIndex}$.
   - Heavy floor penalty ($20$) reflects the real-world cost and time of climbing ramps.
2. **Pure Functional Design**:
   - `SlotAllocator` is a pure calculation engine: it inspects the grid and returns a slot candidate, but **never** modifies the slot state or parks a vehicle.
3. **Time Complexity Analysis**:
   - Current linear scan: $O(F \times S)$ where $F = 3$ floors, $S = 10$ slots (30 iterations maximum).
   - PriorityQueue alternative: Using min-heaps for $O(1)$ retrieval and $O(\log N)$ updates in massive facilities.
4. **Time & Duration Arithmetic (`java.time`)**:
   - Uses `java.time.Duration` and `java.time.LocalDateTime` for daylight and leap-year safe calculations.
   - `Math.ceil` for rounding partial hours up to full billable hours.
5. **Composite Fee Structure & EV Charging**:
   - First 30 minutes free. Daily 24-hour caps applied across multi-day stays.
   - EV Charging connection fee (₹60 flat + ₹15/hr) applied **only** when parked in designated `EV_CHARGING` slots, dropping to ₹0 if parked in fallback regular slots.

---


# 👤 MEMBER 3: Face (UI, Web Dashboard, Validation, Exceptions & QA)

### 📁 Files Owned:
1. `src/main/java/member3/GaragePrinter.java`
2. `src/main/java/member3/Main.java`
3. `src/main/java/member3/WebServer.java` (Built-in Java HTTP Server & REST API)
4. `src/main/resources/web/index.html` (Single-Page Interactive Web Dashboard)
5. `src/main/java/member3/InputValidator.java`
6. `src/main/java/member3/GarageFullException.java`
7. `src/main/java/member3/VehicleNotFoundException.java`
8. `src/main/java/member3/DuplicateVehicleException.java`
9. `src/main/java/member3/InvalidInputException.java`
10. `src/test/java/garage/FeeCalculatorTest.java`
11. `src/test/java/garage/SlotAllocatorTest.java`
12. `src/test/java/garage/ParkingGarageTest.java`
13. `README.md`, `DEMO_SCRIPT.md`

---

1. **Terminal UI & Visual ASCII Art with ANSI Codes**:
   - `GaragePrinter` draws multi-storey floors with symbols (`[s]`, `[ ]`, `[L]`, `[⚡]`, `[B]`, `[C]`, `[S]`, `[E]`).
   - Uses ANSI colors for status with safe fallback for legacy command terminals.
2. **Robust Input Validation & Regex**:
   - Enforces the official Indian License Plate format: `^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$`.
   - Strips whitespace and hyphens, capitalizes letters, and catches bad inputs without crashing.
3. **Custom Exception Hierarchy**:
   - Created specific domain exceptions (`DuplicateVehicleException`, `VehicleNotFoundException`, etc.) extending `Exception` to produce meaningful user-friendly error messages.
4. **Automated Scripted Demonstration**:
   - Option 6 simulates 10 vehicles, exercises all vehicle types, saturates EV bays to prove fallback, and produces realistic receipts.
5. **JUnit 5 Automated Testing**:
   - 20 unit tests covering boundary conditions: free grace period, rounding, daily caps, allocation distance, and full garage error states.

---
