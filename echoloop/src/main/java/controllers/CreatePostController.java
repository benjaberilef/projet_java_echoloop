package controllers;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.application.Platform;

import model.Post;
import model.VoiceComment;
import services.postService;
import services.VoiceCommentService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.*;

public class CreatePostController {

    @FXML private TextField descriptionField;
    @FXML private ImageView imagePreview;
    @FXML private VBox postsContainer;
    @FXML private Label statusLabel;
    @FXML private Button submitButton;

    private File selectedImageFile;
    private final int currentUserId = 2;
    private final postService postService = new postService();
    private final VoiceCommentService voiceCommentService=new VoiceCommentService();

    private Post postBeingEdited = null;

    // Voice recording fields
    private File tempVoiceFile;
    private TargetDataLine audioLine;
    private boolean isRecording = false;
    private Thread recordingThread;


    @FXML
    public void initialize() {
        loadPosts();
    }

    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    @FXML
    public void handleSubmitPost() {
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            statusLabel.setText("Please enter a description.");
            return;
        }

        String imagePath = null;

        if (selectedImageFile != null) {
            try {
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) uploadsDir.mkdirs();
                File dest = new File(uploadsDir, System.currentTimeMillis() + "_" + selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = dest.getAbsolutePath();
            } catch (IOException e) {
                statusLabel.setText("Failed to save image.");
                e.printStackTrace();
                return;
            }
        }

        boolean success;
        if (postBeingEdited != null) {
            // Edit existing post
            postBeingEdited.setDescription(description);
            if (imagePath != null) {
                postBeingEdited.setImagePath(imagePath); // Only update if a new image was selected
            }
            success = postService.updatePost(postBeingEdited);
            postBeingEdited = null;
            submitButton.setText("Create Post");
        } else {
            // Create new post
            Post newPost = new Post(currentUserId, imagePath, description);
            success = postService.savePost(newPost);
        }

