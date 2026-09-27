# Autonomous Electric Heavy Haul Mining Truck — Charging & Dispatch System

> Project brief for kickoff in Antigravity. Frontend: HTML/CSS/JS. Backend: Java (Spring Boot). Copy this file into the project root as `PROJECT_SPEC.md` and use it as the working spec.

---

## 1. Project Summary

An industrial system that:
- Tracks autonomous electric haul trucks (location, battery state-of-charge, status).
- Auto-dispatches trucks to megawatt pantograph charging stations when battery < 20%.
- Bills mining concession operators for tonnage hauled.
- Runs a full mini-ERP: Contacts, Products, Chart of Accounts, Journals, Purchase/Sales cycle, Budgets, Analytic Accounts, and financial reports (Balance Sheet, P&L, Budget Report).


Actors: **Admin** (Mine Transport Director), **Invoicing User** (Fleet Accountant), **System** (automated battery monitor + dispatcher + ledger poster).

---

## 2. Tech Stack

| Layer | Choice |
|---|---|
| Frontend | HTML5 + CSS3 + Vanilla JS (fetch API). Bootstrap 5 via CDN for layout/components. No build step, so Antigravity can serve it as static files. |
| Backend | Java 17, Spring Boot 3.x, Spring Web, Spring Data JPA, Spring Security + JWT, Bean Validation |
| Database | MySQL (`jdbc:mysql://localhost:3306/haulsys`) |
| Build | Maven |
| API style | REST + JSON, versioned under `/api/v1` |
| Scheduling | Spring `@Scheduled` for battery-check / auto-dispatch job |
| Docs | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |

---

## 3. High-Level Architecture

```
[ Browser: HTML/CSS/JS dashboard ]
              |  REST/JSON (fetch, JWT bearer)
              v
[ Spring Boot API layer: Controllers ]
              v
[ Service layer: business rules, dispatch engine, ledger posting ]
              v
[ Repository layer: Spring Data JPA / Hibernate ]
              v
[ MySQL: master data, transactions, ledgers, budgets, auto-increment PKs ]

Background: @Scheduled BatteryMonitorJob -> scans trucks -> creates ChargeSession + Dispatch when SoC < 20%
```

Package structure (`com.mine.haulsys`):
```
config/        security, CORS, OpenAPI config
domain/        JPA entities
repository/    Spring Data repositories
service/       business logic (dispatch, billing, ledger, budget)
controller/    REST controllers
dto/           request/response DTOs
scheduler/     BatteryMonitorJob
exception/     global exception handling
```

---

## 4. Roles & Security

- `ADMIN` — full access: master data, routes, charger config, financial audit.
- `INVOICING` — Contacts, Sales Order, Customer Invoice, Vendor Bill, Payments, Purchase Order.
- `SYSTEM` — internal service account used by the scheduler for dispatch + ledger postings.

Auth: JWT bearer token issued at `POST /api/v1/auth/login`, role-based `@PreAuthorize` on controllers.

---

## 5. Core Domain Entities

### 5.1 Fleet & Charging
**Truck**
- `id`, `truckCode`, `model`, `batteryCapacityKwh`, `currentSoCPercent`, `status` (IDLE, HAULING, EN_ROUTE_TO_CHARGE, CHARGING, MAINTENANCE), `currentLat`, `currentLng`, `assignedRouteId`

**ChargingStation**
- `id`, `stationCode`, `name`, `maxPowerKw`, `location`, `status` (AVAILABLE, OCCUPIED, OFFLINE)

**ChargeSession**
- `id`, `truckId`, `stationId`, `startTime`, `endTime`, `startSoC`, `endSoC`, `energyDeliveredKwh`, `costAmount`

**HaulDispatch**
- `id`, `truckId`, `routeId`, `originPit`, `destinationDump`, `tonnageMoved`, `startTime`, `endTime`, `status` (IN_PROGRESS, COMPLETED, DIVERTED_TO_CHARGE), `distanceKm`

**HaulRoute**
- `id`, `routeName`, `originPit`, `destinationDump`, `distanceKm`, `analyticAccountId`

### 5.2 Master Data
**Contact**
- `id`, `type` (CUSTOMER, VENDOR), `name`, `contactPerson`, `email`, `phone`, `address`, `taxId`

**Product**
- `id`, `name`, `type` (SERVICE, GOODS), `sku`, `unitOfMeasure` (TON_KM, SESSION, UNIT), `unitPrice`, `linkedAccountId`
  - Examples: *Heavy Ore Haulage Ton-km* (Service), *Megawatt Pantograph Charge Session* (Service), *Haul Truck Drive Motor* (Goods)

