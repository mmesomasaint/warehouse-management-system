package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InventoryBalanceRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private InventoryBalanceRepository inventoryBalanceRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryZoneRepository inventoryZoneRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void shouldFindLowStockAlertsByStore() {
        // Setup mock reference entities required by foreign keys
        Region region = new Region();
        region.setRegionName("Intermountain West");
        region = regionRepository.save(region);

        State state = new State();
        state.setStateCode("UT");
        state.setStateName("Utah");
        state.setRegion(region);
        state = stateRepository.save(state);

        Store store = new Store();
        store.setStoreNumber(1001);
        store.setStoreName("SLC Downtown");
        store.setState(state);
        store.setCity("Salt Lake City");
        store.setPostalCode("84101");
        store.setStreetAddress("100 S Main St");
        store = storeRepository.save(store);

        Product product = new Product();
        product.setSku("LUMBER-2X4");
        product.setProductName("2x4 Stud");
        product.setCategory("Lumber");
        product = productRepository.save(product);

        InventoryZone zone = new InventoryZone();
        zone.setZoneName("Lumber Yard");
        zone = inventoryZoneRepository.save(zone);

        // Create inventory balance where on-hand minus allocated is below safety stock (10)
        InventoryBalance.InventoryBalanceId id = new InventoryBalance.InventoryBalanceId(store.getStoreId(), product.getProductId(), zone.getZoneId());
        InventoryBalance balance = new InventoryBalance();
        balance.setId(id);
        balance.setStore(store);
        balance.setProduct(product);
        balance.setZone(zone);
        balance.setQuantityOnHand(15);
        balance.setAllocatedQuantity(10); // Available = 5, which is < minimumSafetyStock (10)
        balance.setMinimumSafetyStock(10);

        inventoryBalanceRepository.save(balance);

        // Execute query
        List<InventoryBalance> lowStockItems = inventoryBalanceRepository.findLowStockAlertsByStore(store.getStoreId());

        // Assertions
        assertThat(lowStockItems).hasSize(1);
        assertThat(lowStockItems.get(0).getProduct().getSku()).isEqualTo("LUMBER-2X4");
    }
}