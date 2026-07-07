package com.crm.personal.presentation;

public enum FxmlView {

    MAIN("/fxml/main.fxml",                     "CRM Personal"),
    CONTACTO_FORM("/fxml/contacto-form.fxml",   "Gestionar Contacto"),
    ETIQUETA_MANAGER("/fxml/etiqueta-manager.fxml", "Gestionar Etiquetas"),
    CAMPO_MANAGER("/fxml/campo-manager.fxml", "Gestionar Campos");

    private final String fxmlPath;
    private final String title;

    FxmlView(String fxmlPath, String title) {
        this.fxmlPath = fxmlPath;
        this.title    = title;
    }

    public String getFxmlPath() { return fxmlPath; }
    public String getTitle()    { return title;    }
}
