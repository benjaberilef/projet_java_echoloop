package services;

import Main.DatabaseConnection;
import model.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class postService {

    private static Connection cnx = DatabaseConnection.getInstance().getCnx();

    public boolean savePost(Post post) {
        String sql = "INSERT INTO posts (user_id, image_path, description) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, post.getUserId());
            stmt.setString(2, post.getImagePath());
            stmt.setString(3, post.getDescription());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM posts ORDER BY id DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("image_path"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at")
                );
                posts.add(post);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    public boolean deletePost(int id) {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePost(Post post) {
        String sql = "UPDATE posts SET description = ?, image_path = ? WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, post.getDescription());
            stmt.setString(2, post.getImagePath());
            stmt.setInt(3, post.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
