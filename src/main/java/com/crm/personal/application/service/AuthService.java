package com.crm.personal.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gestiona la contraseña maestra que cifra la base de datos SQLite.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.auth.file}")
    private String authFilePath;

    @Value("${app.data.dir}")
    private String dataDir;

    public AuthService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /** true si no existe el archivo de hash (primer arranque). */
    public boolean isFirstRun() {
        return !Files.exists(Paths.get(authFilePath));
    }

    /** Crea la contraseña maestra en el primer arranque. */
    public void setupPassword(String plainPassword) {
        if (!isFirstRun()) {
            throw new IllegalStateException("La contraseña ya está configurada.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        try {
            Files.createDirectories(Paths.get(dataDir));
            String hash = passwordEncoder.encode(plainPassword);
            Files.writeString(Paths.get(authFilePath), hash, StandardCharsets.UTF_8);
            log.info("Contraseña maestra configurada correctamente.");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el hash de contraseña.", e);
        }
    }

    /** Verifica que la contraseña coincida con el hash almacenado. */
    public boolean verifyPassword(String plainPassword) {
        try {
            Path path = Paths.get(authFilePath);
            if (!Files.exists(path)) return false;
            String storedHash = Files.readString(path, StandardCharsets.UTF_8).trim();
            return passwordEncoder.matches(plainPassword, storedHash);
        } catch (IOException e) {
            log.error("Error al leer archivo de autenticación", e);
            return false;
        }
    }

    /** Cambia la contraseña maestra (requiere la actual). */
    public void changePassword(String currentPassword, String newPassword) {
        if (!verifyPassword(currentPassword)) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }
        try {
            String newHash = passwordEncoder.encode(newPassword);
            Files.writeString(Paths.get(authFilePath), newHash, StandardCharsets.UTF_8);
            log.info("Contraseña maestra actualizada.");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo actualizar la contraseña.", e);
        }
    }
}
