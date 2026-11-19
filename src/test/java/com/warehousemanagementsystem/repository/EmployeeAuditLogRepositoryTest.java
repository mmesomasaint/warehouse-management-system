package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.EmployeeAuditAction;
import com.warehousemanagementsystem.model.EmployeeAuditLog;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeAuditLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    static boolean dockerAvailable = false;
    static PostgreSQLContainer<?> postgres;

    @Autowired
    private EmployeeAuditLogRepository auditLogRepository;

    private OffsetDateTime t1;
    private OffsetDateTime t2;
    private OffsetDateTime t3;
    private OffsetDateTime t4;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();

        t1 = OffsetDateTime.parse("2026-11-18T08:00:00Z");
        t2 = OffsetDateTime.parse("2026-11-18T10:30:00Z");
        t3 = OffsetDateTime.parse("2026-11-19T09:15:00Z");
        t4 = OffsetDateTime.parse("2026-11-19T10:45:00Z");

        // Target 10L created & updated by actor 100L; later deactivated by actor 200L
        auditLogRepository.saveAndFlush(new EmployeeAuditLog(
                10L,
                "staff.ten@warehouse.com",
                EmployeeAuditAction.CREATED,
                100L,
                "Initial onboarding",
                "Assigned STORE_ASSOCIATE",
                t1
        ));
        auditLogRepository.saveAndFlush(new EmployeeAuditLog(
                10L,
                "staff.ten@warehouse.com",
                EmployeeAuditAction.ROLES_UPDATED,
                100L,
                "Cross-training",
                "Added RECEIVING_CLERK",
                t2
        ));
        auditLogRepository.saveAndFlush(new EmployeeAuditLog(
                10L,
                "staff.ten@warehouse.com",
                EmployeeAuditAction.DEACTIVATED,
                200L,
                "End of shift contract",
                "Account disabled",
                t4
        ));

        // Target 20L created by actor 200L
        auditLogRepository.saveAndFlush(new EmployeeAuditLog(
                20L,
                "staff.twenty@warehouse.com",
                EmployeeAuditAction.CREATED,
                200L,
                "Initial onboarding",
                "Assigned TRANSFER_COORDINATOR",
                t3
        ));
    }

    @BeforeAll
    static void checkDockerAndStartContainer() {
        try {
            dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            dockerAvailable = false;
        }

        if (dockerAvailable) {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine");
            postgres.start();
        }
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        if (dockerAvailable && postgres != null) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        } else {
            registry.add(
                    "spring.datasource.url",
                    () -> "jdbc:h2:mem:auditlogdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
            );
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        }
    }

    @Test
    void shouldRetrieveLogsByTargetEmployeeIdOrderedByOccurredAtDesc() {
        List<EmployeeAuditLog> logsForTarget10 =
                auditLogRepository.findByTargetEmployeeIdOrderByOccurredAtDesc(10L);

        assertThat(logsForTarget10).hasSize(3);
        assertThat(logsForTarget10)
                .extracting(EmployeeAuditLog::getAction)
                .containsExactly(
                        EmployeeAuditAction.DEACTIVATED,
                        EmployeeAuditAction.ROLES_UPDATED,
                        EmployeeAuditAction.CREATED
                );
        assertThat(logsForTarget10)
                .extracting(EmployeeAuditLog::getOccurredAt)
                .containsExactly(t4, t2, t1);
    }

    @Test
    void shouldRetrieveLogsByActorEmployeeIdOrderedByOccurredAtDesc() {
        List<EmployeeAuditLog> logsByActor200 =
                auditLogRepository.findByActorEmployeeIdOrderByOccurredAtDesc(200L);

        assertThat(logsByActor200).hasSize(2);
        assertThat(logsByActor200)
                .extracting(EmployeeAuditLog::getTargetEmployeeId)
                .containsExactly(10L, 20L);
        assertThat(logsByActor200)
                .extracting(EmployeeAuditLog::getAction)
                .containsExactly(EmployeeAuditAction.DEACTIVATED, EmployeeAuditAction.CREATED);
        assertThat(logsByActor200)
                .extracting(EmployeeAuditLog::getOccurredAt)
                .containsExactly(t4, t3);
    }

    @Test
    void shouldFilterByTargetEmployeeIdAndActionOrderedByOccurredAtDesc() {
        List<EmployeeAuditLog> createdLogsForTarget10 =
                auditLogRepository.findByTargetEmployeeIdAndActionOrderByOccurredAtDesc(
                        10L,
                        EmployeeAuditAction.CREATED
                );

        assertThat(createdLogsForTarget10).hasSize(1);
        assertThat(createdLogsForTarget10.get(0).getActorEmployeeId()).isEqualTo(100L);
        assertThat(createdLogsForTarget10.get(0).getOccurredAt()).isEqualTo(t1);
    }

    @Test
    void shouldGenerateUuidOnPersist() {
        EmployeeAuditLog log = new EmployeeAuditLog(
                50L,
                "uuid.check@warehouse.com",
                EmployeeAuditAction.CREATED,
                100L,
                "Initial onboarding",
                "Assigned STORE_ASSOCIATE",
                OffsetDateTime.parse("2026-11-19T12:00:00Z")
        );

        EmployeeAuditLog saved = auditLogRepository.saveAndFlush(log);

        assertThat(saved.getAuditId()).isNotNull();
        assertThat(saved.getAuditId()).isInstanceOf(java.util.UUID.class);
    }

    @Test
    void shouldEnforceNonNullConstraintsOnRequiredFields() {
        // Missing targetEmail (null)
        EmployeeAuditLog missingEmail = new EmployeeAuditLog(
                50L,
                null,
                EmployeeAuditAction.CREATED,
                100L,
                "Initial onboarding",
                "Details",
                OffsetDateTime.parse("2026-11-19T12:00:00Z")
        );
        auditLogRepository.save(missingEmail);
        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        entityManager.clear();

        // Missing action (null)
        EmployeeAuditLog missingAction = new EmployeeAuditLog(
                50L,
                "notnull.check@warehouse.com",
                null,
                100L,
                "Initial onboarding",
                "Details",
                OffsetDateTime.parse("2026-11-19T12:00:00Z")
        );
        auditLogRepository.save(missingAction);
        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        entityManager.clear();

        // Missing occurredAt (null)
        EmployeeAuditLog missingTimestamp = new EmployeeAuditLog(
                50L,
                "notnull.check@warehouse.com",
                EmployeeAuditAction.DEACTIVATED,
                100L,
                "Account disabled",
                "Details",
                null
        );
        auditLogRepository.save(missingTimestamp);
        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        entityManager.clear();
    }
}