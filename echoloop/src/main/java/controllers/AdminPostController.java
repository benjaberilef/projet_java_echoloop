package controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import model.Post;
import services.postService;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminPostController implements Initializable {

    @FXML private TableView<Post> postsTable;
    @FXML private TableColumn<Post, Long> idColumn;
    @FXML private TableColumn<Post, String> userColumn;
    @FXML private TableColumn<Post, String> contentColumn;
    @FXML private TableColumn<Post, String> dateColumn;
    @FXML private TableColumn<Post, Boolean> hasImageColumn;
    @FXML private TableColumn<Post, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label totalPostsLabel;
    @FXML private Label filteredPostsLabel;
    @FXML private Label statusLabel;

    private postService postService;
    private ObservableList<Post> allPosts;
    private FilteredList<Post> filteredPosts;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        postService = new postService();
        initializeTable();
        initializeComboBoxes();
        loadPosts();
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userColumn.setCellValueFactory(cellData -> {
            // Since we only have userId, we could fetch username or just display the ID
            return new SimpleStringProperty("User " + cellData.getValue().getUserId());
        });
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().toLocalDateTime().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });

        hasImageColumn.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().getImagePath() != null && !cellData.getValue().getImagePath().isEmpty()));

        // Custom boolean cell that shows "Yes" or "No" instead of checkbox
        hasImageColumn.setCellFactory(col -> new TableCell<Post, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        // Set up the actions column with view and delete buttons
        Callback<TableColumn<Post, Void>, TableCell<Post, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Post, Void> call(final TableColumn<Post, Void> param) {
                return new TableCell<>() {
                    private final Button viewButton = new Button("View");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttonBox = new HBox(5, viewButton, deleteButton);

                    {
                        viewButton.getStyleClass().add("first-button");
                        deleteButton.getStyleClass().add("second-button");
                        buttonBox.setAlignment(Pos.CENTER);

                        viewButton.setOnAction(event -> {
                            Post post = getTableView().getItems().get(getIndex());
                            showPostDetails(post);
                        });

                        deleteButton.setOnAction(event -> {
                            Post post = getTableView().getItems().get(getIndex());
                            handleDeletePost(post);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonBox);
                        }
                    }
                };
            }
        };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void initializeComboBoxes() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "All Posts",
                "With Images",
                "Without Images"
        );
        filterComboBox.setItems(filterOptions);
        filterComboBox.getSelectionModel().selectFirst();

        // Add listener to update filtered posts when filter changes
        filterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    @FXML
    private void handleRefresh() {
        loadPosts();
        statusLabel.setText("Posts refreshed successfully!");
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        filterComboBox.getSelectionModel().selectFirst();
        applyFilters();
    }

    @FXML
    private void handleExport() {
        // Implementation for exporting data
        statusLabel.setText("Export feature not implemented yet.");
    }

    private void loadPosts() {
        try {
            List<Post> posts = postService.getAllPosts();
            allPosts = FXCollections.observableArrayList(posts);
            filteredPosts = new FilteredList<>(allPosts, p -> true);
            postsTable.setItems(filteredPosts);

            updatePostCountLabels();
            statusLabel.setText("Posts loaded successfully!");
        } catch (Exception e) {
            statusLabel.setText("Error loading posts: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String filterOption = filterComboBox.getValue();

        filteredPosts.setPredicate(post -> {
            // First apply search filter
            boolean matchesSearch = searchText.isEmpty()
                    || post.getDescription().toLowerCase().contains(searchText)
                    || String.valueOf(post.getUserId()).contains(searchText);

            if (!matchesSearch) {
                return false;
            }

            // Then apply combo box filter
            if (filterOption.equals("All Posts")) {
                return true;
            } else if (filterOption.equals("With Images")) {
                return post.getImagePath() != null && !post.getImagePath().isEmpty();
            } else if (filterOption.equals("Without Images")) {
                return post.getImagePath() == null || post.getImagePath().isEmpty();
            }

            return true;
        });

        updatePostCountLabels();
    }

    private void updatePostCountLabels() {
        totalPostsLabel.setText("Total Posts: " + allPosts.size());
        filteredPostsLabel.setText("Showing: " + filteredPosts.size());
    }

    private void showPostDetails(Post post) {
        // Implementation for showing post details in a dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Post Details");
        dialog.setHeaderText("Post ID: " + post.getId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                new Label("User ID: " + post.getUserId()),
                new Label("Description: " + post.getDescription()),
                new Label("Created: " + (post.getCreatedAt() != null ? post.getCreatedAt().toString() : "N/A")),
                new Label("Has Image: " + (post.getImagePath() != null && !post.getImagePath().isEmpty() ? "Yes" : "No"))
        );

        // Add image if it exists
        if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
            try {
                // Create image view
                ImageView imageView = new ImageView(new Image(new File(post.getImagePath()).toURI().toString()));
                imageView.setFitWidth(300);  // Set preferred width
                imageView.setPreserveRatio(true);  // Maintain aspect ratio

                // Create a scrollable container in case the image is large
                ScrollPane scrollPane = new ScrollPane(imageView);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefHeight(200);  // Set preferred height for the scroll pane

                // Add image to the content
                content.getChildren().add(new Label("Image:"));
                content.getChildren().add(scrollPane);
            } catch (Exception e) {
                // Handle image loading error
                content.getChildren().add(new Label("Error loading image: " + e.getMessage()));
            }
        }

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.getDialogPane().setContent(content);

        // Make the dialog resizable for better image viewing
        dialog.setResizable(true);

        dialog.showAndWait();
    }

    private void handleDeletePost(Post post) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Post");
        confirmDialog.setHeaderText("Delete Post #" + post.getId());
        confirmDialog.setContentText("Are you sure you want to delete this post? This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postService.deletePost(post.getId());
                allPosts.remove(post);
                updatePostCountLabels();
                statusLabel.setText("Post #" + post.getId() + " deleted successfully!");
            } catch (Exception e) {
                statusLabel.setText("Error deleting post: " + e.getMessage());
            }
        }
    }
}