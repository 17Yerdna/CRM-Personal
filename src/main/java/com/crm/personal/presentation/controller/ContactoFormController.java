package com.crm.personal.presentation.controller;

import com.crm.personal.application.contact.command.CreateContactCommand;
import com.crm.personal.application.contact.command.UpdateContactCommand;
import com.crm.personal.application.contact.port.CreateContactUseCase;
import com.crm.personal.application.contact.port.UpdateContactUseCase;
import com.crm.personal.application.desktop.dto.DesktopCampoDinamicoDto;
import com.crm.personal.application.desktop.dto.DesktopCampoValorDto;
import com.crm.personal.application.desktop.dto.DesktopContactoDto;
import com.crm.personal.application.desktop.dto.DesktopEtiquetaDto;
import com.crm.personal.application.desktop.port.DesktopCrmUseCase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
@Scope("prototype")
public class ContactoFormController {

    @FXML private Label    tituloLabel;
    @FXML private TextField nombreField;
    @FXML private TextField dniField;
    @FXML private TextField direccionField;
    @FXML private ImageView fotoPreview;
    @FXML private Button    btnSeleccionarFoto;
    @FXML private ListView<DesktopEtiquetaDto> etiquetasDisponibles;
    @FXML private ListView<DesktopEtiquetaDto> etiquetasSeleccionadas;
    @FXML private Button    btnAsignarEtiqueta;
    @FXML private Button    btnQuitarEtiqueta;
    @FXML private GridPane  camposDinamicosGrid;
    @FXML private Button    btnGuardar;
    @FXML private Button    btnCancelar;

    private final CreateContactUseCase    createContactUseCase;
    private final UpdateContactUseCase    updateContactUseCase;
    private final DesktopCrmUseCase       desktopCrm;

    private DesktopContactoDto contactoExistente;
    private String   fotoPath;
    private final Map<Long, TextField> campoInputs = new LinkedHashMap<>();

    /** Callback invocado al guardar correctamente */
    private Runnable onSuccess;

    public ContactoFormController(CreateContactUseCase createContactUseCase,
                                  UpdateContactUseCase updateContactUseCase,
                                  DesktopCrmUseCase desktopCrm) {
        this.createContactUseCase    = createContactUseCase;
        this.updateContactUseCase    = updateContactUseCase;
        this.desktopCrm              = desktopCrm;
    }

    @FXML
    public void initialize() {
        configurarListViews();
        cargarEtiquetasDisponibles();
        cargarCamposDinamicos();
    }

    private void configurarListViews() {
        etiquetasDisponibles.setCellFactory(lv -> etiquetaCell());
        etiquetasSeleccionadas.setCellFactory(lv -> etiquetaCell());
    }

    private ListCell<DesktopEtiquetaDto> etiquetaCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(DesktopEtiquetaDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.nombre());
                    setStyle(item.colorHex() != null
                        ? "-fx-text-fill: " + item.colorHex() + ";" : "");
                }
            }
        };
    }

    private void cargarEtiquetasDisponibles() {
        etiquetasDisponibles.getItems().setAll(desktopCrm.findAllTags());
    }

    private void cargarCamposDinamicos() {
        campoInputs.clear();
        camposDinamicosGrid.getChildren().clear();
        List<DesktopCampoDinamicoDto> campos = desktopCrm.findActiveFields();
        for (int i = 0; i < campos.size(); i++) {
            DesktopCampoDinamicoDto campo = campos.get(i);
            Label lbl = new Label(campo.nombre() + ":");
            lbl.setStyle("-fx-text-fill:#c0c0d0;");
            TextField tf = new TextField();
            tf.setPromptText("Ingresa " + campo.nombre());
            tf.setStyle("-fx-background-color:#3d3d5c;-fx-text-fill:white;-fx-background-radius:4;");
            camposDinamicosGrid.add(lbl, 0, i);
            camposDinamicosGrid.add(tf, 1, i);
            campoInputs.put(campo.id(), tf);
        }
    }

    public void setContacto(DesktopContactoDto contacto) {
        this.contactoExistente = contacto;
        if (contacto != null) rellenarFormulario(contacto);
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    private void rellenarFormulario(DesktopContactoDto c) {
        tituloLabel.setText("Editar Contacto");
        nombreField.setText(c.nombre());
        dniField.setText(c.dni());
        direccionField.setText(c.direccion());
        fotoPath = c.fotoPerfilPath();
        if (fotoPath != null) {
            try { fotoPreview.setImage(new Image("file:" + fotoPath)); }
            catch (Exception ignored) {}
        }
        // Etiquetas
        etiquetasSeleccionadas.getItems().setAll(c.etiquetas());
        etiquetasDisponibles.getItems().removeAll(c.etiquetas());
        // Campos dinámicos
        for (DesktopCampoValorDto v : c.camposDinamicos()) {
            TextField tf = campoInputs.get(v.campoId());
            if (tf != null) tf.setText(v.valor());
        }
    }

    @FXML
    public void handleSeleccionarFoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar foto de perfil");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.jpg","*.jpeg","*.png","*.gif","*.bmp"));
        File file = fc.showOpenDialog(btnSeleccionarFoto.getScene().getWindow());
        if (file != null) {
            fotoPath = file.getAbsolutePath();
            fotoPreview.setImage(new Image("file:" + fotoPath));
        }
    }

    @FXML
    public void handleAsignarEtiqueta() {
        DesktopEtiquetaDto sel = etiquetasDisponibles.getSelectionModel().getSelectedItem();
        if (sel != null) {
            etiquetasDisponibles.getItems().remove(sel);
            etiquetasSeleccionadas.getItems().add(sel);
        }
    }

    @FXML
    public void handleQuitarEtiqueta() {
        DesktopEtiquetaDto sel = etiquetasSeleccionadas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            etiquetasSeleccionadas.getItems().remove(sel);
            etiquetasDisponibles.getItems().add(sel);
        }
    }

    @FXML
    public void handleGuardar() {
        String nombre    = nombreField.getText().trim();
        String dni       = dniField.getText().trim();
        String direccion = direccionField.getText().trim();

        if (nombre.isBlank() || dni.isBlank() || direccion.isBlank()) {
            alerta("Campos requeridos", "Nombre, DNI y Dirección son obligatorios.");
            return;
        }
        if (!dni.matches("\\d{8}")) {
            alerta("DNI inválido", "El DNI debe tener exactamente 8 dígitos numéricos.");
            return;
        }

        Set<Long> etiquetaIds = new HashSet<>();
        etiquetasSeleccionadas.getItems().forEach(e -> etiquetaIds.add(e.id()));

        Map<Long, String> camposMap = new HashMap<>();
        campoInputs.forEach((id, tf) -> {
            if (!tf.getText().isBlank()) camposMap.put(id, tf.getText().trim());
        });

        try {
            if (contactoExistente != null) {
                updateContactUseCase.update(new UpdateContactCommand(
                    contactoExistente.id(), nombre, dni, direccion, fotoPath, etiquetaIds, camposMap
                ));
            } else {
                createContactUseCase.create(new CreateContactCommand(
                    nombre, dni, direccion, fotoPath, etiquetaIds, camposMap
                ));
            }
            if (onSuccess != null) onSuccess.run();
            ((Stage) btnGuardar.getScene().getWindow()).close();
        } catch (Exception e) {
            alerta("Error al guardar", e.getMessage());
        }
    }

    @FXML
    public void handleCancelar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }

    private void alerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
