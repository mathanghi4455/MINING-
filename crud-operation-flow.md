# CRUD Operation Flow — Mining Haul Dispatch & Charging System

> Companion to `mining-truck-dispatch-system.md` and `page-to-page-workflow.md`. For every entity: where it is **fetched from** (Read), where it is **created**, where it is **updated**, where it is **deleted**, and what must be fetched first to populate that form.

---

## 1. How to Read This Document

Each module below has:
- **Fetch (Read)** — which page loads it, when, and the endpoint.
- **Create** — which page has the "New ___" action, what it needs pre-fetched for dropdowns, the endpoint, and what refreshes after.
- **Update** — where edits happen and the endpoint.
- **Delete** — where, the endpoint, and any guard rails (can't delete if referenced elsewhere).

---

## 2. Master Data Modules

### 2.1 Contact (Customer / Vendor)
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `masters.html` → Contacts tab, on tab load | `GET /api/v1/contacts?type=` | Also fetched (dropdown-only, minimal fields) by `sales.html` (type=CUSTOMER) and `purchase.html` (type=VENDOR) each time those pages load |
| Fetch (single) | `masters.html`, on row click → edit modal | `GET /api/v1/contacts/{id}` | |
| Create | `masters.html` → "New Contact" button | `POST /api/v1/contacts` | No pre-fetch needed. On success: refresh Contacts list, and any open dropdown elsewhere on next fetch |
| Update | `masters.html` → edit modal → Save | `PUT /api/v1/contacts/{id}` | |
| Delete | `masters.html` → row delete icon | `DELETE /api/v1/contacts/{id}` | Blocked (409) if referenced by any Sales Order, Purchase Order, or Invoice/Bill — API returns list of referencing docs |

### 2.2 Product
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `masters.html` → Products tab | `GET /api/v1/products?type=` | Also fetched by `sales.html` (SERVICE products) and `purchase.html` (GOODS products) for line-item dropdown |
| Fetch (single) | `masters.html`, edit modal | `GET /api/v1/products/{id}` | |
| Create | `masters.html` → "New Product" | `POST /api/v1/products` | Requires Chart of Accounts already fetched (to pick `linkedAccountId`) — fetch `GET /api/v1/accounts` to populate that dropdown first |
| Update | `masters.html` → edit modal → Save | `PUT /api/v1/products/{id}` | |
| Delete | `masters.html` → row delete icon | `DELETE /api/v1/products/{id}` | Blocked if used on any SO/PO line |

### 2.3 Account (Chart of Accounts)
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list, tree) | `masters.html` → Chart of Accounts tab | `GET /api/v1/accounts` | Also fetched by `budgets.html` (GL account dropdown) and `reports.html` (to render Balance Sheet / P&L groupings) |
| Fetch (single) | `masters.html`, edit modal | `GET /api/v1/accounts/{id}` | |
| Create | `masters.html` → "New Account" | `POST /api/v1/accounts` | If it's a sub-account, fetch parent accounts list first to populate `parentAccountId` dropdown |
| Update | `masters.html` → edit modal → Save | `PUT /api/v1/accounts/{id}` | Changing `type` (Asset/Liability/etc.) should warn if journal lines already posted against it |
| Delete | `masters.html` → row delete icon | `DELETE /api/v1/accounts/{id}` | Blocked if any `JournalLine`, `Product.linkedAccountId`, or `Budget` references it |

### 2.4 Journal
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `masters.html` (optional "Journals" sub-tab) or admin config page | `GET /api/v1/journals` | Also fetched internally by services when posting entries (Sales→SALES journal, Purchase→PURCHASE journal, Payments→BANK/CASH journal) |
| Create | Admin config (typically seeded once at setup, rarely user-created) | `POST /api/v1/journals` | Seed once via `CommandLineRunner`: SALES, PURCHASE, CASH, BANK |
| Update | Admin config | `PUT /api/v1/journals/{id}` | |
| Delete | Admin config | `DELETE /api/v1/journals/{id}` | Blocked if any JournalEntry references it |

---

## 3. Fleet & Dispatch Modules

### 3.1 Truck
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `dashboard.html`, on load | `GET /api/v1/haul/trucks` | Also fetched by `dispatch.html` (truck dropdown, filtered to `IDLE`/`HAULING`) |
| Fetch (single) | `dashboard.html` → click truck card | `GET /api/v1/haul/trucks/{id}` | Returns truck + linked recent `HaulDispatch` + `ChargeSession` history |
| Create | `dashboard.html` → "New Truck" | `POST /api/v1/haul/trucks` | No pre-fetch required |
| Update | `dashboard.html` → truck detail → Edit (e.g., correct capacity, set MAINTENANCE) | `PUT /api/v1/haul/trucks/{id}` | SoC/status fields are also updated automatically by the System job — UI should re-fetch, not assume its own cached value is current |
| Delete | `dashboard.html` → truck detail → Delete | `DELETE /api/v1/haul/trucks/{id}` | Blocked if truck has any non-completed `HaulDispatch` or active `ChargeSession` |

