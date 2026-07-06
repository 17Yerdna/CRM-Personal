package com.crm.personal.infrastructure.security;

/**
 * Titular estático de la contraseña maestra en memoria.
 *
 * Se establece ANTES de que Spring inicie para que {@code DatabaseConfig}
 * pueda configurar el DataSource cifrado con SQLCipher.
 *
 * Nota de seguridad: la contraseña se almacena como {@code String}.
 * Para mayor seguridad podría usarse {@code char[]} y limpiarla
 * después de abrir la conexión, pero se mantiene simple por claridad.
 */
public final class PasswordHolder {

    private static volatile String password;

    private PasswordHolder() {}

    public static void setPassword(String pwd) {
        password = pwd;
    }

    public static String getPassword() {
        return password;
    }

    public static void clearPassword() {
        password = null;
    }

    public static boolean hasPassword() {
        return password != null && !password.isEmpty();
    }
}
