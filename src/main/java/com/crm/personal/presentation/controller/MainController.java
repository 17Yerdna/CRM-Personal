package com.crm.personal.presentation.controller;

import com.crm.personal.application.desktop.command.DesktopSaveMediaCommand;
import com.crm.personal.application.desktop.dto.*;
import com.crm.personal.application.desktop.port.DesktopCrmUseCase;
import com.crm.personal.presentation.javafx.SpringFXMLLoader;
import com.crm.personal.presentation.FxmlView;
import com.crm.personal.presentation.StageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Scope("prototype")
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    // ── Panel izquierdo ────────────────────────────────────────────────────
    @FXML private TreeView<DesktopEtiquetaDto> etiquetasTreeView;
    @FXML private RadioButton        radioAnd;
    @FXML private RadioButton        radioOr;
    @FXML private Button             btnNuevaEtiqueta;

    // ── Panel central ──────────────────────────────────────────────────────
    @FXML private TextField          searchField;
    @FXML private ListView<DesktopContactoDto> contactosListView;
    @FXML private Label              lblContadorContactos;
    @FXML private Button             btnNuevoContacto;
    @FXML private Button             btnImportar;
    @FXML private Button             btnDescargarPlantilla;

    // ── Panel derecho ──────────────────────────────────────────────────────
    @FXML private VBox       expedientePanel;
    @FXML private ImageView  fotoPerfilView;
    @FXML private Label      contactoNombreLabel;
    @FXML private Label      contactoDniLabel;
    @FXML private Label      contactoDireccionLabel;
    @FXML private VBox       camposDinamicosBox;
    @FXML private FlowPane   etiquetasChips;
    @FXML private VBox       timelineContainer;
    @FXML private Button     btnAgregarNota;
    @FXML private Button     btnAgregarMedia;
    @FXML private Button     btnExportarPdf;
    @FXML private Button     btnEditarContacto;
    @FXML private Button     btnEliminarContacto;

    private final DesktopCrmUseCase       desktopCrm;
    private final StageManager            stageManager;
    private final SpringFXMLLoader        springFXMLLoader;

    private DesktopContactoDto contactoActual;

    public MainController(DesktopCrmUseCase desktopCrm,
                          StageManager stageManager,
                          SpringFXMLLoader springFXMLLoader) {
        this.desktopCrm          = desktopCrm;
        this.stageManager        = stageManager;
        this.springFXMLLoader    = springFXMLLoader;
    }

    @FXML
    public void initialize() {
        configurarListView();
        configurarTree();
        cargarContactos();
        cargarEtiquetas();
        ocultarExpediente();
        searchField.textProperty().addListener((obs, old, nw) -> handleBuscar());
    }

    // ═══════════════════════════════════════════════════════════════
    // Configuración de controles
    // ═══════════════════════════════════════════════════════════════

    private void configurarListView() {
        contactosListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DesktopContactoDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox box  = new VBox(2);
                    Label nom = new Label(item.nombre());
                    nom.setStyle("-fx-font-weight:bold; -fx-text-fill:#e0e0f0;");
                    Label dni = new Label("DNI: " + item.dni());
                    dni.setStyle("-fx-text-fill:#9090b0; -fx-font-size:11px;");
                    box.getChildren().addAll(nom, dni);
                    setGraphic(box);
                    setText(null);
                }
            }
        });
        contactosListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> {
                if (sel != null) {
                    // Cargar con colecciones inicializadas (evita LazyInitializationException)
                    mostrarExpediente(desktopCrm.loadContact(sel.id()));
                }
            });
    }

    private void configurarTree() {
        etiquetasTreeView.setCellFactory(tv -> new TreeCell<>() {
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
        etiquetasTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        etiquetasTreeView.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, nw) -> handleBuscar());
    }

    // ═══════════════════════════════════════════════════════════════
    // Carga de datos
    // ═══════════════════════════════════════════════════════════════

    private void cargarContactos() {
        List<DesktopContactoDto> todos = desktopCrm.findAllContacts();
        contactosListView.getItems().setAll(todos);
        lblContadorContactos.setText(todos.size() + " contacto" + (todos.size() != 1 ? "s" : ""));
    }

    private void cargarEtiquetas() {
        TreeItem<DesktopEtiquetaDto> root = new TreeItem<>();
        root.setExpanded(true);
        desktopCrm.findAllTags().forEach(e -> root.getChildren().add(toTreeItem(e)));
        etiquetasTreeView.setRoot(root);
        etiquetasTreeView.setShowRoot(false);
    }

    private TreeItem<DesktopEtiquetaDto> toTreeItem(DesktopEtiquetaDto e) {
        TreeItem<DesktopEtiquetaDto> item = new TreeItem<>(e);
        item.setExpanded(true);
        return item;
    }

    // ═══════════════════════════════════════════════════════════════
    // Expediente / Timeline
    // ═══════════════════════════════════════════════════════════════

    private void mostrarExpediente(DesktopContactoDto c) {
        this.contactoActual = c;
        expedientePanel.setVisible(true);

        contactoNombreLabel.setText(c.nombre());
        contactoDniLabel.setText("DNI: " + c.dni());
        contactoDireccionLabel.setText("\uD83D\uDCCD " + c.direccion());

        // Foto de perfil
        if (c.fotoPerfilPath() != null) {
            try { fotoPerfilView.setImage(new Image("file:" + c.fotoPerfilPath())); }
            catch (Exception e) { fotoPerfilView.setImage(null); }
        } else {
            fotoPerfilView.setImage(null);
        }

        // Campos dinámicos
        camposDinamicosBox.getChildren().clear();
        for (DesktopCampoValorDto v : c.camposDinamicos()) {
            HBox row = new HBox(8);
            Label lbl = new Label(v.campoNombre() + ":");
            lbl.setStyle("-fx-text-fill:#9090b0; -fx-min-width:120;");
            Label val = new Label(v.valor());
            val.setStyle("-fx-text-fill:#e0e0f0;");
            row.getChildren().addAll(lbl, val);
            camposDinamicosBox.getChildren().add(row);
        }

        // Chips de etiquetas
        etiquetasChips.getChildren().clear();
        for (DesktopEtiquetaDto et : c.etiquetas()) {
            Label chip = new Label(et.nombre());
            String bg  = et.colorHex() != null ? et.colorHex() : "#6C63FF";
            chip.setStyle(
                "-fx-background-color:" + bg + "; -fx-text-fill:white; " +
                "-fx-background-radius:12; -fx-padding:3 10; -fx-font-size:10px;");
            etiquetasChips.getChildren().add(chip);
        }

        renderTimeline(c);
    }

    private void renderTimeline(DesktopContactoDto c) {
        timelineContainer.getChildren().clear();
        List<DesktopTimelineRecordDto> records = desktopCrm.getTimeline(c.id());

        if (records.isEmpty()) {
            Label empty = new Label("Sin registros en el expediente.");
            empty.setStyle("-fx-text-fill:#606080; -fx-font-style:italic; -fx-padding:20;");
            timelineContainer.getChildren().add(empty);
            return;
        }
        records.forEach(r -> timelineContainer.getChildren().add(crearCard(r)));
    }

    private VBox crearCard(DesktopTimelineRecordDto record) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color:#2a2a3e; -fx-background-radius:8; " +
                      "-fx-border-color:#3d3d5c; -fx-border-radius:8; " +
                      "-fx-border-width:1; -fx-padding:12;");
        card.setMaxWidth(Double.MAX_VALUE);

        // Encabezado
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        String ico = "NOTA".equals(record.tipo()) ? "\uD83D\uDCDD" : "\uD83D\uDDBC\uFE0F";
        Label icon  = new Label(ico); icon.setStyle("-fx-font-size:15px;");
        Label title = new Label(record.titulo());
        title.setStyle("-fx-font-weight:bold; -fx-text-fill:#e0e0f0; -fx-font-size:13px;");
        Region sp   = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label fecha = new Label(record.fecha() != null ? record.fecha().format(DATE_FMT) : "");
        fecha.setStyle("-fx-text-fill:#6060a0; -fx-font-size:10px;");

        Button btnE = iconBtn("\u270F\uFE0F", "-fx-text-fill:#a0a0c0;");
        Button btnB = iconBtn("\uD83D\uDDD1\uFE0F", "-fx-text-fill:#ff6b6b;");
        btnE.setOnAction(e -> handleEditarRecord(record));
        btnB.setOnAction(e -> handleEliminarRecord(record));

        header.getChildren().addAll(icon, title, sp, fecha, btnE, btnB);
        card.getChildren().add(header);

        // Contenido
        if ("NOTA".equals(record.tipo()) && record.contenidoHtml() != null) {
            String plain = record.contenidoHtml().replaceAll("<[^>]+>","").replace("&nbsp;"," ").trim();
            Label contenido = new Label(plain);
            contenido.setStyle("-fx-text-fill:#c0c0d0; -fx-wrap-text:true;");
            contenido.setWrapText(true);
            contenido.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(contenido);
        }

        // Galería multimedia
        if (!record.adjuntos().isEmpty()) {
            FlowPane gallery = new FlowPane(8, 8);
            record.adjuntos().forEach(adj -> gallery.getChildren().add(crearMiniMedia(adj)));
            card.getChildren().add(gallery);
        }
        return card;
    }

    private VBox crearMiniMedia(DesktopMediaAdjuntoDto adj) {
        VBox box = new VBox(4);
        box.setStyle("-fx-background-color:#1e1e2e; -fx-background-radius:6; -fx-padding:6;");
        box.setMaxWidth(130);

        if (adj.filePath() != null) {
            try {
                ImageView thumb = new ImageView(new Image("file:" + adj.filePath(), 120, 90, true, true));
                thumb.setFitWidth(120); thumb.setFitHeight(90);
                box.getChildren().add(thumb);
            } catch (Exception ignored) {}
        }
        if (adj.descripcion() != null && !adj.descripcion().isBlank()) {
            Label d = new Label(adj.descripcion());
            d.setStyle("-fx-text-fill:#c0c0d0; -fx-font-size:10px; -fx-wrap-text:true;");
            d.setMaxWidth(120); d.setWrapText(true);
            box.getChildren().add(d);
        }
        if (adj.lugar() != null && !adj.lugar().isBlank()) {
            box.getChildren().add(miniLabel("\uD83D\uDCCD " + adj.lugar()));
        }
        if (adj.fechaCaptura() != null) {
            box.getChildren().add(miniLabel("\uD83D\uDCC5 " +
                adj.fechaCaptura().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        }
        return box;
    }

    private void ocultarExpediente() {
        expedientePanel.setVisible(false);
    }

    // ═══════════════════════════════════════════════════════════════
    // Handlers
    // ═══════════════════════════════════════════════════════════════

    @FXML public void handleBuscar() {
        List<Long> etiquetaIds = new ArrayList<>();
        etiquetasTreeView.getSelectionModel().getSelectedItems().forEach(item -> {
            if (item != null && item.getValue() != null)
                etiquetaIds.add(item.getValue().id());
        });

        List<DesktopContactoDto> resultados = desktopCrm.searchContacts(
            searchField.getText(),
            etiquetaIds.isEmpty() ? null : etiquetaIds,
            radioOr.isSelected() ? "OR" : "AND"
        );
        contactosListView.getItems().setAll(resultados);
        lblContadorContactos.setText(resultados.size() + " contacto" + (resultados.size() != 1 ? "s" : ""));
    }

    @FXML public void handleNuevoContacto()  { abrirFormularioContacto(null); }
    @FXML public void handleEditarContacto() {
        if (contactoActual == null) { aviso("Selecciona un contacto primero."); return; }
        abrirFormularioContacto(contactoActual);
    }

    private void abrirFormularioContacto(DesktopContactoDto contacto) {
        try {
            SpringFXMLLoader.LoadResult<ContactoFormController> result =
                springFXMLLoader.loadWithController(FxmlView.CONTACTO_FORM.getFxmlPath());
            ContactoFormController ctrl = result.controller();
            if (contacto != null) ctrl.setContacto(contacto);
            ctrl.setOnSuccess(() -> Platform.runLater(() -> {
                cargarContactos();
                if (contacto != null) {
                    mostrarExpediente(desktopCrm.loadContact(contacto.id()));
                }
            }));
            Stage modal = new Stage();
            modal.setTitle(contacto != null ? "Editar Contacto" : "Nuevo Contacto");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(stageManager.getPrimaryStage());
            Scene scene = new Scene(result.root());
            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            modal.setScene(scene);
            modal.showAndWait();
        } catch (Exception e) {
            log.error("Error abriendo formulario", e);
            error("No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML public void handleEliminarContacto() {
        if (contactoActual == null) { aviso("Selecciona un contacto primero."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar a " + contactoActual.nombre() + "? Se eliminará todo su expediente.");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                desktopCrm.deleteContact(contactoActual.id());
                contactoActual = null;
                ocultarExpediente();
                cargarContactos();
            }
        });
    }

    @FXML public void handleNuevaEtiqueta() {
        stageManager.openModal(FxmlView.ETIQUETA_MANAGER);
        cargarEtiquetas();
    }

    @FXML public void handleAgregarNota() {
        if (contactoActual == null) return;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Agregar Nota");
        dlg.setHeaderText("Nueva nota para " + contactoActual.nombre());
        dlg.initOwner(stageManager.getPrimaryStage());
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setPrefWidth(520);
        TextField titulo  = new TextField(); titulo.setPromptText("Título de la nota (opcional)");
        TextArea  cuerpo  = new TextArea();
        cuerpo.setWrapText(true); cuerpo.setPrefHeight(220);
        cuerpo.setPromptText("Escribe aquí el contenido...");
        content.getChildren().addAll(new Label("Título:"), titulo, new Label("Contenido:"), cuerpo);
        dlg.getDialogPane().setContent(content);

        dlg.showAndWait().ifPresent(res -> {
            if (res == guardar) {
                String t = titulo.getText().isBlank() ? "Nota" : titulo.getText();
                String html = "<p>" + cuerpo.getText().replace("\n","<br/>") + "</p>";
                desktopCrm.addNote(contactoActual.id(), t, html);
                mostrarExpediente(desktopCrm.loadContact(contactoActual.id()));
            }
        });
    }

    @FXML public void handleAgregarMedia() {
        if (contactoActual == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar imágenes");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes","*.jpg","*.jpeg","*.png","*.gif","*.bmp"));
        List<java.io.File> files = fc.showOpenMultipleDialog(stageManager.getPrimaryStage());
        if (files == null || files.isEmpty()) return;

        List<DesktopSaveMediaCommand> adjuntos = new ArrayList<>();
        for (java.io.File file : files) {
            DesktopSaveMediaCommand adj = pedirMetadatos(file);
            if (adj != null) adjuntos.add(adj);
        }
        if (!adjuntos.isEmpty()) {
            desktopCrm.addMedia(contactoActual.id(),
                "Galería (" + adjuntos.size() + " foto(s))", adjuntos);
            mostrarExpediente(desktopCrm.loadContact(contactoActual.id()));
        }
    }

    private DesktopSaveMediaCommand pedirMetadatos(java.io.File file) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Metadatos: " + file.getName());
        dlg.initOwner(stageManager.getPrimaryStage());
        ButtonType ok = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(15));
        TextField desc  = new TextField(); desc.setPromptText("Descripción");
        TextField lugar = new TextField(); lugar.setPromptText("Lugar");
        DatePicker dp   = new DatePicker(java.time.LocalDate.now());
        grid.add(new Label("Descripción:"),0,0); grid.add(desc,1,0);
        grid.add(new Label("Lugar:"),0,1);       grid.add(lugar,1,1);
        grid.add(new Label("Fecha captura:"),0,2); grid.add(dp,1,2);
        dlg.getDialogPane().setContent(grid);

        var res = dlg.showAndWait();
        if (res.isPresent() && res.get() == ok) {
            return new DesktopSaveMediaCommand(
                file.getAbsolutePath(),
                file.getName(),
                desc.getText(),
                lugar.getText(),
                dp.getValue(),
                mime(file.getName())
            );
        }
        return null;
    }

    @FXML public void handleExportarPdf() {
        if (contactoActual == null) { aviso("Selecciona un contacto primero."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF");
        fc.setInitialFileName(contactoActual.nombre().replaceAll("\\s+","_") + "_expediente.pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
        java.io.File file = fc.showSaveDialog(stageManager.getPrimaryStage());
        if (file == null) return;
        try {
            byte[] pdf = desktopCrm.exportContactToPdf(contactoActual.id());
            Files.write(file.toPath(), pdf);
            info("PDF guardado en:\n" + file.getAbsolutePath());
        } catch (Exception e) { error(e.getMessage()); }
    }

    @FXML public void handleDescargarPlantilla() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar plantilla Excel");
        fc.setInitialFileName("plantilla_contactos.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel","*.xlsx"));
        java.io.File file = fc.showSaveDialog(stageManager.getPrimaryStage());
        if (file == null) return;
        try {
            Files.write(file.toPath(), desktopCrm.generateExcelTemplate());
            info("Plantilla guardada en:\n" + file.getAbsolutePath());
        } catch (Exception e) { error(e.getMessage()); }
    }

    @FXML public void handleImportar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importar desde Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel","*.xlsx","*.xls"));
        java.io.File file = fc.showOpenDialog(stageManager.getPrimaryStage());
        if (file == null) return;
        try (InputStream is = new FileInputStream(file)) {
            var result = desktopCrm.importFromExcel(is);
            cargarContactos();
            String msg = result.getResumen();
            if (!result.getMensajesError().isEmpty())
                msg += "\n\nErrores:\n" + String.join("\n", result.getMensajesError());
            info("Importación completada\n" + msg);
        } catch (Exception e) { error(e.getMessage()); }
    }

    private void handleEditarRecord(DesktopTimelineRecordDto record) {
        if (!"NOTA".equals(record.tipo())) {
            aviso("Solo se pueden editar notas de texto.");
            return;
        }
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Editar Nota");
        dlg.initOwner(stageManager.getPrimaryStage());
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        VBox content = new VBox(10); content.setPadding(new Insets(15)); content.setPrefWidth(520);
        TextField titulo = new TextField(record.titulo());
        TextArea cuerpo  = new TextArea();
        cuerpo.setWrapText(true); cuerpo.setPrefHeight(200);
        if (record.contenidoHtml() != null)
            cuerpo.setText(record.contenidoHtml().replaceAll("<[^>]+>","").trim());
        content.getChildren().addAll(new Label("Título:"), titulo, new Label("Contenido:"), cuerpo);
        dlg.getDialogPane().setContent(content);

        dlg.showAndWait().ifPresent(res -> {
            if (res == guardar) {
                desktopCrm.updateNote(record.id(), titulo.getText(),
                    "<p>" + cuerpo.getText().replace("\n","<br/>") + "</p>");
                mostrarExpediente(desktopCrm.loadContact(contactoActual.id()));
            }
        });
    }

    private void handleEliminarRecord(DesktopTimelineRecordDto record) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar '" + record.titulo() + "'?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                desktopCrm.deleteTimelineRecord(record.id());
                mostrarExpediente(desktopCrm.loadContact(contactoActual.id()));
            }
        });
    }

    // ── Utilidades ────────────────────────────────────────────────────────

    private Button iconBtn(String icon, String style) {
        Button b = new Button(icon);
        b.setStyle("-fx-background-color:transparent; -fx-cursor:hand; -fx-font-size:13px; " + style);
        return b;
    }

    private Label miniLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#9090b0; -fx-font-size:9px;");
        return l;
    }

    private String mime(String filename) {
        String l = filename.toLowerCase();
        if (l.endsWith(".jpg") || l.endsWith(".jpeg")) return "image/jpeg";
        if (l.endsWith(".png"))  return "image/png";
        if (l.endsWith(".gif"))  return "image/gif";
        return "application/octet-stream";
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void aviso(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
