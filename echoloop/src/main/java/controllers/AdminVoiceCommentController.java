package controllers;

import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Callback;
import model.VoiceComment;
import services.VoiceCommentService;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminVoiceCommentController implements Initializable {

    @FXML private TableView<VoiceComment> voiceCommentsTable;
    @FXML private TableColumn<VoiceComment, Integer> idColumn;
    @FXML private TableColumn<VoiceComment, String> userColumn;
    @FXML private TableColumn<VoiceComment, String> postColumn;
    @FXML private TableColumn<VoiceComment, String> dateColumn;
    @FXML private TableColumn<VoiceComment, String> audioPathColumn;
    @FXML private TableColumn<VoiceComment, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label totalCommentsLabel;
    @FXML private Label filteredCommentsLabel;
    @FXML private Label statusLabel;

    private VoiceCommentService voiceCommentService;
    private ObservableList<VoiceComment> allVoiceComments;
    private FilteredList<VoiceComment> filteredVoiceComments;
    private MediaPlayer currentMediaPlayer;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        voiceCommentService = new VoiceCommentService();
        initializeTable();
        initializeComboBoxes();
        loadVoiceComments();
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        userColumn.setCellValueFactory(cellData -> {
            // Since we only have userId, we could fetch username or just display the ID
            return new SimpleStringProperty("User " + cellData.getValue().getUserId());
        });

        postColumn.setCellValueFactory(cellData -> {
            // Since we only have postId, we could fetch post title or just display the ID
            return new SimpleStringProperty("Post " + cellData.getValue().getPostId());
        });

        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });

        audioPathColumn.setCellValueFactory(new PropertyValueFactory<>("audioPath"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        // Set up the actions column with play, view details and delete buttons
        Callback<TableColumn<VoiceComment, Void>, TableCell<VoiceComment, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<VoiceComment, Void> call(final TableColumn<VoiceComment, Void> param) {
                return new TableCell<>() {
                    private final Button playButton = new Button("Play");
                    private final Button viewButton = new Button("View");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttonBox = new HBox(5, playButton, viewButton, deleteButton);

                    {
                        playButton.getStyleClass().add("play-button");
                        viewButton.getStyleClass().add("view-button");
                        deleteButton.getStyleClass().add("delete-button");
                        buttonBox.setAlignment(Pos.CENTER);

                        playButton.setOnAction(event -> {
                            VoiceComment comment = getTableView().getItems().get(getIndex());
                            playAudio(comment);
                        });

                        viewButton.setOnAction(event -> {
                            VoiceComment comment = getTableView().getItems().get(getIndex());
                            showVoiceCommentDetails(comment);
                        });

                        deleteButton.setOnAction(event -> {
                            VoiceComment comment = getTableView().getItems().get(getIndex());
                            handleDeleteVoiceComment(comment);
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
                "All Comments",
                "By Post ID",
                "By User ID"
        );
        filterComboBox.setItems(filterOptions);
        filterComboBox.getSelectionModel().selectFirst();

        // Add listener to update filtered comments when filter changes
        filterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    @FXML
    private void handleRefresh() {
        loadVoiceComments();
        statusLabel.setText("Voice comments refreshed successfully!");
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

    private void loadVoiceComments() {
        try {
            // In a real application, you might want to load all voice comments across all posts,
            // but for simplicity in this example, we'll just get those for a specific post or user
            List<VoiceComment> allComments = FXCollections.observableArrayList();

            // Get all voice comments (this might need to be implemented in the service)
            // For now, we'll just simulate it by getting voice comments for post with ID 1
            try {
                // You would need to implement a method to get all voice comments
                // This is just a placeholder approach
                for (int i = 1; i <= 10; i++) {
                    allComments.addAll(voiceCommentService.getVoiceCommentsByPostId(i));
                }
            } catch (SQLException e) {
                System.err.println("Error loading voice comments: " + e.getMessage());
            }

            allVoiceComments = FXCollections.observableArrayList(allComments);
            filteredVoiceComments = new FilteredList<>(allVoiceComments, p -> true);
            voiceCommentsTable.setItems(filteredVoiceComments);

            updateCommentCountLabels();
            statusLabel.setText("Voice comments loaded successfully!");
        } catch (Exception e) {
            statusLabel.setText("Error loading voice comments: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String filterOption = filterComboBox.getValue();

        filteredVoiceComments.setPredicate(comment -> {
            // Apply search filter based on the selected filter option
            boolean matchesSearch = false;

            if (searchText.isEmpty()) {
                matchesSearch = true;
            } else {
                switch (filterOption) {
                    case "All Comments":
                        matchesSearch = String.valueOf(comment.getId()).contains(searchText) ||
                                String.valueOf(comment.getUserId()).contains(searchText) ||
                                String.valueOf(comment.getPostId()).contains(searchText) ||
                                (comment.getAudioPath() != null && comment.getAudioPath().toLowerCase().contains(searchText));
                        break;
                    case "By Post ID":
                        matchesSearch = String.valueOf(comment.getPostId()).contains(searchText);
                        break;
                    case "By User ID":
                        matchesSearch = String.valueOf(comment.getUserId()).contains(searchText);
                        break;
                    default:
                        matchesSearch = true;
                        break;
                }
            }

            return matchesSearch;
        });

        updateCommentCountLabels();
    }

    private void updateCommentCountLabels() {
        totalCommentsLabel.setText("Total Comments: " + allVoiceComments.size());
        filteredCommentsLabel.setText("Showing: " + filteredVoiceComments.size());
    }

    private void playAudio(VoiceComment comment) {
        try {
            // Stop any currently playing audio
            if (currentMediaPlayer != null) {
                currentMediaPlayer.stop();
            }

            File audioFile = new File(comment.getAudioPath());
            if (!audioFile.exists()) {
                statusLabel.setText("Audio file not found: " + comment.getAudioPath());
                return;
            }

            Media media = new Media(audioFile.toURI().toString());
            currentMediaPlayer = new MediaPlayer(media);
            currentMediaPlayer.play();

            statusLabel.setText("Playing voice comment #" + comment.getId());

            // Auto-stop when done
            currentMediaPlayer.setOnEndOfMedia(() -> {
                currentMediaPlayer.stop();
                statusLabel.setText("Playback finished");
            });
        } catch (Exception e) {
            statusLabel.setText("Error playing audio: " + e.getMessage());
        }
    }

    private void showVoiceCommentDetails(VoiceComment comment) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Voice Comment Details");
        dialog.setHeaderText("Voice Comment ID: " + comment.getId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                new Label("User ID: " + comment.getUserId()),
                new Label("Post ID: " + comment.getPostId()),
                new Label("Created: " + (comment.getCreatedAt() != null ? comment.getCreatedAt().format(dateFormatter) : "N/A")),
                new Label("Audio Path: " + comment.getAudioPath())
        );

        // Add audio controls
        try {
            File audioFile = new File(comment.getAudioPath());
            if (audioFile.exists()) {
                Media media = new Media(audioFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);

                Button playButton = new Button("Play");
                Button pauseButton = new Button("Pause");
                Button stopButton = new Button("Stop");

                playButton.setOnAction(e -> mediaPlayer.play());
                pauseButton.setOnAction(e -> mediaPlayer.pause());
                stopButton.setOnAction(e -> mediaPlayer.stop());

                HBox controls = new HBox(10, playButton, pauseButton, stopButton);
                controls.setAlignment(Pos.CENTER);

                content.getChildren().addAll(
                        new Label("Audio Controls:"),
                        mediaView,
                        controls
                );

                // Clean up when dialog is closed
                dialog.setOnCloseRequest(e -> mediaPlayer.dispose());
            } else {
                content.getChildren().add(new Label("Audio file not found"));
            }
        } catch (Exception e) {
            content.getChildren().add(new Label("Error loading audio: " + e.getMessage()));
        }

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.getDialogPane().setContent(content);
        dialog.setResizable(true);

        dialog.showAndWait();
    }

    private void handleDeleteVoiceComment(VoiceComment comment) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Voice Comment");
        confirmDialog.setHeaderText("Delete Voice Comment #" + comment.getId());
        confirmDialog.setContentText("Are you sure you want to delete this voice comment? This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = voiceCommentService.deleteVoiceComment(comment.getId());
                if (success) {
                    allVoiceComments.remove(comment);
                    updateCommentCountLabels();
                    statusLabel.setText("Voice comment #" + comment.getId() + " deleted successfully!");
                } else {
                    statusLabel.setText("Failed to delete voice comment #" + comment.getId());
                }
            } catch (Exception e) {
                statusLabel.setText("Error deleting voice comment: " + e.getMessage());
            }
        }
    }
}