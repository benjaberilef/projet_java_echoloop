package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarController {

    @FXML private Button homeBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;
    @FXML private Button voiceBtn;
    @FXML
    private void handleHome(ActionEvent event) {
        try {
        Parent root = FXMLLoader.load(getClass().getResource("/views/userManagement.fxml"));

        // Get the stage from the event source (the button that was clicked)
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Create a new Scene with the loaded FXML
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/views/userManagement.css").toExternalForm());
        // Set the new Scene on the current Stage
        currentStage.setScene(scene);
        currentStage.setTitle("Users Management");
        currentStage.show();
    } catch (
    IOException e) {
        e.printStackTrace();
        // You might want to display an error message to the user here
    }
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/adminpost.fxml"));

            // Get the stage from the event source (the button that was clicked)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new Scene with the loaded FXML
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/adminpost.css").toExternalForm());
            // Set the new Scene on the current Stage
            currentStage.setScene(scene);
            currentStage.setTitle("Users Management");
            currentStage.show();
        } catch (
                IOException e) {
            e.printStackTrace();
            // You might want to display an error message to the user here
        }
    }
    @FXML
    private void voice(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/AdminVoiceComment.fxml"));

            // Get the stage from the event source (the button that was clicked)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new Scene with the loaded FXML
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/Voice.css").toExternalForm());
            // Set the new Scene on the current Stage
            currentStage.setScene(scene);
            currentStage.setTitle("Comment Management");
            currentStage.show();
        } catch (
                IOException e) {
            e.printStackTrace();
            // You might want to display an error message to the user here
        }
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));

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
}
