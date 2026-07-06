package com.crm.personal.infrastructure.security;

import java.util.Arrays;

public final class MasterPassword {

    private char[] value;

    public MasterPassword(char[] value) {
        this.value = Arrays.copyOf(value, value.length);
    }

    public String asString() {
        ensureAvailable();
        return new String(value);
    }

    public void clear() {
        if (value != null) {
            Arrays.fill(value, '\0');
            value = null;
        }
    }

    private void ensureAvailable() {
        if (value == null) {
            throw new IllegalStateException("La contraseña maestra ya fue limpiada");
        }
    }
}