### 3.2 ChargingStation
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `dashboard.html`, on load | `GET /api/v1/haul/chargers` | Also fetched internally by `DispatchService.routeToNearestCharger()` (filtered to `AVAILABLE`) |
| Create | `dashboard.html` → "New Charger" | `POST /api/v1/haul/chargers` | No pre-fetch required |
| Update | `dashboard.html` → charger tile → Edit (power limit, status OFFLINE for maintenance) | `PUT /api/v1/haul/chargers/{id}` | |
| Delete | `dashboard.html` → charger tile → Delete | `DELETE /api/v1/haul/chargers/{id}` | Blocked if it has an active `ChargeSession` |

### 3.3 HaulRoute
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `dispatch.html`, on load (route dropdown) | `GET /api/v1/haul/routes` | |
| Create | `dispatch.html` → "New Route" (or in `masters.html` if you prefer routes as master data) | `POST /api/v1/haul/routes` | Requires `AnalyticAccount` list fetched first (`GET /api/v1/analytic-accounts`) to tag the route for budget tracking |
| Update | `dispatch.html` → route edit | `PUT /api/v1/haul/routes/{id}` | |
| Delete | `dispatch.html` → route edit → Delete | `DELETE /api/v1/haul/routes/{id}` | Blocked if any dispatch references it |

### 3.4 HaulDispatch
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `dispatch.html`, on load | `GET /api/v1/haul/dispatches?status=` | Also fetched by `sales.html` when converting SO → Invoice (pulls `COMPLETED` dispatches for a customer/period) |
| Fetch (single) | `dispatch.html` → row click | `GET /api/v1/haul/dispatches/{id}` | |
| Create | `dispatch.html` → "Log New Dispatch" | `POST /api/v1/haul/dispatches` | Requires Truck list + Route list pre-fetched for the form dropdowns. May trigger auto-charge diversion synchronously in the same call |
| Update | `dispatch.html` → row → mark `COMPLETED` / edit tonnage/time | `PUT /api/v1/haul/dispatches/{id}` | Also auto-updated by System job (status → `DIVERTED_TO_CHARGE`) |
| Delete | `dispatch.html` → row delete (rare — usually corrections only) | `DELETE /api/v1/haul/dispatches/{id}` | Blocked once tonnage has been pulled into a `CustomerInvoice` |

### 3.5 ChargeSession
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `dashboard.html` (active sessions) and `dispatch.html` (inline panel on diverted truck) | `GET /api/v1/haul/charge-sessions?active=true` | |
| Create | **Not user-created** — always system-created by `BatteryMonitorJob` / `DispatchService` when SoC < 20% | `POST /api/v1/haul/charge-sessions` (internal call) | No form; UI only displays |
| Update | System job on charge completion (`endTime`, `endSoC`, `energyDeliveredKwh`, `costAmount`) | `PUT /api/v1/haul/charge-sessions/{id}` | Also posts a `JournalEntry` as a side effect |
| Delete | Not exposed in UI | `DELETE /api/v1/haul/charge-sessions/{id}` (admin/API only) | Financial record — deletion should be restricted to Admin + only if no journal entry posted yet |

---

## 4. Transaction Modules

### 4.1 PurchaseOrder
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `purchase.html`, on load | `GET /api/v1/purchase-orders?status=` | |
| Create | `purchase.html` → "New PO" | `POST /api/v1/purchase-orders` | Requires Contact list (type=VENDOR) and Product list (type=GOODS) pre-fetched for the form |
| Update | `purchase.html` → row → Confirm (status DRAFT→CONFIRMED) or edit qty/price while still DRAFT | `PUT /api/v1/purchase-orders/{id}` | Locked for edits once `BILLED` |
| Delete | `purchase.html` → row delete (only while DRAFT) | `DELETE /api/v1/purchase-orders/{id}` | Blocked once converted to a Vendor Bill |

