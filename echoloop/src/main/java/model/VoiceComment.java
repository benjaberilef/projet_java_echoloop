package model;
import java.time.LocalDateTime;

/**
 * Model class for voice comments in the application.
 * Represents voice comments associated with posts.
 */
public class VoiceComment {
    private int id;
    private int postId;
    private int userId;
    private String audioPath;
    private LocalDateTime createdAt;

    // Default constructor
    public VoiceComment() {
    }

    // Constructor with all fields except id and createdAt (for new voice comments)
    public VoiceComment(int postId, int userId, String audioPath) {
        this.postId = postId;
        this.userId = userId;
        this.audioPath = audioPath;
    }

    // Constructor with all fields (for retrieving from database)
    public VoiceComment(int id, int postId, int userId, String audioPath, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.audioPath = audioPath;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "VoiceComment{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", audioPath='" + audioPath + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}