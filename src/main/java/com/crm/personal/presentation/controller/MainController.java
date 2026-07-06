package com.crm.personal.presentation.controller;

import com.crm.personal.application.dto.SearchCriteriaDTO;
import com.crm.personal.application.dto.SearchOperator;
import com.crm.personal.application.service.*;
import com.crm.personal.domain.model.*;
import com.crm.personal.domain.repository.CampoDinamicoRepository;
import com.crm.personal.infrastructure.config.SpringFXMLLoader;
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
    @FXML private TreeView<Etiqueta> etiquetasTreeView;
    @FXML private RadioButton        radioAnd;
    @FXML private RadioButton        radioOr;
    @FXML private Button             btnNuevaEtiqueta;

    // ── Panel central ──────────────────────────────────────────────────────
    @FXML private TextField          searchField;
    @FXML private ListView<Contacto> contactosListView;
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

    private final ContactoService         contactoService;
    private final EtiquetaService         etiquetaService;
    private final ExportImportService     exportImportService;
    private final TimelineService         timelineService;
    private final CampoDinamicoRepository campoRepo;
    private final StageManager            stageManager;
    private final SpringFXMLLoader        springFXMLLoader;

    private Contacto contactoActual;

    public MainController(ContactoService contactoService,
                          EtiquetaService etiquetaService,
                          ExportImportService exportImportService,
                          TimelineService timelineService,
                          CampoDinamicoRepository campoRepo,
                          StageManager stageManager,
                          SpringFXMLLoader springFXMLLoader) {
        this.contactoService     = contactoService;
        this.etiquetaService     = etiquetaService;
        this.exportImportService = exportImportService;
        this.timelineService     = timelineService;
        this.campoRepo           = campoRepo;
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
            protected void updateItem(Contacto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox box  = new VBox(2);
                    Label nom = new Label(item.getNombre());
                    nom.setStyle("-fx-font-weight:bold; -fx-text-fill:#e0e0f0;");
                    Label dni = new Label("DNI: " + item.getDni());
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
                    mostrarExpediente(contactoService.loadFull(sel.getId()));
                }
            });
    }

    private void configurarTree() {
        etiquetasTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Etiqueta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item.getNombre());
                    setStyle(item.getColorHex() != null
                        ? "-fx-text-fill:" + item.getColorHex() + ";"
                        : "-fx-text-fill:#c0c0d0;");
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
        List<Contacto> todos = contactoService.findAll();
        contactosListView.getItems().setAll(todos);
        lblContadorContactos.setText(todos.size() + " contacto" + (todos.size() != 1 ? "s" : ""));
    }

    private void cargarEtiquetas() {
        TreeItem<Etiqueta> root = new TreeItem<>();
        root.setExpanded(true);
        etiquetaService.findRoots().forEach(e -> root.getChildren().add(toTreeItem(e)));
        etiquetasTreeView.setRoot(root);
        etiquetasTreeView.setShowRoot(false);
    }

    private TreeItem<Etiqueta> toTreeItem(Etiqueta e) {
        TreeItem<Etiqueta> item = new TreeItem<>(e);
        item.setExpanded(true);
        e.getHijos().forEach(hijo -> item.getChildren().add(toTreeItem(hijo)));
        return item;
    }

    // ═══════════════════════════════════════════════════════════════
    // Expediente / Timeline
    // ═══════════════════════════════════════════════════════════════

    private void mostrarExpediente(Contacto c) {
        this.contactoActual = c;
        expedientePanel.setVisible(true);

        contactoNombreLabel.setText(c.getNombre());
        contactoDniLabel.setText("DNI: " + c.getDni());
        contactoDireccionLabel.setText("\uD83D\uDCCD " + c.getDireccion());

        // Foto de perfil
        if (c.getFotoPerfilPath() != null) {
            try { fotoPerfilView.setImage(new Image("file:" + c.getFotoPerfilPath())); }
            catch (Exception e) { fotoPerfilView.setImage(null); }
        } else {
            fotoPerfilView.setImage(null);
        }

        // Campos dinámicos
        camposDinamicosBox.getChildren().clear();
        for (CampoDinamicoValor v : c.getCamposDinamicos()) {
            HBox row = new HBox(8);
            Label lbl = new Label(v.getCampo().getNombre() + ":");
            lbl.setStyle("-fx-text-fill:#9090b0; -fx-min-width:120;");
            Label val = new Label(v.getValor());
            val.setStyle("-fx-text-fill:#e0e0f0;");
            row.getChildren().addAll(lbl, val);
            camposDinamicosBox.getChildren().add(row);
        }

        // Chips de etiquetas
        etiquetasChips.getChildren().clear();
        for (Etiqueta et : c.getEtiquetas()) {
            Label chip = new Label(et.getNombre());
            String bg  = et.getColorHex() != null ? et.getColorHex() : "#6C63FF";
            chip.setStyle(
                "-fx-background-color:" + bg + "; -fx-text-fill:white; " +
                "-fx-background-radius:12; -fx-padding:3 10; -fx-font-size:10px;");
            etiquetasChips.getChildren().add(chip);
        }

        renderTimeline(c);
    }

    private void renderTimeline(Contacto c) {
        timelineContainer.getChildren().clear();
        List<TimelineRecord> records = timelineService.getTimeline(c.getId());

        if (records.isEmpty()) {
            Label empty = new Label("Sin registros en el expediente.");
            empty.setStyle("-fx-text-fill:#606080; -fx-font-style:italic; -fx-padding:20;");
            timelineContainer.getChildren().add(empty);
            return;
        }
        records.forEach(r -> timelineContainer.getChildren().add(crearCard(r)));
    }

    private VBox crearCard(TimelineRecord record) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color:#2a2a3e; -fx-background-radius:8; " +
                      "-fx-border-color:#3d3d5c; -fx-border-radius:8; " +
                      "-fx-border-width:1; -fx-padding:12;");
        card.setMaxWidth(Double.MAX_VALUE);

        // Encabezado
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        String ico = record.getTipo() == TimelineRecordType.NOTA ? "\uD83D\uDCDD" : "\uD83D\uDDBC\uFE0F";
        Label icon  = new Label(ico); icon.setStyle("-fx-font-size:15px;");
        Label title = new Label(record.getTitulo());
        title.setStyle("-fx-font-weight:bold; -fx-text-fill:#e0e0f0; -fx-font-size:13px;");
        Region sp   = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label fecha = new Label(record.getFecha() != null ? record.getFecha().format(DATE_FMT) : "");
        fecha.setStyle("-fx-text-fill:#6060a0; -fx-font-size:10px;");

        Button btnE = iconBtn("\u270F\uFE0F", "-fx-text-fill:#a0a0c0;");
        Button btnB = iconBtn("\uD83D\uDDD1\uFE0F", "-fx-text-fill:#ff6b6b;");
        btnE.setOnAction(e -> handleEditarRecord(record));
        btnB.setOnAction(e -> handleEliminarRecord(record));

        header.getChildren().addAll(icon, title, sp, fecha, btnE, btnB);
        card.getChildren().add(header);

        // Contenido
        if (record.getTipo() == TimelineRecordType.NOTA && record.getContenidoHtml() != null) {
            String plain = record.getContenidoHtml().replaceAll("<[^>]+>","").replace("&nbsp;"," ").trim();
            Label contenido = new Label(plain);
            contenido.setStyle("-fx-text-fill:#c0c0d0; -fx-wrap-text:true;");
            contenido.setWrapText(true);
            contenido.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(contenido);
        }

        // Galería multimedia
        if (!record.getAdjuntos().isEmpty()) {
            FlowPane gallery = new FlowPane(8, 8);
            record.getAdjuntos().forEach(adj -> gallery.getChildren().add(crearMiniMedia(adj)));
            card.getChildren().add(gallery);
        }
        return card;
    }

    private VBox crearMiniMedia(MediaAdjunto adj) {
        VBox box = new VBox(4);
        box.setStyle("-fx-background-color:#1e1e2e; -fx-background-radius:6; -fx-padding:6;");
        box.setMaxWidth(130);

        if (adj.getFilePath() != null) {
            try {
                ImageView thumb = new ImageView(new Image("file:" + adj.getFilePath(), 120, 90, true, true));
                thumb.setFitWidth(120); thumb.setFitHeight(90);
                box.getChildren().add(thumb);
            } catch (Exception ignored) {}
        }
        if (adj.getDescripcion() != null && !adj.getDescripcion().isBlank()) {
            Label d = new Label(adj.getDescripcion());
            d.setStyle("-fx-text-fill:#c0c0d0; -fx-font-size:10px; -fx-wrap-text:true;");
            d.setMaxWidth(120); d.setWrapText(true);
            box.getChildren().add(d);
        }
        if (adj.getLugar() != null && !adj.getLugar().isBlank()) {
            box.getChildren().add(miniLabel("\uD83D\uDCCD " + adj.getLugar()));
        }
        if (adj.getFechaCaptura() != null) {
            box.getChildren().add(miniLabel("\uD83D\uDCC5 " +
                adj.getFechaCaptura().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
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
                etiquetaIds.add(item.getValue().getId());
        });

        SearchCriteriaDTO criteria = SearchCriteriaDTO.builder()
            .texto(searchField.getText())
            .etiquetaIds(etiquetaIds.isEmpty() ? null : etiquetaIds)
            .operador(radioOr.isSelected() ? SearchOperator.OR : SearchOperator.AND)
            .build();

        List<Contacto> resultados = contactoService.search(criteria);
        contactosListView.getItems().setAll(resultados);
        lblContadorContactos.setText(resultados.size() + " contacto" + (resultados.size() != 1 ? "s" : ""));
    }

    @FXML public void handleNuevoContacto()  { abrirFormularioContacto(null); }
    @FXML public void handleEditarContacto() {
        if (contactoActual == null) { aviso("Selecciona un contacto primero."); return; }
        abrirFormularioContacto(contactoActual);
    }

    private void abrirFormularioContacto(Contacto contacto) {
        try {
            SpringFXMLLoader.LoadResult<ContactoFormController> result =
                springFXMLLoader.loadWithController(FxmlView.CONTACTO_FORM.getFxmlPath());
            ContactoFormController ctrl = result.controller();
            if (contacto != null) ctrl.setContacto(contacto);
            ctrl.setOnSuccess(() -> Platform.runLater(() -> {
                cargarContactos();
                if (contacto != null) {
                    mostrarExpediente(contactoService.loadFull(contacto.getId()));
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
            "¿Eliminar a " + contactoActual.getNombre() + "? Se eliminará todo su expediente.");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                contactoService.delete(contactoActual.getId());
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
        dlg.setHeaderText("Nueva nota para " + contactoActual.getNombre());
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
                timelineService.addNota(contactoActual.getId(), t, html);
                mostrarExpediente(contactoActual);
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

        List<MediaAdjunto> adjuntos = new ArrayList<>();
        for (java.io.File file : files) {
            MediaAdjunto adj = pedirMetadatos(file);
            if (adj != null) adjuntos.add(adj);
        }
        if (!adjuntos.isEmpty()) {
            timelineService.addMedia(contactoActual.getId(),
                "Galería (" + adjuntos.size() + " foto(s))", adjuntos);
            mostrarExpediente(contactoActual);
        }
    }

    private MediaAdjunto pedirMetadatos(java.io.File file) {
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
            return MediaAdjunto.builder()
                .filePath(file.getAbsolutePath())
                .nombreArchivo(file.getName())
                .descripcion(desc.getText())
                .lugar(lugar.getText())
                .fechaCaptura(dp.getValue())
                .tipoMime(mime(file.getName()))
                .build();
        }
        return null;
    }

    @FXML public void handleExportarPdf() {
        if (contactoActual == null) { aviso("Selecciona un contacto primero."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar PDF");
        fc.setInitialFileName(contactoActual.getNombre().replaceAll("\\s+","_") + "_expediente.pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
        java.io.File file = fc.showSaveDialog(stageManager.getPrimaryStage());
        if (file == null) return;
        try {
            byte[] pdf = exportImportService.exportContactoToPdf(contactoActual.getId());
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
            Files.write(file.toPath(), exportImportService.generarPlantillaExcel());
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
            var result = exportImportService.importarDesdeExcel(is);
            cargarContactos();
            String msg = result.getResumen();
            if (!result.getMensajesError().isEmpty())
                msg += "\n\nErrores:\n" + String.join("\n", result.getMensajesError());
            info("Importación completada\n" + msg);
        } catch (Exception e) { error(e.getMessage()); }
    }

    private void handleEditarRecord(TimelineRecord record) {
        if (record.getTipo() != TimelineRecordType.NOTA) {
            aviso("Solo se pueden editar notas de texto.");
            return;
        }
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Editar Nota");
        dlg.initOwner(stageManager.getPrimaryStage());
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        VBox content = new VBox(10); content.setPadding(new Insets(15)); content.setPrefWidth(520);
        TextField titulo = new TextField(record.getTitulo());
        TextArea cuerpo  = new TextArea();
        cuerpo.setWrapText(true); cuerpo.setPrefHeight(200);
        if (record.getContenidoHtml() != null)
            cuerpo.setText(record.getContenidoHtml().replaceAll("<[^>]+>","").trim());
        content.getChildren().addAll(new Label("Título:"), titulo, new Label("Contenido:"), cuerpo);
        dlg.getDialogPane().setContent(content);

        dlg.showAndWait().ifPresent(res -> {
            if (res == guardar) {
                timelineService.updateNota(record.getId(), titulo.getText(),
                    "<p>" + cuerpo.getText().replace("\n","<br/>") + "</p>");
                mostrarExpediente(contactoActual);
            }
        });
    }

    private void handleEliminarRecord(TimelineRecord record) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar '" + record.getTitulo() + "'?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                timelineService.deleteRecord(record.getId());
                mostrarExpediente(contactoActual);
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
