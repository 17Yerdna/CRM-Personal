package com.crm.personal.presentation.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class LoginController {

    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    private String enteredPassword;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        passwordField.setOnAction(e -> handleLogin(null));
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String pwd = passwordField.getText();
        if (pwd == null || pwd.isBlank()) {
            errorLabel.setText("La contraseña no puede estar vacía.");
            errorLabel.setVisible(true);
            return;
        }
        enteredPassword = pwd;
        ((Stage) loginButton.getScene().getWindow()).close();
    }

    public String getEnteredPassword() {
        return enteredPassword;
    }
}
