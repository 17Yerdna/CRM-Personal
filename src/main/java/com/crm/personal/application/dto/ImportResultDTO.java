package com.crm.personal.application.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDTO {

    private int importados = 0;
    private int errores    = 0;
    private final List<String> mensajesError = new ArrayList<>();

    public void addImportado() {
        importados++;
    }

    public void addError(String mensaje) {
        errores++;
        mensajesError.add(mensaje);
    }

    public int getImportados()           { return importados; }
    public int getErrores()              { return errores; }
    public List<String> getMensajesError() { return mensajesError; }

    public String getResumen() {
        return String.format("Importados: %d  |  Errores: %d", importados, errores);
    }
}
