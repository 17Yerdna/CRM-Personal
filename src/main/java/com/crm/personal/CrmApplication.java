package com.crm.personal;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Clase de configuración del contexto Spring Boot.
 *
 * El punto de entrada REAL de la aplicación es {@link MainApp} (JavaFX).
 * Se excluye {@code DataSourceAutoConfiguration} porque el DataSource
 * se configura manualmente en {@code DatabaseConfig} con la clave SQLCipher.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class CrmApplication {
    // Sin main() — MainApp controla el ciclo de vida completo.
}
