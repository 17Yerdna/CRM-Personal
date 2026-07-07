package com.crm.personal.presentation;

import com.crm.personal.presentation.javafx.SpringFXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Gestor centralizado de ventanas JavaFX.
 * Permite cambiar la escena principal y abrir modales con controladores Spring.
 */
@Component
public class StageManager {

    private static final Logger log = LoggerFactory.getLogger(StageManager.class);

    private final SpringFXMLLoader fxmlLoader;
    private Stage primaryStage;

    public StageManager(SpringFXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /** Reemplaza la escena del Stage principal. */
    public void switchScene(FxmlView view) {
        try {
            Parent root = fxmlLoader.load(view.getFxmlPath());
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                applyStyles(scene);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            primaryStage.setTitle(view.getTitle());
            primaryStage.show();
        } catch (IOException e) {
            log.error("Error cargando vista {}: {}", view.getFxmlPath(), e.getMessage(), e);
            throw new RuntimeException("No se pudo cargar: " + view.getFxmlPath(), e);
        }
    }

    /** Abre un Stage modal (showAndWait). */
    public Stage openModal(FxmlView view) {
        try {
            Parent root = fxmlLoader.load(view.getFxmlPath());
            Stage modal = buildModal(view.getTitle(), root);
            modal.showAndWait();
            return modal;
        } catch (IOException e) {
            log.error("Error abriendo modal {}: {}", view.getFxmlPath(), e.getMessage(), e);
            throw new RuntimeException("No se pudo abrir: " + view.getFxmlPath(), e);
        }
    }

    /** Abre un modal y devuelve el controlador tipado. */
    public <T> SpringFXMLLoader.LoadResult<T> openModalWithController(FxmlView view) {
        try {
            SpringFXMLLoader.LoadResult<T> result =
                    fxmlLoader.loadWithController(view.getFxmlPath());
            Stage modal = buildModal(view.getTitle(), result.root());
            modal.showAndWait();
            return result;
        } catch (IOException e) {
            log.error("Error abriendo modal {}: {}", view.getFxmlPath(), e.getMessage(), e);
            throw new RuntimeException("No se pudo abrir: " + view.getFxmlPath(), e);
        }
    }

    // ── Privados ─────────────────────────────────────────────────────────────

    private Stage buildModal(String title, Parent root) {
        Stage modal = new Stage();
        modal.setTitle(title);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(primaryStage);
        Scene scene = new Scene(root);
        applyStyles(scene);
        modal.setScene(scene);
        return modal;
    }

    private void applyStyles(Scene scene) {
        var css = getClass().getResource("/css/styles.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
