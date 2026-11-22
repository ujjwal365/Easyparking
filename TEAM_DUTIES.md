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

### ❓ Top 5 Viva Questions & Crisp Answers for Member 1:

#### Q1: Why did you use a 2D Array for the garage instead of a flat List?
> **Answer (English):** A 2D array naturally maps to the physical architecture of a multi-storey building where `grid[floor][slot]` represents floor levels and parking bays. It provides instant coordinate access with zero object overhead.  
> **उत्तर (Hindi):** 2D Array multi-storey building ke structure ko natural tarike se darshata hai, jisme pehla dimension floor aur doosra dimension slot index hota hai. Isse coordinates ke hisaab se seedha access milta hai.

#### Q2: What is the purpose of having both a `HashMap` and an `ArrayList` in `ParkingGarage`?
> **Answer (English):** The `HashMap<String, Ticket>` stores *active* vehicles keyed by license plate for instantaneous $O(1)$ lookup during checkout or search. The `ArrayList<Ticket>` stores *completed historical tickets* sequentially for reporting and audit logs.  
> **उत्तर (Hindi):** Active gaadiyon ko number plate se turant $O(1)$ time me dhoondhne ke liye `HashMap` use kiya hai, aur checkout ho chuki purani gaadiyon ka audit record maintain karne ke liye `ArrayList` use kiya hai.

#### Q3: How do you prevent two cars with the same license plate from entering?
> **Answer (English):** In `ParkingGarage.parkVehicle()`, we query `activeTickets.containsKey(plate)`. If true, we immediately throw a custom `DuplicateVehicleException` and abort the transaction before any slot is allocated.  
> **उत्तर (Hindi):** `activeTickets.containsKey(plate)` check karta hai ki kya yeh plate pehle se garage ke andar active hai. Agar hai, toh `DuplicateVehicleException` throw kar di jaati hai.

#### Q4: Where are the compatibility rules between vehicle types and slot types defined?
> **Answer (English):** In `VehicleType.java`. Placing compatibility inside the `VehicleType` enum centralizes all business rules in one place, adhering to the Single Source of Truth principle.  
> **उत्तर (Hindi):** Saare rules `VehicleType` enum me centralized hain. Isse code clean rehta hai aur naye rules add karne ke liye sirf ek jagah change karna padta hai.

#### Q5: What happens internally when a vehicle departs (`exitVehicle`)?
> **Answer (English):** 1) We fetch the ticket from `activeTickets`. 2) Calculate the fee using `FeeCalculator`. 3) Call `slot.vacate()`. 4) Stamp exit time on the ticket. 5) Remove it from `activeTickets` and push it to `ticketHistory`. 6) Add fee to `totalRevenue`.  
> **उत्तर (Hindi):** activeTickets se ticket nikalte hain, FeeCalculator se fee banate hain, slot ko `vacate()` karke khaali karte hain, ticket close karke history list me move karte hain aur total revenue update karte hain.

---

# 👤 MEMBER 2: Brain (Core Algorithms & Pricing Logic)

### 📁 Files Owned:
1. `src/main/java/member2/SlotAllocator.java`
2. `src/main/java/member2/FeeCalculator.java`
3. `src/main/java/member2/FeeBreakdown.java`

---

### 🔑 Key Concepts to Explain in Viva:
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

### ❓ Top 5 Viva Questions & Crisp Answers for Member 2:

#### Q1: Explain the distance formula used in `SlotAllocator`. Why is `FLOOR_PENALTY` set to 20?
> **Answer (English):** Formula: `Distance = (floor * 20) + slotIndex`. Exit is at `(0, 0)`. Since each floor has 10 slots (0 to 9), a floor penalty of 20 ensures any slot on Floor 0 (max distance 9) is strictly preferred over Floor 1 Slot 0 (distance 20). It mirrors real drivers preferring ground floor over climbing ramps.  
> **उत्तर (Hindi):** Exit ground floor par hai (0, 0). Floor penalty 20 isliye rakhi hai taaki Ground Floor ka sabse aakhri slot (distance 9) bhi Floor 1 ke pehle slot (distance 20) se pehle bhare, kyunki ramp chadhna drivers avoid karte hain.

#### Q2: How does the EV slot fallback mechanism work?
> **Answer (English):** The allocator first searches for free `EV_CHARGING` slots closest to the exit. If all EV bays are full, it falls back to finding the nearest free `REGULAR` or `LARGE` slot, logs a notification to the driver, and ensures `FeeCalculator` waives the EV charging fee.  
> **उत्तर (Hindi):** Pehle allocator EV_CHARGING slot dhoondta hai. Agar saare EV slots full hon, toh fallback karke Regular ya Large slot de deta hai aur screen par notice print karta hai. Fallback slot me EV charging fee ₹0 lagti hai.