        if (success) {
            statusLabel.setText("Post saved successfully!");
            descriptionField.clear();
            imagePreview.setImage(null);
            selectedImageFile = null;
            loadPosts();
        } else {
            statusLabel.setText("Failed to save post.");
        }
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();
        List<Post> posts = postService.getAllPosts();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Post post : posts) {
            VBox postBox = new VBox(10);
            postBox.getStyleClass().add("post-container");
            postBox.setPadding(new Insets(15));
            postBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");

            // Post Header
            HBox header = new HBox(10);
            Label userLabel = new Label("User ID: " + post.getUserId());
            Label timeLabel = new Label(post.getCreatedAt().toLocalDateTime().format(formatter));
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button editBtn = new Button("Edit");
            Button delBtn = new Button("Delete");
            Button detailsBtn = new Button("Details");

            detailsBtn.setOnAction(e -> showPostDetails(post));

            delBtn.setOnAction(e -> {
                postService.deletePost(post.getId());
                loadPosts();
            });

            editBtn.setOnAction(e -> {
                // Populate fields for editing
                descriptionField.setText(post.getDescription());
                if (post.getImagePath() != null) {
                    File imgFile = new File(post.getImagePath());
                    if (imgFile.exists()) {
                        imagePreview.setImage(new Image(imgFile.toURI().toString()));
                        selectedImageFile = null; // Do not re-copy unless user picks a new image
                    }
                } else {
                    imagePreview.setImage(null);
                    selectedImageFile = null;
                }
                postBeingEdited = post;
                submitButton.setText("Update Post");
                statusLabel.setText("Editing post...");
            });

            header.getChildren().addAll(userLabel, timeLabel, spacer, detailsBtn, editBtn, delBtn);
            postBox.getChildren().add(header);

            // Description
            Label descLabel = new Label(post.getDescription());
            descLabel.setWrapText(true);
            postBox.getChildren().add(descLabel);

            // Image
            if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
                ImageView imageView = new ImageView(new Image(new File(post.getImagePath()).toURI().toString()));
                imageView.setFitWidth(300);
                imageView.setPreserveRatio(true);
                postBox.getChildren().add(imageView);
            }

            // Voice Comments Section
            try {
                List<VoiceComment> voiceComments = voiceCommentService.getVoiceCommentsByPostId(post.getId());
                if (!voiceComments.isEmpty()) {
                    Label commentsHeader = new Label("Voice Comments (" + voiceComments.size() + "):");
                    commentsHeader.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 0 0;");
                    postBox.getChildren().add(commentsHeader);

                    VBox commentsContainer = new VBox(5);
                    commentsContainer.setStyle("-fx-padding: 5 0 0 0;");

                    for (VoiceComment comment : voiceComments) {
                        HBox commentBox = createVoiceCommentBox(comment);
                        commentsContainer.getChildren().add(commentBox);
                    }

                    postBox.getChildren().add(commentsContainer);
                }
            } catch (SQLException e) {
                System.err.println("Error loading voice comments: " + e.getMessage());
                e.printStackTrace();
            }

            // Add Voice Comment Button
            Button addVoiceCommentBtn = new Button("Add Voice Comment");
            addVoiceCommentBtn.setOnAction(e -> showVoiceCommentDialog(post.getId()));
            postBox.getChildren().add(addVoiceCommentBtn);

            postsContainer.getChildren().add(postBox);
        }
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

        // Add voice comments section
        try {
            List<VoiceComment> voiceComments = voiceCommentService.getVoiceCommentsByPostId(post.getId());
            if (!voiceComments.isEmpty()) {
                Label commentsHeader = new Label("Voice Comments (" + voiceComments.size() + "):");
                commentsHeader.setStyle("-fx-font-weight: bold;");
                content.getChildren().add(commentsHeader);

                VBox commentsContainer = new VBox(10);
                for (VoiceComment comment : voiceComments) {
                    HBox commentBox = createVoiceCommentBox(comment);
                    commentsContainer.getChildren().add(commentBox);
                }

                content.getChildren().add(commentsContainer);
            }
        } catch (SQLException e) {
            content.getChildren().add(new Label("Error loading voice comments: " + e.getMessage()));
        }

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.getDialogPane().setContent(content);

        // Make the dialog resizable for better image viewing
        dialog.setResizable(true);

        dialog.showAndWait();
    }

    private HBox createVoiceCommentBox(VoiceComment comment) {
        HBox commentBox = new HBox(10);
        commentBox.setStyle("-fx-background-color: #e1e1e1; -fx-padding: 10; -fx-border-radius: 5;");

        Label userLabel = new Label("User " + comment.getUserId());
        userLabel.setMinWidth(60);

        Button playButton = new Button("Play");
        Button stopButton = new Button("Stop");
        stopButton.setDisable(true);

        AtomicBoolean isPlaying = new AtomicBoolean(false);
        final MediaPlayer[] mediaPlayer = {null};

        playButton.setOnAction(e -> {
            try {
                if (mediaPlayer[0] != null) {
                    mediaPlayer[0].stop();
                }

                File audioFile = new File(comment.getAudioPath());
                Media media = new Media(audioFile.toURI().toString());
                mediaPlayer[0] = new MediaPlayer(media);

                mediaPlayer[0].setOnReady(() -> {
                    playButton.setDisable(true);
                    stopButton.setDisable(false);
                    isPlaying.set(true);
                    mediaPlayer[0].play();
                });

                mediaPlayer[0].setOnEndOfMedia(() -> {
                    playButton.setDisable(false);
                    stopButton.setDisable(true);
                    isPlaying.set(false);
                    mediaPlayer[0].seek(Duration.ZERO);
                    mediaPlayer[0].stop();
                });

                mediaPlayer[0].setOnError(() -> {
                    playButton.setDisable(false);
                    stopButton.setDisable(true);
                    isPlaying.set(false);
                    statusLabel.setText("Error playing audio.");
                });

            } catch (Exception ex) {
                statusLabel.setText("Error playing voice comment: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        stopButton.setOnAction(e -> {
            if (mediaPlayer[0] != null && isPlaying.get()) {
                mediaPlayer[0].stop();
                playButton.setDisable(false);
                stopButton.setDisable(true);
                isPlaying.set(false);
            }
        });

        // Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            try {
                if (voiceCommentService.deleteVoiceComment(comment.getId())) {
                    // Remove from UI
                    ((VBox) commentBox.getParent()).getChildren().remove(commentBox);
                    statusLabel.setText("Voice comment deleted successfully.");

                    // If we're in the dialog, we need to refresh the whole posts list
                    if (commentBox.getScene() != null && commentBox.getScene().getWindow() != null) {
                        Window window = commentBox.getScene().getWindow();
                        if (window instanceof Stage && ((Stage) window).getModality() != Modality.NONE) {
                            Platform.runLater(this::loadPosts);
                        }
                    }
                } else {
                    statusLabel.setText("Failed to delete voice comment.");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error deleting voice comment: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Date/time label
        Label dateLabel = new Label(comment.getCreatedAt() != null ?
                comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) :
                "");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        commentBox.getChildren().addAll(userLabel, playButton, stopButton, spacer, dateLabel, deleteButton);

        return commentBox;
    }

    private void showVoiceCommentDialog(int postId) {
        Dialog<VoiceComment> dialog = new Dialog<>();
        dialog.setTitle("Add Voice Comment");
        dialog.setHeaderText("Record a voice comment for post #" + postId);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Record/Stop buttons
        HBox buttonBox = new HBox(10);
        Button recordButton = new Button("Start Recording");
        Button stopButton = new Button("Stop Recording");
        stopButton.setDisable(true);
        buttonBox.getChildren().addAll(recordButton, stopButton);

        // Status label
        Label recordingStatus = new Label("Not recording");

        // Preview player (shows up after recording)
        HBox previewBox = new HBox(10);
        Button previewButton = new Button("Preview");
        Button previewStopButton = new Button("Stop Preview");
        previewButton.setDisable(true);
        previewStopButton.setDisable(true);
        previewBox.getChildren().addAll(previewButton, previewStopButton);

        content.getChildren().addAll(buttonBox, recordingStatus, previewBox);

        // Record button handler
        recordButton.setOnAction(e -> {
            try {
                startRecording();
                recordButton.setDisable(true);
                stopButton.setDisable(false);
                previewButton.setDisable(true);
                recordingStatus.setText("Recording...");
            } catch (Exception ex) {
                recordingStatus.setText("Error starting recording: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Stop button handler
        stopButton.setOnAction(e -> {
            stopRecording();
            recordButton.setDisable(false);
            stopButton.setDisable(true);
            previewButton.setDisable(false);
            recordingStatus.setText("Recording stopped. Click Preview to listen.");
        });

        // Preview functionality
        final MediaPlayer[] previewPlayer = {null};

        previewButton.setOnAction(e -> {
            try {
                if (tempVoiceFile != null && tempVoiceFile.exists()) {
                    if (previewPlayer[0] != null) {
                        previewPlayer[0].stop();
                    }

                    Media media = new Media(tempVoiceFile.toURI().toString());
                    previewPlayer[0] = new MediaPlayer(media);

                    previewPlayer[0].setOnReady(() -> {
                        previewButton.setDisable(true);
                        previewStopButton.setDisable(false);
                        recordingStatus.setText("Playing preview...");
                        previewPlayer[0].play();
                    });

                    previewPlayer[0].setOnEndOfMedia(() -> {
                        previewButton.setDisable(false);
                        previewStopButton.setDisable(true);
                        recordingStatus.setText("Preview finished.");
                        previewPlayer[0].seek(Duration.ZERO);
                        previewPlayer[0].stop();
                    });
                }
            } catch (Exception ex) {
                recordingStatus.setText("Error playing preview: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        previewStopButton.setOnAction(e -> {
            if (previewPlayer[0] != null) {
                previewPlayer[0].stop();
                previewButton.setDisable(false);
                previewStopButton.setDisable(true);
                recordingStatus.setText("Preview stopped.");
            }
        });

        // Dialog buttons
        ButtonType saveButtonType = new ButtonType("Save Comment", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Disable the save button until a recording is made
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Enable save button once recording is completed
        stopButton.setOnAction(e -> {
            stopRecording();
            recordButton.setDisable(false);
            stopButton.setDisable(true);
            previewButton.setDisable(false);
            saveButton.setDisable(false);
            recordingStatus.setText("Recording stopped. Click Preview to listen or Save to submit.");
        });

        dialog.getDialogPane().setContent(content);

        // Handle save button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType && tempVoiceFile != null && tempVoiceFile.exists()) {
                try {
                    // Copy temp file to permanent location
                    File voiceDir = new File("voice_comments");
                    if (!voiceDir.exists()) voiceDir.mkdirs();

                    String fileName = System.currentTimeMillis() + "_comment.wav";
                    File destFile = new File(voiceDir, fileName);

                    Files.copy(tempVoiceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Create and return voice comment
                    return new VoiceComment(postId, currentUserId, destFile.getAbsolutePath());
                } catch (Exception ex) {
                    recordingStatus.setText("Error saving voice comment: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            return null;
        });

        // Process the result
        dialog.showAndWait().ifPresent(voiceComment -> {
            try {
                VoiceComment savedComment = voiceCommentService.createVoiceComment(voiceComment);
                if (savedComment != null) {
                    statusLabel.setText("Voice comment added successfully!");
                    loadPosts(); // Refresh posts to show new comment
                } else {
                    statusLabel.setText("Failed to add voice comment.");
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error adding voice comment: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Cleanup temp file when dialog closes
        dialog.setOnCloseRequest(e -> {
            if (isRecording) {
                stopRecording();
            }

            if (tempVoiceFile != null && tempVoiceFile.exists()) {
                try {
                    tempVoiceFile.delete();
                } catch (Exception ex) {
                    System.err.println("Error deleting temp file: " + ex.getMessage());
                }
            }
        });
    }

    private void startRecording() {
        try {
            AudioFormat format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    44100, // sample rate
                    16,    // sample size in bits
                    2,     // channels
                    4,     // frame size
                    44100, // frame rate
                    false  // big endian
            );

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                throw new Exception("Audio line not supported!");
            }

            audioLine = (TargetDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();

            // Create temp file
            tempVoiceFile = File.createTempFile("voice_recording_", ".wav");
            tempVoiceFile.deleteOnExit();

            isRecording = true;

            // Start recording in a thread
            recordingThread = new Thread(() -> {
                AudioInputStream audioStream = new AudioInputStream(audioLine);
                try {
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, tempVoiceFile);
                } catch (IOException e) {
                    // Handle exception
                    System.err.println("Error writing audio file: " + e.getMessage());
                }
            });

            recordingThread.start();

        } catch (Exception e) {
            System.err.println("Error starting recording: " + e.getMessage());
            e.printStackTrace();

            if (audioLine != null) {
                audioLine.close();
            }
            isRecording = false;
        }
    }

    private void stopRecording() {
        if (isRecording && audioLine != null) {
            isRecording = false;
            audioLine.stop();
            audioLine.close();

            // Wait for recording thread to finish
            if (recordingThread != null) {
                try {
                    recordingThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}