**Account** (Chart of Accounts)
- `id`, `code`, `name`, `type` (ASSET, LIABILITY, INCOME, EXPENSE, EQUITY), `parentAccountId`
  - Assets: Electric Haul Truck Fleet, Pantograph Chargers, Cash/Bank
  - Liabilities: Vehicle Supplier Payables, Power Utility Creditors
  - Income: Haulage Transport Revenue
  - Expenses: Fleet Charging Power Costs, Heavy Tire & Motor Replacement Expenses

**Journal**
- `id`, `name`, `type` (SALES, PURCHASE, CASH, BANK)

**JournalEntry / JournalLine**
- Entry: `id`, `journalId`, `date`, `reference`, `sourceDocType`, `sourceDocId`
- Line: `id`, `entryId`, `accountId`, `debit`, `credit`, `analyticAccountId`

### 5.3 Transactions
**PurchaseOrder** → `id`, `vendorId`, `productId`, `qty`, `unitPrice`, `status` (DRAFT, CONFIRMED, BILLED)
**VendorBill** → `id`, `poId`, `vendorId`, `amount`, `dueDate`, `status` (OPEN, PAID)
**SalesOrder** → `id`, `customerId`, `productId`, `qty`, `unitPrice`, `period`, `status`
**CustomerInvoice** → `id`, `soId`, `customerId`, `tonnageBilled`, `amount`, `dueDate`, `status` (OPEN, PAID, OVERDUE)
**Payment** → `id`, `direction` (IN, OUT), `relatedInvoiceId`/`relatedBillId`, `method` (BANK, CASH), `amount`, `date`

### 5.4 Budgeting
**AnalyticAccount** — `id`, `name` (e.g., *Open Pit Transport Zone 1*)
**Budget** — `id`, `analyticAccountId`, `accountId`, `period`, `plannedAmount`, `actualAmount` (computed from journal lines)

---

## 6. Core Business Logic

1. **Battery Monitor & Auto-Dispatch** (`BatteryMonitorJob`, runs every N minutes):
   - Scan all trucks with status `HAULING`.
   - If `currentSoCPercent < 20`, find nearest `AVAILABLE` `ChargingStation`, create a `ChargeSession`, set truck status to `EN_ROUTE_TO_CHARGE`, mark station `OCCUPIED`.
   - On charge completion, update `endSoC`, `energyDeliveredKwh`, `costAmount`, post a journal entry debiting *Fleet Charging Power Costs* / crediting *Power Utility Creditors* or *Cash/Bank*.

2. **Haul Billing**:
   - `HaulDispatch.tonnageMoved` accumulated per customer/period → generates `CustomerInvoice` using *Heavy Ore Haulage Ton-km* product rate.
   - Posts journal entry: debit *Vehicle Supplier Payables* n/a; for sales: debit Accounts Receivable (or Cash/Bank on payment), credit *Haulage Transport Revenue*.

3. **Purchase-to-Pay** (tires/motors):
   - `PurchaseOrder` → `VendorBill` → `Payment` (Bank). Posts debit *Heavy Tire & Motor Replacement Expenses*, credit *Vehicle Supplier Payables*; on payment, debit Payables, credit Cash/Bank.

4. **Budget vs Actuals**:
   - `Budget.actualAmount` = sum of journal lines tagged with matching `analyticAccountId` + `accountId` within the period.

---

## 7. REST API Endpoints

### Auth
- `POST /api/v1/auth/login`

### Fleet & Dispatch
- `POST /api/v1/haul/trucks` · `GET /api/v1/haul/trucks` · `GET/PUT/DELETE /api/v1/haul/trucks/{id}`
- `POST /api/v1/haul/chargers` · `GET /api/v1/haul/chargers`
- `POST /api/v1/haul/dispatches` — logs a haul; triggers auto-route check
- `GET /api/v1/haul/dispatches` · `GET /api/v1/haul/dispatches/{id}`
- `GET /api/v1/haul/charge-sessions`

### Master Data
- `POST/GET /api/v1/contacts`
- `POST/GET /api/v1/products`
- `POST/GET /api/v1/accounts` (Chart of Accounts)
- `POST/GET /api/v1/journals`

### Transactions
- `POST/GET /api/v1/purchase-orders`
- `POST /api/v1/purchase-orders/{id}/convert-to-bill`
- `POST/GET /api/v1/vendor-bills`
- `POST /api/v1/vendor-bills/{id}/pay`
- `POST/GET /api/v1/sales-orders`
- `POST /api/v1/sales-orders/{id}/convert-to-invoice`
- `POST/GET /api/v1/customer-invoices`
- `POST /api/v1/customer-invoices/{id}/collect-payment`
- `POST/GET /api/v1/payments`

### Budgeting
- `POST/GET /api/v1/analytic-accounts`
- `POST/GET /api/v1/budgets`
- `GET /api/v1/budgets/{id}/variance`

