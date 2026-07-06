package com.crm.personal.presentation.javafx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class GlobalFxExceptionHandler {

    private GlobalFxExceptionHandler() {
    }

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (Platform.isFxApplicationThread()) {
                showErrorDialog(throwable);
            } else {
                Platform.runLater(() -> showErrorDialog(throwable));
            }
        });
    }

    private static void showErrorDialog(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR, userFriendlyMessage(throwable), ButtonType.OK);
        alert.setTitle("Error inesperado");
        alert.setHeaderText("Ocurrió un problema en la aplicación");
        alert.showAndWait();
    }

    private static String userFriendlyMessage(Throwable throwable) {
        return switch (throwable) {
            case IllegalArgumentException ex -> ex.getMessage();
            case SecurityException ex -> "La operación fue bloqueada por seguridad.";
            default -> "La operación no pudo completarse. Revisa los datos o intenta nuevamente.";
        };
    }
}
