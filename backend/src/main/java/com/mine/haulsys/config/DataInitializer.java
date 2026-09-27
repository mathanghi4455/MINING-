package com.mine.haulsys.config;

import com.mine.haulsys.models.*;
import com.mine.haulsys.models.enums.*;
import com.mine.haulsys.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final AnalyticAccountRepository analyticAccountRepository;
    private final HaulRouteRepository haulRouteRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final TruckRepository truckRepository;
    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final HaulDispatchRepository haulDispatchRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // ===== USERS =====
        if (userRepository.count() == 0) {
            AppUser admin = AppUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .fullName("Mine Transport Director")
                .email("admin@minehaulops.com")
                .build();
            userRepository.save(admin);

            AppUser accountant = AppUser.builder()
                .username("accountant")
                .password(passwordEncoder.encode("acc123"))
                .role(UserRole.INVOICING)
                .fullName("Fleet Accountant")
                .email("accountant@minehaulops.com")
                .build();
            userRepository.save(accountant);
            log.info("Seeded 2 users");
        }

        // ===== CHART OF ACCOUNTS =====
        Account acc5000 = null;
        Account acc4000 = null;
        if (accountRepository.count() == 0) {
            accountRepository.save(Account.builder().code("1000").name("Electric Haul Truck Fleet").type(AccountType.ASSET).build());
            accountRepository.save(Account.builder().code("1100").name("Pantograph Chargers").type(AccountType.ASSET).build());
            accountRepository.save(Account.builder().code("1200").name("Cash/Bank").type(AccountType.ASSET).build());
            accountRepository.save(Account.builder().code("1300").name("Accounts Receivable").type(AccountType.ASSET).build());
            accountRepository.save(Account.builder().code("2000").name("Vehicle Supplier Payables").type(AccountType.LIABILITY).build());
            accountRepository.save(Account.builder().code("2100").name("Power Utility Creditors").type(AccountType.LIABILITY).build());
            accountRepository.save(Account.builder().code("3000").name("Equity Capital").type(AccountType.EQUITY).build());
            acc4000 = accountRepository.save(Account.builder().code("4000").name("Haulage Transport Revenue").type(AccountType.INCOME).build());
            acc5000 = accountRepository.save(Account.builder().code("5000").name("Fleet Charging Power Costs").type(AccountType.EXPENSE).build());
            accountRepository.save(Account.builder().code("5100").name("Heavy Tire & Motor Replacement Expenses").type(AccountType.EXPENSE).build());
            log.info("Seeded 10 accounts");
        } else {
            acc4000 = accountRepository.findByCode("4000").orElse(null);
            acc5000 = accountRepository.findByCode("5000").orElse(null);
        }

        // ===== JOURNALS =====
        if (journalRepository.count() == 0) {
            journalRepository.save(Journal.builder().name("Sales Journal").type(JournalType.SALES).build());
            journalRepository.save(Journal.builder().name("Purchase Journal").type(JournalType.PURCHASE).build());
            journalRepository.save(Journal.builder().name("Bank Journal").type(JournalType.BANK).build());
            journalRepository.save(Journal.builder().name("Cash Journal").type(JournalType.CASH).build());
            log.info("Seeded 4 journals");
        }

        // ===== ANALYTIC ACCOUNTS =====
        AnalyticAccount zone1 = null;
        AnalyticAccount zone2 = null;
        if (analyticAccountRepository.count() == 0) {
            zone1 = analyticAccountRepository.save(AnalyticAccount.builder().name("Open Pit Transport Zone 1").description("Northern zone haulage operations").build());
            zone2 = analyticAccountRepository.save(AnalyticAccount.builder().name("Open Pit Transport Zone 2").description("Southern zone haulage operations").build());
            analyticAccountRepository.save(AnalyticAccount.builder().name("Charging Operations").description("Megawatt pantograph charging costs").build());
            log.info("Seeded 3 analytic accounts");
        } else {
            zone1 = analyticAccountRepository.findAll().stream().findFirst().orElse(null);
            zone2 = analyticAccountRepository.findAll().stream().skip(1).findFirst().orElse(null);
        }

        // ===== HAUL ROUTES =====
        HaulRoute routeC = null;
        if (haulRouteRepository.count() == 0) {
            haulRouteRepository.save(HaulRoute.builder().routeName("North Pit Run").originPit("North Pit").destinationDump("East Dump").distanceKm(4.2).build());
            haulRouteRepository.save(HaulRoute.builder().routeName("South Pit Run").originPit("South Pit").destinationDump("West Crusher").distanceKm(6.8).build());
            routeC = haulRouteRepository.save(HaulRoute.builder().routeName("Central Run").originPit("Central Pit").destinationDump("Storage Yard").distanceKm(3.1).build());
            log.info("Seeded 3 haul routes");
        } else {
            routeC = haulRouteRepository.findAll().stream().skip(2).findFirst().orElse(null);
        }

        // ===== CHARGING STATIONS =====
        if (chargingStationRepository.count() == 0) {
            chargingStationRepository.save(ChargingStation.builder().stationCode("CS-001").name("Pantograph Station Alpha").maxPowerKw(350.0).latitude(-26.10).longitude(28.10).status(StationStatus.AVAILABLE).build());
            chargingStationRepository.save(ChargingStation.builder().stationCode("CS-002").name("Pantograph Station Beta").maxPowerKw(500.0).latitude(-26.15).longitude(28.15).status(StationStatus.AVAILABLE).build());
            chargingStationRepository.save(ChargingStation.builder().stationCode("CS-003").name("Pantograph Station Gamma").maxPowerKw(350.0).latitude(-26.20).longitude(28.08).status(StationStatus.AVAILABLE).build());
            log.info("Seeded 3 charging stations");
        }

        // ===== TRUCKS =====
        Truck truck1 = null;
        if (truckRepository.count() == 0) {
            truck1 = truckRepository.save(Truck.builder().truckCode("TRK-001").model("Cat 797F").batteryCapacityKwh(1800.0).currentSoCPercent(78.0).status(TruckStatus.IDLE).currentLat(-26.12).currentLng(28.11).build());
            truckRepository.save(Truck.builder().truckCode("TRK-002").model("Komatsu 960E").batteryCapacityKwh(2000.0).currentSoCPercent(45.0).status(TruckStatus.HAULING).currentLat(-26.13).currentLng(28.12).build());
            truckRepository.save(Truck.builder().truckCode("TRK-003").model("BelAZ 75600").batteryCapacityKwh(1600.0).currentSoCPercent(18.0).status(TruckStatus.HAULING).currentLat(-26.14).currentLng(28.09).build());
            truckRepository.save(Truck.builder().truckCode("TRK-004").model("Cat 797F").batteryCapacityKwh(1800.0).currentSoCPercent(92.0).status(TruckStatus.IDLE).currentLat(-26.11).currentLng(28.13).build());
            truckRepository.save(Truck.builder().truckCode("TRK-005").model("Komatsu 960E").batteryCapacityKwh(2000.0).currentSoCPercent(34.0).status(TruckStatus.HAULING).currentLat(-26.16).currentLng(28.14).build());
            log.info("Seeded 5 trucks");
        } else {
            truck1 = truckRepository.findAll().stream().findFirst().orElse(null);
        }

        // ===== CONTACTS =====
        Contact ironvale = null;
        Contact heavyParts = null;
        if (contactRepository.count() == 0) {
            ironvale = contactRepository.save(Contact.builder().type(ContactType.CUSTOMER).name("Ironvale Mining Corp").contactPerson("John Carter").email("john@ironvale.com").phone("+27-11-555-0100").build());
            contactRepository.save(Contact.builder().type(ContactType.CUSTOMER).name("Goldridge Operations Ltd").contactPerson("Sarah Mills").email("sarah@goldridge.com").phone("+27-11-555-0200").build());
            contactRepository.save(Contact.builder().type(ContactType.VENDOR).name("ElectroPower Utilities").contactPerson("David Chan").email("david@electropower.com").phone("+27-11-555-0300").build());
            heavyParts = contactRepository.save(Contact.builder().type(ContactType.VENDOR).name("HeavyParts Supply Co").contactPerson("Mike Torres").email("mike@heavyparts.com").phone("+27-11-555-0400").build());
            log.info("Seeded 4 contacts");
        } else {
            ironvale = contactRepository.findAll().stream().findFirst().orElse(null);
            heavyParts = contactRepository.findAll().stream().skip(3).findFirst().orElse(null);
        }

        // ===== PRODUCTS =====
        Product haul001 = null;
        Product part001 = null;
        if (productRepository.count() == 0) {
            haul001 = productRepository.save(Product.builder().name("Heavy Ore Haulage Ton-km").type(ProductType.SERVICE).sku("HAUL-001").unitOfMeasure(UnitOfMeasure.TON_KM).unitPrice(new BigDecimal("8.50")).build());
            productRepository.save(Product.builder().name("Megawatt Pantograph Charge Session").type(ProductType.SERVICE).sku("CHG-001").unitOfMeasure(UnitOfMeasure.SESSION).unitPrice(new BigDecimal("450.00")).build());
            part001 = productRepository.save(Product.builder().name("Haul Truck Drive Motor").type(ProductType.GOODS).sku("PART-001").unitOfMeasure(UnitOfMeasure.UNIT).unitPrice(new BigDecimal("28500.00")).build());
            productRepository.save(Product.builder().name("Heavy Mining Tire Set").type(ProductType.GOODS).sku("PART-002").unitOfMeasure(UnitOfMeasure.UNIT).unitPrice(new BigDecimal("12000.00")).build());
            log.info("Seeded 4 products");
        } else {
            haul001 = productRepository.findAll().stream().findFirst().orElse(null);
            part001 = productRepository.findAll().stream().skip(2).findFirst().orElse(null);
        }

        // ===== HAUL DISPATCHES =====
        if (haulDispatchRepository.count() == 0 && truck1 != null && routeC != null) {
            HaulDispatch dispatch = HaulDispatch.builder()
                .truckId(truck1.getId())
                .routeId(routeC.getId())
                .originPit("Central Pit")
                .destinationDump("Storage Yard")
                .tonnageMoved(510.0)
                .status(DispatchStatus.COMPLETED)
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now().minusMinutes(30))
                .distanceKm(3.1)
                .build();
            haulDispatchRepository.save(dispatch);
            log.info("Seeded 1 dispatch");
        }

        // ===== SALES ORDERS =====
        if (salesOrderRepository.count() == 0 && ironvale != null && haul001 != null) {
            SalesOrder so = SalesOrder.builder()
                .customerId(ironvale.getId())
                .productId(haul001.getId())
                .qty(5000.0)
                .unitPrice(new BigDecimal("8.50"))
                .totalAmount(new BigDecimal("42500.00"))
                .period("2025-Q3")
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();
            salesOrderRepository.save(so);
            log.info("Seeded 1 sales order");
        }

        // ===== PURCHASE ORDERS =====
        if (purchaseOrderRepository.count() == 0 && heavyParts != null && part001 != null) {
            PurchaseOrder po = PurchaseOrder.builder()
                .vendorId(heavyParts.getId())
                .productId(part001.getId())
                .qty(2.0)
                .unitPrice(new BigDecimal("28500.00"))
                .totalAmount(new BigDecimal("57000.00"))
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();
            purchaseOrderRepository.save(po);
            log.info("Seeded 1 purchase order");
        }

        // ===== BUDGETS =====
        if (budgetRepository.count() == 0 && zone1 != null && zone2 != null && acc4000 != null && acc5000 != null) {
            budgetRepository.save(Budget.builder().analyticAccountId(zone1.getId()).accountId(acc5000.getId()).period("2025-Q3").plannedAmount(new BigDecimal("85000.00")).build());
            budgetRepository.save(Budget.builder().analyticAccountId(zone1.getId()).accountId(acc4000.getId()).period("2025-Q3").plannedAmount(new BigDecimal("420000.00")).build());
            budgetRepository.save(Budget.builder().analyticAccountId(zone2.getId()).accountId(acc5000.getId()).period("2025-Q3").plannedAmount(new BigDecimal("65000.00")).build());
            log.info("Seeded 3 budgets");
        }

        log.info("Data initialization complete.");
    }
}
