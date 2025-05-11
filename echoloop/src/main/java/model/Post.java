package model;
import javafx.scene.control.Cell;

import java.sql.Timestamp;

public class Post {
    private int id;
    private int userId;
    private String imagePath;
    private String description;
    private Timestamp createdAt;

    public Post(int id, int userId, String imagePath, String description, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.imagePath = imagePath;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Post(int userId, String imagePath, String description) {
        this.userId = userId;
        this.imagePath = imagePath;
        this.description = description;
    }

    // Getters and Setters

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
