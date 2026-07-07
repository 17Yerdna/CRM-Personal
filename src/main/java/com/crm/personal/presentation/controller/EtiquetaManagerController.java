package com.crm.personal.presentation.controller;

import com.crm.personal.application.dto.EtiquetaDTO;
import com.crm.personal.application.desktop.command.DesktopSaveEtiquetaCommand;
import com.crm.personal.application.desktop.dto.DesktopEtiquetaDto;
import com.crm.personal.application.desktop.port.DesktopCrmUseCase;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class EtiquetaManagerController {

    @FXML private TreeView<DesktopEtiquetaDto> etiquetasTree;
    @FXML private TextField             nombreField;
    @FXML private ColorPicker           colorPicker;
    @FXML private ComboBox<DesktopEtiquetaDto> padreCombo;
    @FXML private Button                btnGuardar;
    @FXML private Button                btnEliminar;
    @FXML private Button                btnNueva;

    private final DesktopCrmUseCase desktopCrm;
    private DesktopEtiquetaDto etiquetaSeleccionada;

    public EtiquetaManagerController(DesktopCrmUseCase desktopCrm) {
        this.desktopCrm = desktopCrm;
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
            protected void updateItem(DesktopEtiquetaDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String color = item.colorHex() != null ? item.colorHex() : "#c2c8bf";
                    
                    Region colorIndicator = new Region();
                    colorIndicator.setPrefSize(6, 16);
                    colorIndicator.setMaxSize(6, 16);
                    colorIndicator.setStyle(
                        "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 3;"
                    );
                    
                    Label nameLabel = new Label(item.nombre());
                    nameLabel.setStyle("-fx-text-fill: #2c2c2c;");
                    
                    HBox hbox = new HBox(8, colorIndicator, nameLabel);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
    }

    private void configurarComboBox() {
        padreCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DesktopEtiquetaDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nombre());
            }
        });
        padreCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(DesktopEtiquetaDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sin padre (raíz)" : item.nombre());
            }
        });
    }

    private void cargarDatos() {
        // Árbol
        TreeItem<DesktopEtiquetaDto> root = new TreeItem<>();
        root.setExpanded(true);
        for (DesktopEtiquetaDto e : desktopCrm.findRootTags()) {
            root.getChildren().add(buildTreeItem(e));
        }
        etiquetasTree.setRoot(root);
        etiquetasTree.setShowRoot(false);

        // Combo padre
        padreCombo.getItems().clear();
        padreCombo.getItems().add(null);
        padreCombo.getItems().addAll(desktopCrm.findAllTags());
    }

    private TreeItem<DesktopEtiquetaDto> buildTreeItem(DesktopEtiquetaDto e) {
        TreeItem<DesktopEtiquetaDto> item = new TreeItem<>(e);
        item.setExpanded(true);
        for (DesktopEtiquetaDto hijo : e.hijos()) {
            item.getChildren().add(buildTreeItem(hijo));
        }
        return item;
    }

    private void cargarEnFormulario(DesktopEtiquetaDto etiqueta) {
        etiquetaSeleccionada = etiqueta;
        nombreField.setText(etiqueta.nombre());
        if (etiqueta.colorHex() != null) {
            try { colorPicker.setValue(Color.web(etiqueta.colorHex())); }
            catch (Exception ignored) {}
        }
        padreCombo.getItems().stream()
            .filter(item -> item != null && item.id().equals(etiqueta.padreId()))
            .findFirst()
            .ifPresentOrElse(padreCombo::setValue, () -> padreCombo.setValue(null));
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

        try {
            desktopCrm.saveTag(new DesktopSaveEtiquetaCommand(
                etiquetaSeleccionada != null ? etiquetaSeleccionada.id() : null,
                nombre,
                hex,
                padreCombo.getValue() != null ? padreCombo.getValue().id() : null
            ));
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
            "¿Eliminar '" + etiquetaSeleccionada.nombre() + "'?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    desktopCrm.deleteTag(etiquetaSeleccionada.id());
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
