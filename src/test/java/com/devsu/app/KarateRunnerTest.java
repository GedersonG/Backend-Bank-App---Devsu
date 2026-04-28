package com.devsu.app;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KarateRunnerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testHealth() {
        return Karate.run("classpath:karate/health.feature")
                .systemProperty("server.port", String.valueOf(port));
    }

    @Karate.Test
    Karate testClientes() {
        return Karate.run("classpath:karate/clientes.feature")
                .systemProperty("server.port", String.valueOf(port));
    }

    @Karate.Test
    Karate testCuentas() {
        return Karate.run("classpath:karate/cuentas.feature")
                .systemProperty("server.port", String.valueOf(port));
    }

    @Karate.Test
    Karate testMovimientos() {
        return Karate.run("classpath:karate/movimientos.feature")
                .systemProperty("server.port", String.valueOf(port));
    }
}