### 4.2 VendorBill
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `purchase.html` → Vendor Bills sub-list | `GET /api/v1/vendor-bills?status=` | |
| Create | `purchase.html` → PO row → "Convert to Bill" (not a blank form — always derived from a PO) | `POST /api/v1/purchase-orders/{id}/convert-to-bill` | Requires the source PO already fetched/loaded (status CONFIRMED). Posts a `JournalEntry` as a side effect |
| Update | `purchase.html` → bill row → edit due date / amount adjustment (before payment) | `PUT /api/v1/vendor-bills/{id}` | |
| Delete | `purchase.html` → bill row delete (only if OPEN, unpaid, and reversible) | `DELETE /api/v1/vendor-bills/{id}` | Blocked once a Payment exists against it |

### 4.3 SalesOrder
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `sales.html`, on load | `GET /api/v1/sales-orders?status=` | |
| Create | `sales.html` → "New SO" | `POST /api/v1/sales-orders` | Requires Contact list (type=CUSTOMER) and Product list (type=SERVICE) pre-fetched |
| Update | `sales.html` → row edit (period, estimated qty/rate) while still open | `PUT /api/v1/sales-orders/{id}` | |
| Delete | `sales.html` → row delete (only before invoicing) | `DELETE /api/v1/sales-orders/{id}` | Blocked once converted to a Customer Invoice |

### 4.4 CustomerInvoice
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `sales.html` → Customer Invoices sub-list | `GET /api/v1/customer-invoices?status=` | Also fetched by `reports.html` (P&L drill-down) |
| Create | `sales.html` → SO row → "Convert to Invoice" (not a blank form) | `POST /api/v1/sales-orders/{id}/convert-to-invoice` | Requires: source SO fetched, plus `GET /api/v1/haul/dispatches?customerId=&status=COMPLETED&period=` fetched to compute `tonnageBilled`. Posts a `JournalEntry` as a side effect |
| Update | `sales.html` → invoice row edit (before payment, e.g. correct tonnage) | `PUT /api/v1/customer-invoices/{id}` | |
| Delete | `sales.html` → invoice row delete (only if OPEN/unpaid) | `DELETE /api/v1/customer-invoices/{id}` | Blocked once a Payment exists |

### 4.5 Payment
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `sales.html` and `purchase.html`, each filtered by direction | `GET /api/v1/payments?direction=IN` / `?direction=OUT` | Also fetched by `reports.html` (Balance Sheet cash reconciliation) |
| Create | `sales.html` → invoice row → "Collect Payment"; `purchase.html` → bill row → "Pay via Bank" | `POST /api/v1/customer-invoices/{id}/collect-payment` or `POST /api/v1/vendor-bills/{id}/pay` | Requires the source invoice/bill already loaded (amount due). Posts a `JournalEntry` as a side effect; flips invoice/bill status to `PAID` |
| Update | Not typically editable once posted | `PUT /api/v1/payments/{id}` (Admin-only correction) | |
| Delete | Not exposed in UI | `DELETE /api/v1/payments/{id}` (Admin/API only) | Reversal should create a counter journal entry rather than hard-delete, for audit integrity |

---

## 5. Budgeting Modules

### 5.1 AnalyticAccount
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `budgets.html`, on load | `GET /api/v1/analytic-accounts` | Also fetched by `dispatch.html` (Route form, to tag a route) and `reports.html` (Budget Report filter) |
| Create | `budgets.html` → "New Analytic Account" | `POST /api/v1/analytic-accounts` | No pre-fetch required |
| Update | `budgets.html` → edit | `PUT /api/v1/analytic-accounts/{id}` | |
| Delete | `budgets.html` → delete | `DELETE /api/v1/analytic-accounts/{id}` | Blocked if referenced by any Route, Budget, or JournalLine |

### 5.2 Budget
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | `budgets.html`, on load | `GET /api/v1/budgets?period=` | |
| Fetch (variance) | `budgets.html`, per row, and `reports.html` Budget Report | `GET /api/v1/budgets/{id}/variance` | Computed on the fly from `JournalLine` sums — not stored, always fetched fresh |
| Create | `budgets.html` → "New Budget Line" | `POST /api/v1/budgets` | Requires AnalyticAccount list and Account (GL) list pre-fetched for the two dropdowns |
| Update | `budgets.html` → edit planned amount | `PUT /api/v1/budgets/{id}` | |
| Delete | `budgets.html` → delete | `DELETE /api/v1/budgets/{id}` | No hard blocks — historical variance report should still reflect actuals even if the budget line is later removed, so prefer soft-delete/archive |

---

## 6. Journal / Ledger (System-Generated, Read-Mostly)

