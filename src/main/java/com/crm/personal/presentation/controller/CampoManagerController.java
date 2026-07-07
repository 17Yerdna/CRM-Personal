package com.crm.personal.presentation.controller;

import com.crm.personal.application.desktop.command.DesktopSaveCampoDinamicoCommand;
import com.crm.personal.application.desktop.dto.DesktopCampoDinamicoDto;
import com.crm.personal.application.desktop.port.DesktopCrmUseCase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CampoManagerController {

    @FXML private ListView<DesktopCampoDinamicoDto> camposList;
    @FXML private TextField nombreField;
    @FXML private ComboBox<String> tipoCombo;
    @FXML private CheckBox activoCheck;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnNuevo;

    private final DesktopCrmUseCase desktopCrm;
    private DesktopCampoDinamicoDto campoSeleccionado;

    public CampoManagerController(DesktopCrmUseCase desktopCrm) {
        this.desktopCrm = desktopCrm;
    }

    @FXML
    public void initialize() {
        tipoCombo.getItems().addAll("TEXT", "NUMBER", "DATE", "BOOLEAN");
        tipoCombo.setValue("TEXT");
        
        configurarListView();
        cargarDatos();
        btnEliminar.setDisable(true);

        camposList.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newItem) -> {
                if (newItem != null) {
                    cargarEnFormulario(newItem);
                }
            });
    }

    private void configurarListView() {
        camposList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DesktopCampoDinamicoDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String estado = item.activo() ? "✓" : "✗";
                    setText(item.nombre() + " [" + item.tipo() + "] " + estado);
                }
            }
        });
    }

    private void cargarDatos() {
        camposList.getItems().clear();
        camposList.getItems().addAll(desktopCrm.findAllFields());
    }

    private void cargarEnFormulario(DesktopCampoDinamicoDto campo) {
        campoSeleccionado = campo;
        nombreField.setText(campo.nombre());
        tipoCombo.setValue(campo.tipo());
        activoCheck.setSelected(campo.activo());
        btnEliminar.setDisable(false);
    }

    @FXML
    public void handleNuevo() {
        campoSeleccionado = null;
        nombreField.clear();
        tipoCombo.setValue("TEXT");
        activoCheck.setSelected(true);
        btnEliminar.setDisable(true);
        camposList.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        String nombre = nombreField.getText().trim();
        if (nombre.isBlank()) {
            alerta(Alert.AlertType.ERROR, "El nombre del campo no puede estar vacío.");
            return;
        }

        try {
            desktopCrm.saveCampoDinamico(new DesktopSaveCampoDinamicoCommand(
                campoSeleccionado != null ? campoSeleccionado.id() : null,
                nombre,
                tipoCombo.getValue(),
                activoCheck.isSelected()
            ));
            cargarDatos();
            handleNuevo();
        } catch (Exception e) {
            alerta(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    public void handleEliminar() {
        if (campoSeleccionado == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar el campo '" + campoSeleccionado.nombre() + "'?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    desktopCrm.deleteCampoDinamico(campoSeleccionado.id());
                    cargarDatos();
                    handleNuevo();
                } catch (Exception e) {
                    alerta(Alert.AlertType.ERROR, e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleCerrar() {
        ((Stage) btnGuardar.getScene().getWindow()).close();
    }

    private void alerta(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
