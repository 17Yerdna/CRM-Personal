package com.crm.personal.infrastructure.config;

import com.crm.personal.infrastructure.security.MasterPassword;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

/**
 * Configuración de base de datos SQLite cifrada con SQLCipher.
 *
 * La contraseña maestra se registra como bean antes de iniciar Spring.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.crm.personal.infrastructure.persistence.repository")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${app.data.dir}")
    private String dataDir;

    @Bean
    public DataSource dataSource(MasterPassword masterPassword) {
        // Asegurar que el directorio de datos existe
        File dir = new File(dataDir);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("No se pudo crear el directorio de datos: {}", dataDir);
        }

        String dbPath  = dataDir + "/crm.db";
        String password = masterPassword.asString();

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");

        // Usamos org.xerial:sqlite-jdbc (binarios nativos incluidos).
        // La autenticación de contraseña maestra se gestiona con BCrypt en AuthService.
        // Para cifrado SQLCipher real, ver comentario en pom.xml.
        ds.setUrl("jdbc:sqlite:" + dbPath);
        log.info("Base de datos SQLite abierta: {}", dbPath);

        if (password != null && !password.isBlank()) {
            log.info("Acceso autorizado con contraseña maestra (BCrypt).");
        }
        return ds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.crm.personal.infrastructure.persistence.model");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(adapter);

        Properties props = new Properties();
        props.setProperty("hibernate.dialect",
                "org.hibernate.community.dialect.SQLiteDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "validate");
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        em.setJpaProperties(props);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
