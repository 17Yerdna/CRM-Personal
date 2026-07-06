package com.crm.personal.infrastructure.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

/**
 * Cargador FXML consciente de Spring.
 *
 * Usa {@code ApplicationContext::getBean} como {@code controllerFactory} del
 * {@code FXMLLoader}, de modo que los controladores son beans de Spring y
 * pueden recibir {@code @Autowired} normalmente.
 */
@Component
public class SpringFXMLLoader {

    private final ApplicationContext context;

    public SpringFXMLLoader(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Carga un FXML y devuelve su raíz.
     */
    public Parent load(String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("FXML no encontrado en el classpath: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(context::getBean);
        return loader.load();
    }

    /**
     * Carga un FXML y devuelve tanto la raíz como el controlador tipado.
     */
    public <T> LoadResult<T> loadWithController(String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("FXML no encontrado en el classpath: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();
        T controller = loader.getController();
        return new LoadResult<>(root, controller);
    }

    /** Contenedor inmutable de raíz + controlador. */
    public record LoadResult<T>(Parent root, T controller) {}
}