### Reports
- `GET /api/v1/reports/balance-sheet?asOf=YYYY-MM-DD`
- `GET /api/v1/reports/profit-and-loss?from=&to=`
- `GET /api/v1/reports/budget?analyticAccountId=&period=`

---

## 8. Frontend Pages (static HTML/CSS/JS)

```
frontend/
  index.html            login
  dashboard.html         fleet overview: truck list, SoC gauges, charger status
  dispatch.html          log/view haul dispatches
  masters.html           tabs: Contacts, Products, Chart of Accounts
  purchase.html          PO -> Vendor Bill -> Payment flow
  sales.html              Sales Order -> Customer Invoice -> Payment flow
  budgets.html            analytic accounts + budget vs actual table
  reports.html            Balance Sheet, P&L, Budget Report (render as tables/charts)
  /assets/js/api.js       fetch wrapper with JWT header
  /assets/js/*.js         per-page logic
  /assets/css/style.css
```

Use `fetch()` against the Spring Boot API (enable CORS for the static origin during dev, e.g. `http://localhost:5500`).

---

## 9. Suggested Folder Structure (full repo)

```
mining-haul-dispatch/
  backend/
    src/main/java/com/mine/haulsys/...
    src/main/resources/application.yml
    pom.xml
  frontend/
    (pages listed above)
  PROJECT_SPEC.md   <- this file
  README.md
```

---

## 10. Sample Code Starters

**Truck.java**
```java
@Entity
public class Truck {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String truckCode;
    private String model;
    private Double batteryCapacityKwh;
    private Double currentSoCPercent;

    @Enumerated(EnumType.STRING)
    private TruckStatus status;

    private Double currentLat;
    private Double currentLng;
    // getters/setters
}
```

**BatteryMonitorJob.java**
```java
@Component
public class BatteryMonitorJob {
    private final TruckRepository truckRepo;
    private final DispatchService dispatchService;

    @Scheduled(fixedRate = 60000)
    public void checkBatteries() {
        truckRepo.findByStatus(TruckStatus.HAULING).stream()
            .filter(t -> t.getCurrentSoCPercent() < 20.0)
            .forEach(dispatchService::routeToNearestCharger);
    }
}
```

**HaulTruckController.java**
```java
@RestController
@RequestMapping("/api/v1/haul/trucks")
public class HaulTruckController {
    private final TruckService truckService;

    @PostMapping
    public ResponseEntity<TruckDto> create(@Valid @RequestBody TruckDto dto) {
        return ResponseEntity.ok(truckService.create(dto));
    }

    @GetMapping
    public List<TruckDto> list() { return truckService.findAll(); }
}
```

**frontend/assets/js/api.js**
```javascript
const API_BASE = "http://localhost:8080/api/v1";
async function apiFetch(path, options = {}) {
  const token = localStorage.getItem("jwt");
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return res.json();
}
```

---

## 11. Setup & Run

**Backend**
```bash
cd backend
mvn spring-boot:run
# API on http://localhost:8080, Swagger on /swagger-ui.html
```

**Database (Postgres via Docker, optional)**
```bash
docker run --name haul-db -e POSTGRES_DB=hauldb -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15
```

**Frontend**
```bash
cd frontend
npx serve .    # or any static server, e.g. VS Code Live Server
```

---

## 12. Development Milestones (recommended build order for Antigravity)

1. Scaffold Spring Boot project (deps: Web, JPA, Security, Validation, Postgres/H2, springdoc).
2. Build entities + repositories for Truck, ChargingStation, ChargeSession, HaulDispatch, HaulRoute.
3. Build Master Data entities: Contact, Product, Account, Journal + CRUD controllers.
4. Implement JWT auth + role guards.
5. Implement Dispatch API + BatteryMonitorJob auto-routing logic.
6. Implement Transaction flow: PurchaseOrder → VendorBill → Payment; SalesOrder → CustomerInvoice → Payment, with journal-posting side effects.
7. Implement AnalyticAccount + Budget entities and variance calculation.
8. Implement Reports endpoints (Balance Sheet, P&L, Budget Report) as aggregation queries.
9. Build static frontend pages consuming the API (start with dashboard.html + dispatch.html).
10. Wire up reports.html with simple tables/charts (e.g., Chart.js via CDN).
11. Seed demo data (trucks, chargers, a few contacts/products/CoA) via a `CommandLineRunner` or SQL seed script.

---

## 13. Non-Functional Requirements

- All monetary values stored as `BigDecimal` (never float/double).
- Every transaction that touches money must generate a balanced `JournalEntry` (sum debit = sum credit).
- API responses paginated for list endpoints (`page`, `size`).
- Log all auto-dispatch events for audit (truck id, station id, SoC at trigger, timestamp).
- Basic input validation on all POST/PUT via Bean Validation annotations.
