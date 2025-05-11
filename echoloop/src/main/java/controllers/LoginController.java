package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import services.UserService;
import model.User;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final UserService userService = new UserService();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        // Call the userService to verify login
        User user = userService.loginUser(username, password);

        if (user != null) {
            if ("client".equals(user.getRole())) {
                statusLabel.setText("Login successful! Redirecting to client page...");
                // Redirect to the client page
                handleGoToClientPage();  // Define this method to handle navigation
            } else {
                statusLabel.setText("Login successful, but you're an administrator.");
                handleGoToAdminPage();
            };
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void handleGoToSignup(ActionEvent event) {
        try {
            // Load the signup FXML
            Parent root = FXMLLoader.load(getClass().getResource("/views/signup.fxml"));

            // Get the stage from the event source (the button that was clicked)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new Scene with the loaded FXML
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/auth.css").toExternalForm());
            // Set the new Scene on the current Stage
            currentStage.setScene(scene);
            currentStage.setTitle("Sign Up");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // You might want to display an error message to the user here
        }
    }
    private void handleGoToClientPage() {
        try {
            // Load the client page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/create_post.fxml"));
            Parent root = loader.load();

            // Get the stage and set the scene
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/frontpostcss.css").toExternalForm());
            currentStage.setScene(scene);
            currentStage.setTitle("Client Dashboard");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading the client page.");
        }
    }
    private void handleGoToAdminPage() {
        try {
            // Load the client page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/userManagement.fxml"));
            Parent root = loader.load();

            // Get the stage and set the scene
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/userManagement.css").toExternalForm());
            currentStage.setScene(scene);
            currentStage.setTitle("Client Dashboard");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading the client page.");
        }
    }
}
