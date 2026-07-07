package com.crm.personal.presentation.controller;

import com.crm.personal.application.dto.EtiquetaDTO;
import com.crm.personal.infrastructure.legacy.EtiquetaService;
import com.crm.personal.infrastructure.persistence.model.Etiqueta;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class EtiquetaManagerController {

    @FXML private TreeView<Etiqueta>    etiquetasTree;
    @FXML private TextField             nombreField;
    @FXML private ColorPicker           colorPicker;
    @FXML private ComboBox<Etiqueta>    padreCombo;
    @FXML private Button                btnGuardar;
    @FXML private Button                btnEliminar;
    @FXML private Button                btnNueva;

    private final EtiquetaService etiquetaService;
    private Etiqueta etiquetaSeleccionada;

    public EtiquetaManagerController(EtiquetaService etiquetaService) {
        this.etiquetaService = etiquetaService;
    }

    @FXML
    public void initialize() {
        configurarTreeView();
        configurarComboBox();
        cargarDatos();
        btnEliminar.setDisable(true);

        etiquetasTree.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newItem) -> {
                if (newItem != null && newItem.getValue() != null) {
                    cargarEnFormulario(newItem.getValue());
                }
            });
    }

    private void configurarTreeView() {
        etiquetasTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Etiqueta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getNombre());
                    setStyle(item.getColorHex() != null
                        ? "-fx-text-fill: " + item.getColorHex() + ";"
                        : "");
                }
            }
        });
    }

    private void configurarComboBox() {
        padreCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Etiqueta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        padreCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Etiqueta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sin padre (raíz)" : item.getNombre());
            }
        });
    }

    private void cargarDatos() {
        // Árbol
        TreeItem<Etiqueta> root = new TreeItem<>();
        root.setExpanded(true);
        for (Etiqueta e : etiquetaService.findRoots()) {
            root.getChildren().add(buildTreeItem(e));
        }
        etiquetasTree.setRoot(root);
        etiquetasTree.setShowRoot(false);

        // Combo padre
        padreCombo.getItems().clear();
        padreCombo.getItems().add(null);
        padreCombo.getItems().addAll(etiquetaService.findAll());
    }

    private TreeItem<Etiqueta> buildTreeItem(Etiqueta e) {
        TreeItem<Etiqueta> item = new TreeItem<>(e);
        item.setExpanded(true);
        for (Etiqueta hijo : e.getHijos()) {
            item.getChildren().add(buildTreeItem(hijo));
        }
        return item;
    }

    private void cargarEnFormulario(Etiqueta etiqueta) {
        etiquetaSeleccionada = etiqueta;
        nombreField.setText(etiqueta.getNombre());
        if (etiqueta.getColorHex() != null) {
            try { colorPicker.setValue(Color.web(etiqueta.getColorHex())); }
            catch (Exception ignored) {}
        }
        padreCombo.setValue(etiqueta.getPadre());
        btnEliminar.setDisable(false);
    }

    @FXML
    public void handleNueva() {
        etiquetaSeleccionada = null;
        nombreField.clear();
        colorPicker.setValue(Color.web("#6C63FF"));
        padreCombo.setValue(null);
        btnEliminar.setDisable(true);
        etiquetasTree.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGuardar() {
        String nombre = nombreField.getText().trim();
        if (nombre.isBlank()) {
            alerta(Alert.AlertType.ERROR, "El nombre de la etiqueta no puede estar vacío.");
            return;
        }
        Color c = colorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
            (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255));

        EtiquetaDTO dto = EtiquetaDTO.builder()
            .id(etiquetaSeleccionada != null ? etiquetaSeleccionada.getId() : null)
            .nombre(nombre).colorHex(hex)
            .padreId(padreCombo.getValue() != null ? padreCombo.getValue().getId() : null)
            .build();

        try {
            etiquetaService.save(dto);
            cargarDatos();
            handleNueva();
        } catch (Exception e) {
            alerta(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    public void handleEliminar() {
        if (etiquetaSeleccionada == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar '" + etiquetaSeleccionada.getNombre() + "'?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    etiquetaService.delete(etiquetaSeleccionada.getId());
                    cargarDatos();
                    handleNueva();
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