### 6.1 JournalEntry / JournalLine
| Operation | Page | Endpoint | Notes |
|---|---|---|---|
| Fetch (list) | Not directly exposed on a page by default — surfaced through `reports.html` (Balance Sheet, P&L) and `budgets.html` (variance) | `GET /api/v1/journal-entries?from=&to=&accountId=` | Consider adding a "General Ledger" admin drill-down page if raw audit trail viewing is needed |
| Create | **Never created directly from a form** — always a side effect of: Vendor Bill conversion, Customer Invoice conversion, Payment collection, Charge Session completion | Internal service calls, not a public POST endpoint | Every one of these must produce a balanced entry (sum debit = sum credit) |
| Update | Not editable — correction = reversing entry | — | |
| Delete | Not allowed | — | Financial integrity: never hard-delete a posted entry |

---

## 7. Fetch-Before-Create Dependency Chain

Use this order when wiring up a "New ___" form so its dropdowns are never empty:

```
Chart of Accounts  ──▶ Products (needs linkedAccountId)
Chart of Accounts  ──▶ Budgets (needs GL account)
Contacts           ──▶ Sales Orders (needs Customer)
Contacts           ──▶ Purchase Orders (needs Vendor)
Products           ──▶ Sales Orders (needs Service product)
Products           ──▶ Purchase Orders (needs Goods product)
Analytic Accounts  ──▶ Haul Routes (needs tagging)
Analytic Accounts  ──▶ Budgets (needs analytic dimension)
Haul Routes        ──▶ Haul Dispatches (needs Route)
Trucks             ──▶ Haul Dispatches (needs Truck)
Sales Orders + completed Haul Dispatches ──▶ Customer Invoices
Purchase Orders                          ──▶ Vendor Bills
Customer Invoices  ──▶ Payments (IN)
Vendor Bills       ──▶ Payments (OUT)
```

**Rule of thumb:** any page with a "New ___" button must first `GET` every dropdown's source list on page load (or on modal-open) before rendering the form — never let a create form open with an empty/unfetched dropdown.

---

## 8. Quick Reference — All Endpoints by CRUD Verb

| Module | GET (list) | GET (one) | POST | PUT | DELETE |
|---|---|---|---|---|---|
| Contact | `/contacts` | `/contacts/{id}` | `/contacts` | `/contacts/{id}` | `/contacts/{id}` |
| Product | `/products` | `/products/{id}` | `/products` | `/products/{id}` | `/products/{id}` |
| Account | `/accounts` | `/accounts/{id}` | `/accounts` | `/accounts/{id}` | `/accounts/{id}` |
| Journal | `/journals` | `/journals/{id}` | `/journals` | `/journals/{id}` | `/journals/{id}` |
| Truck | `/haul/trucks` | `/haul/trucks/{id}` | `/haul/trucks` | `/haul/trucks/{id}` | `/haul/trucks/{id}` |
| ChargingStation | `/haul/chargers` | `/haul/chargers/{id}` | `/haul/chargers` | `/haul/chargers/{id}` | `/haul/chargers/{id}` |
| HaulRoute | `/haul/routes` | `/haul/routes/{id}` | `/haul/routes` | `/haul/routes/{id}` | `/haul/routes/{id}` |
| HaulDispatch | `/haul/dispatches` | `/haul/dispatches/{id}` | `/haul/dispatches` | `/haul/dispatches/{id}` | `/haul/dispatches/{id}` |
| ChargeSession | `/haul/charge-sessions` | `/haul/charge-sessions/{id}` | internal only | internal only | admin only |
| PurchaseOrder | `/purchase-orders` | `/purchase-orders/{id}` | `/purchase-orders` | `/purchase-orders/{id}` | `/purchase-orders/{id}` |
| VendorBill | `/vendor-bills` | `/vendor-bills/{id}` | `/purchase-orders/{id}/convert-to-bill` | `/vendor-bills/{id}` | `/vendor-bills/{id}` |
| SalesOrder | `/sales-orders` | `/sales-orders/{id}` | `/sales-orders` | `/sales-orders/{id}` | `/sales-orders/{id}` |
| CustomerInvoice | `/customer-invoices` | `/customer-invoices/{id}` | `/sales-orders/{id}/convert-to-invoice` | `/customer-invoices/{id}` | `/customer-invoices/{id}` |
| Payment | `/payments` | `/payments/{id}` | `/customer-invoices/{id}/collect-payment`, `/vendor-bills/{id}/pay` | admin only | not allowed |
| AnalyticAccount | `/analytic-accounts` | `/analytic-accounts/{id}` | `/analytic-accounts` | `/analytic-accounts/{id}` | `/analytic-accounts/{id}` |
| Budget | `/budgets` (+ `/budgets/{id}/variance`) | `/budgets/{id}` | `/budgets` | `/budgets/{id}` | `/budgets/{id}` |
| JournalEntry | `/journal-entries` | `/journal-entries/{id}` | internal only | not allowed | not allowed |
