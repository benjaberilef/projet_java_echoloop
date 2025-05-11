package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import services.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    private final UserService userService = new UserService();

    @FXML
    public void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Please fill out all fields.");
            return;
        }

        // Hash the password using bcrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        boolean success = userService.registerUser(username, hashedPassword, email);

        if (success) {
            statusLabel.setText("Registration successful!");
            // Optionally, auto-login after successful signup
            // Redirect to login screen or home screen
        } else {
            statusLabel.setText("Username already taken.");
        }
    }

    @FXML
    public void handleGoToLogin(ActionEvent event) {
            try {
                // Load the signup FXML
                Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));

                // Get the stage from the event source (the button that was clicked)
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Create a new Scene with the loaded FXML
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/views/auth.css").toExternalForm());
                // Set the new Scene on the current Stage
                currentStage.setScene(scene);
                currentStage.setTitle("Login");
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                // You might want to display an error message to the user here
            }
        }
}
