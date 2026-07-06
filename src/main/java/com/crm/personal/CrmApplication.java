package com.crm.personal;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Clase de configuración del contexto Spring Boot.
 *
 * El punto de entrada real de la aplicación es {@link Main} (JavaFX).
 * Se excluye {@code DataSourceAutoConfiguration} porque el DataSource
 * se configura manualmente en {@code DatabaseConfig} con la clave SQLCipher.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class CrmApplication {
    // Sin main() — Main controla el ciclo de vida completo.
}
