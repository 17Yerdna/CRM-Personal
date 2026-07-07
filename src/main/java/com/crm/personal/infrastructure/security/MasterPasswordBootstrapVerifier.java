package com.crm.personal.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MasterPasswordBootstrapVerifier {

    private static final Path AUTH_DIR = Path.of(System.getProperty("user.home"), ".crm-personal");
    private static final Path AUTH_FILE = AUTH_DIR.resolve("auth.bcrypt");
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder(12);

    private MasterPasswordBootstrapVerifier() {
    }

    public static Path getDataDir() {
        return AUTH_DIR;
    }

    public static boolean isFirstRun() {
        return !Files.exists(AUTH_FILE);
    }

    public static void verifyOrInitialize(char[] password) throws IOException {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("La contraseña maestra es obligatoria");
        }

        String rawPassword = new String(password);

        if (isFirstRun()) {
            Files.createDirectories(AUTH_DIR);
            Files.writeString(AUTH_FILE, BCRYPT.encode(rawPassword), StandardCharsets.UTF_8);
            return;
        }

        String storedHash = Files.readString(AUTH_FILE, StandardCharsets.UTF_8).trim();
        if (!BCRYPT.matches(rawPassword, storedHash)) {
            throw new SecurityException("La contraseña maestra es incorrecta");
        }
    }
}