#### Q3: Why is `SlotAllocator` designed as a pure function?
> **Answer (English):** Separation of Concerns and safety. An allocator should only make decisions; it should never have side-effects like mutating grid data or altering vehicle references. This makes it trivial to unit test and thread-safe.  
> **उत्तर (Hindi):** Pure function hone se koi side-effect nahi hota. Allocator sirf best slot dhoondh ke batata hai, grid me changes nahi karta. Isse unit testing aasan aur safe ho jaati hai.

#### Q4: How could this algorithm be optimized for a garage with 10,000 slots?
> **Answer (English):** Instead of a linear scan of $O(F \times S)$, we could maintain a `PriorityQueue<ParkingSlot>` (min-heap) for each slot type ordered by distance. Allocating a slot would be $O(1)$ peek or $O(\log N)$ poll, and vacating would be $O(\log N)$ insertion.  
> **उत्तर (Hindi):** Agar 10,000 slots hon toh har slot par `PriorityQueue` (Min-Heap) use karenge jo distance ke hisaab se ordered hogi. Isse slot allocation $O(1)$ time me ho jayegi.

#### Q5: How does the FeeCalculator handle multi-day stays (e.g. 26 hours for a Car)?
> **Answer (English):** We calculate full 24-hour days (`26 / 24 = 1 day`) and remaining hours (`26 % 24 = 2 hours`). Day 1 hits the Car daily cap of ₹250. The remaining 2 hours cost $2 \times 30 = \text{₹}60$. Total parking fee = ₹250 + ₹60 = ₹310.  
> **उत्तर (Hindi):** 26 hours ko 24 hours ke din aur bache hue ghanton me baanta jata hai: Pehla din car ke daily cap (₹250) par freeze ho jata hai, aur bache 2 ghanton ka ₹60 lagta hai. Total ₹310 banta hai.

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

### 🔑 Key Concepts to Explain in Viva:
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

### ❓ Top 5 Viva Questions & Crisp Answers for Member 3:

#### Q1: Explain the regex used in `InputValidator` for Indian license plates.
> **Answer (English):** The regex is `^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$`.  
> - `^[A-Z]{2}`: 2 state letters (e.g., MH, DL, KA).  
> - `\d{2}`: 2 RTO district digits (e.g., 12, 01).  
> - `[A-Z]{1,2}`: 1 or 2 series alphabet letters (e.g., AB, Q).  
> - `\d{4}$`: 4 unique vehicle digits (e.g., 1234).  
> **उत्तर (Hindi):** Yeh regex Indian number plate ke format ko check karta hai: 2 State code letters, 2 RTO digits, 1-2 series letters, aur aakhri 4 unique digits.

#### Q2: How does the application prevent crashing if a user enters non-numeric text for a menu choice?
> **Answer (English):** In `InputValidator.parseMenuChoice()`, we read raw text as a String, trim it, and parse it inside a `try-catch(NumberFormatException)` block. If invalid, we throw our custom `InvalidInputException` with a friendly message, allowing the menu loop to continue safely.  
> **उत्तर (Hindi):** Input ko direct integer me parse karne ke bajaye `parseMenuChoice` method me try-catch lagaya hai. Agar user alphabet daalta hai toh `InvalidInputException` throw hoti hai aur program crash hone se bach jata hai.

#### Q3: Why is there no business logic inside `Main.java` or `GaragePrinter.java`?
> **Answer (English):** Adhering to the Separation of Concerns and MVC (Model-View-Controller) principles. The UI layer should only handle user interaction and presentation; all core operations belong to `ParkingGarage`.  
> **उत्तर (Hindi):** Clean architecture aur separation of concerns ke liye. UI ka kaam sirf input lena aur output dikhana hai. Saara business logic `ParkingGarage` ke andar bandh hai.

#### Q4: How does the automated scripted demo in Option 6 prove all requirements work?
> **Answer (English):** It parks 10 mixed vehicles including 7 EVs. Because there are only 6 EV slots across 3 floors, the 7th EV is forced into a regular slot, triggering and printing the fallback message. It then checks out a 3-hour car and a 26-hour SUV to show rounding and multi-day daily cap receipts.  
> **उत्तर (Hindi):** Demo me 10 gaadiyan aati hain jisme 7 EVs hain. 6 EV slots bharne ke baad 7th EV regular slot me jati hai aur fallback notice print hota hai. Phir 3 ghante aur 26 ghante ke bills print karke fee calculation prove ki jati hai.

#### Q5: What is the benefit of writing 20 JUnit 5 unit tests for this project?
> **Answer (English):** Automated unit tests guarantee that edge cases (like the exact 30-minute grace boundary, 31-minute ceiling rounding, and duplicate plate rejection) work correctly without manual manual testing, preventing regressions.  
> **उत्तर (Hindi):** JUnit tests se yeh confirm hota hai ki saare edge cases (jaise 30 minute free limit, 31st minute ka round up, duplicate plate check) bina kisi bug ke 100% sahi chal rahe hain.

