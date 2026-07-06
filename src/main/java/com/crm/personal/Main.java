package com.crm.personal;

import com.crm.personal.infrastructure.security.MasterPassword;
import com.crm.personal.infrastructure.security.MasterPasswordBootstrapVerifier;
import com.crm.personal.presentation.FxmlView;
import com.crm.personal.presentation.StageManager;
import com.crm.personal.presentation.javafx.GlobalFxExceptionHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Arrays;

/**
 * Punto de entrada JavaFX de la aplicación.
 *
 * <p><b>Flujo de arranque:</b>
 * <ol>
 *   <li>JavaFX inicia; se muestra un diálogo de login ANTES de que Spring comience.</li>
 *   <li>Se verifica la contraseña maestra contra el hash local antes de iniciar Spring.</li>
 *   <li>La contraseña se registra como bean singleton mediante {@link SpringApplicationBuilder#initializers}.</li>
 *   <li>Spring Boot inicia su contexto y {@code DatabaseConfig} recibe la contraseña por DI.</li>
 *   <li>Se muestra la ventana principal.</li>
 * </ol>
 */
public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private ConfigurableApplicationContext springContext;
    private MasterPassword masterPassword;

    public static void main(String[] args) {
        GlobalFxExceptionHandler.install();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            char[] password = requestMasterPassword(primaryStage);

            if (password == null) {
                Platform.exit();
                return;
            }

            MasterPasswordBootstrapVerifier.verifyOrInitialize(password);
            this.masterPassword = new MasterPassword(password);
            Arrays.fill(password, '\0');

            springContext = new SpringApplicationBuilder(CrmApplication.class)
                    .headless(false)
                    .initializers(context -> context.getBeanFactory().registerSingleton("masterPassword", masterPassword))
                    .run();

            StageManager stageManager = springContext.getBean(StageManager.class);
            stageManager.setPrimaryStage(primaryStage);
            primaryStage.setMinWidth(1050);
            primaryStage.setMinHeight(680);
            stageManager.switchScene(FxmlView.MAIN);

        } catch (Exception e) {
            log.error("Error fatal en el arranque de la aplicación", e);
            mostrarError("Error de inicio",
                "No se pudo iniciar CRM Personal:\n" + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        if (masterPassword != null) {
            masterPassword.clear();
        }
        Platform.exit();
    }

    // ─── Diálogos pre-Spring ──────────────────────────────────────────────────

    private char[] requestMasterPassword(Stage owner) {
        return MasterPasswordBootstrapVerifier.isFirstRun()
                ? mostrarDialogoCrearPassword(owner)
                : mostrarDialogoLogin(owner);
    }

    private char[] mostrarDialogoLogin(Stage owner) {
        Stage dialog = buildDialog(owner, "CRM Personal \u2014 Acceso");

        // ── ícono + título en card ──────────────────────────────
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(72, 72);
        iconBox.setStyle("-fx-background-color:#d4eca2; -fx-background-radius:16;");
        Label iconLbl = new Label("\uD83D\uDD10");
        iconLbl.setStyle("-fx-font-size:28px;");
        iconBox.getChildren().add(iconLbl);

        Label titulo = label("CRM Personal",
            "-fx-font-size:22px; -fx-font-weight:600; -fx-text-fill:#1e1b16;");
        Label sub = label("Ingresa tu contrase\u00f1a maestra",
            "-fx-text-fill:#46483f; -fx-font-size:14px;");

        Label fieldLbl = label("Contrase\u00f1a maestra",
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#46483f;");

        PasswordField pass = passwordField("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");
        Label error = label("", "-fx-text-fill:#ba1a1a; -fx-font-size:12px;");
        error.setVisible(false);
        Button btnOk = primaryButton("Acceder");

        final char[][] result = { null };
        Runnable tryLogin = () -> {
            if (pass.getText().isBlank()) {
                error.setText("La contrase\u00f1a no puede estar vac\u00eda.");
                error.setVisible(true);
            } else {
                result[0] = pass.getText().toCharArray();
                dialog.close();
            }
        };
        btnOk.setOnAction(e -> tryLogin.run());
        pass.setOnAction(e -> tryLogin.run());

        show(dialog, 380, iconBox, titulo, sub, fieldLbl, pass, error, btnOk);
        return result[0];
    }

    private char[] mostrarDialogoCrearPassword(Stage owner) {
        Stage dialog = buildDialog(owner, "CRM Personal \u2014 Configuraci\u00f3n inicial");

        // Header
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(72, 72);
        iconBox.setStyle("-fx-background-color:#d4eca2; -fx-background-radius:16;");
        Label iconLbl = new Label("\uD83D\uDDDD\uFE0F");
        iconLbl.setStyle("-fx-font-size:28px;");
        iconBox.getChildren().add(iconLbl);

        Label titulo = label("Bienvenido a CRM Personal",
            "-fx-font-size:20px; -fx-font-weight:600; -fx-text-fill:#1e1b16;");

        // Subtexto con warning en rojo
        Label sub = label("Crea tu contrase\u00f1a maestra para proteger tus datos.",
            "-fx-text-fill:#46483f; -fx-font-size:13px;");
        sub.setMaxWidth(320);
        sub.setWrapText(true);

        Label warning = label("Gu\u00e1rdala en un lugar seguro \u2014 no puede recuperarse.",
            "-fx-text-fill:#ba1a1a; -fx-font-size:12px; -fx-font-weight:600;");
        warning.setWrapText(true);
        warning.setMaxWidth(320);

        Label lbl1 = label("Nueva contrase\u00f1a",
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#46483f;");
        PasswordField pass1 = passwordField("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");

        Label lbl2 = label("Confirmar contrase\u00f1a",
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#46483f;");
        PasswordField pass2 = passwordField("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");

        Label error = label("", "-fx-text-fill:#ba1a1a; -fx-font-size:12px;");
        error.setVisible(false);
        Button btnOk = primaryButton("Crear y continuar");

        final char[][] result = { null };
        btnOk.setOnAction(e -> {
            String p1 = pass1.getText();
            String p2 = pass2.getText();
            if (p1.length() < 6) {
                error.setText("La contrase\u00f1a debe tener al menos 6 caracteres.");
                error.setVisible(true);
            } else if (!p1.equals(p2)) {
                error.setText("Las contrase\u00f1as no coinciden.");
                error.setVisible(true);
            } else {
                result[0] = p1.toCharArray();
                dialog.close();
            }
        });

        show(dialog, 400, iconBox, titulo, sub, warning, lbl1, pass1, lbl2, pass2, error, btnOk);
        return result[0];
    }

    // ─── Helpers de UI ────────────────────────────────────────────────────────

    private Stage buildDialog(Stage owner, String title) {
        Stage d = new Stage();
        d.initModality(Modality.APPLICATION_MODAL);
        d.initOwner(owner);
        d.setTitle(title);
        d.setResizable(false);
        return d;
    }

    private void show(Stage dialog, double width, javafx.scene.Node... nodes) {
        VBox box = new VBox(12, nodes);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(36, 40, 36, 40));
        // Tarjeta blanca sobre fondo crema (nuevo tema claro)
        box.setStyle("-fx-background-color:#ffffff; " +
                     "-fx-border-color:#c7c7bc; " +
                     "-fx-border-radius:10; " +
                     "-fx-background-radius:10;");
        box.setPrefWidth(width);

        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color:#FDFBF7;");
        root.setPadding(new Insets(32));

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private Label label(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        l.setWrapText(true);
        return l;
    }

    private PasswordField passwordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setMaxWidth(280);
        pf.setStyle("-fx-background-color:#2d2d3f; -fx-text-fill:white; " +
                    "-fx-border-color:#4a4a6a; -fx-border-radius:4; " +
                    "-fx-background-radius:4; -fx-padding:9 12; -fx-font-size:13px;");
        return pf;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(280);
        b.setStyle("-fx-background-color:#6c63ff; -fx-text-fill:white; " +
                   "-fx-font-weight:bold; -fx-font-size:13px; " +
                   "-fx-padding:9 0; -fx-background-radius:4; -fx-cursor:hand;");
        return b;
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
