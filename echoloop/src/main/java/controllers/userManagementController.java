package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.User;
import org.mindrot.jbcrypt.BCrypt;
import services.UserService;

import java.util.Optional;

public class userManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> actionsColumn;

    private final UserService userService = new UserService();
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    public void initialize() {
        // Initialize the table columns
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        usernameColumn.setMaxWidth(1f * Integer.MAX_VALUE * 40); // 40% width
        emailColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);    // 30%
        roleColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);     // 15%
        actionsColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);  // 15%

        // Populate the table with users from the database
        loadUsers();

        // Set up action column with buttons for updating and deleting users
        actionsColumn.setCellFactory(column -> new TableCell<User, String>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");

            {
                // Style buttons
                updateButton.getStyleClass().add("update-button");
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.getStyleClass().add("second-button");
                updateButton.getStyleClass().add("first-button");
                // Create HBox with spacing
                HBox buttons = new HBox(5, updateButton, deleteButton);
                buttons.getStyleClass().add("action-buttons");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, updateButton, deleteButton));

                    updateButton.setOnAction(e -> handleUpdateUser(getTableRow().getItem()));
                    deleteButton.setOnAction(e -> handleDeleteUser(getTableRow().getItem()));
                }
            }
        });
    }

    private void loadUsers() {
        // Get all users from the database
        usersList.setAll(userService.getAllUsers());
        userTable.setItems(usersList);
    }

    @FXML
    private void handleUpdateUser(User selectedUser) {
        if (selectedUser != null) {
            // Create a custom dialog for editing user details
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Update User");
            dialog.setHeaderText("Edit details for user: " + selectedUser.getUsername());

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form fields and labels
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField usernameField = new TextField(selectedUser.getUsername());
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Enter new password (leave blank to keep current)");
            TextField emailField = new TextField(selectedUser.getEmail());

            ComboBox<String> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll("admin", "client");
            roleComboBox.setValue(selectedUser.getRole());

            grid.add(new Label("Username:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Password:"), 0, 1);
            grid.add(passwordField, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(emailField, 1, 2);
            grid.add(new Label("Role:"), 0, 3);
            grid.add(roleComboBox, 1, 3);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the username field by default
            usernameField.requestFocus();

            // Add validation to the dialog
            final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Basic validation
                if (usernameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                    Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                    validationAlert.setTitle("Validation Error");
                    validationAlert.setHeaderText(null);
                    validationAlert.setContentText("Username and email cannot be empty!");
                    validationAlert.showAndWait();
                    event.consume(); // Prevent dialog from closing
                }

                // Email validation
                String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
                if (!emailField.getText().matches(emailRegex)) {
                    Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                    validationAlert.setTitle("Validation Error");
                    validationAlert.setHeaderText(null);
                    validationAlert.setContentText("Please enter a valid email address!");
                    validationAlert.showAndWait();
                    event.consume(); // Prevent dialog from closing
                }
            });

            // Convert the result to a user when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Handle password - either keep existing or hash the new one
                    String password;
                    if (passwordField.getText().isEmpty()) {
                        // Keep existing password
                        password = selectedUser.getPassword();
                    } else {
                        // Hash the new password with BCrypt
                        password = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(12));
                    }

                    // Create a new User object with updated values
                    User updatedUser = new User(
                            selectedUser.getId(),
                            usernameField.getText(),
                            password,
                            emailField.getText(),
                            roleComboBox.getValue()
                    );
                    return updatedUser;
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<User> result = dialog.showAndWait();

            result.ifPresent(updatedUser -> {
                // Update the user in the database
                boolean success = userService.updateUser(updatedUser);

                if (success) {
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("User Updated");
                    alert.setHeaderText(null);
                    alert.setContentText("User information has been updated successfully.");
                    alert.showAndWait();

                    // Reload users to refresh the table
                    loadUsers();
                } else {
                    // Show error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Update Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to update user information. Please try again.");
                    alert.showAndWait();
                }
            });
        }
    }

    @FXML
    private void handleDeleteUser(User selectedUser) {
        if (selectedUser != null) {
            // Show a confirmation dialog before deleting the user
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User");
            alert.setHeaderText("Are you sure you want to delete the user: " + selectedUser.getUsername() + "?");
            alert.setContentText("This action cannot be undone.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = userService.deleteUser(selectedUser.getId());  // Delete the user from DB

                    if (success) {
                        loadUsers();  // Reload the user list after deletion
                    } else {
                        // Show error message if deletion fails
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Deletion Failed");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Failed to delete user. Please try again.");
                        errorAlert.showAndWait();
                    }
                }
            });
        }
    }
}