package services;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Main.DatabaseConnection;
import model.VoiceComment;
/**
 * Service class for handling database operations related to voice comments.
 */
public class VoiceCommentService {
    private static Connection cnx = DatabaseConnection.getInstance().getCnx();




    public VoiceComment createVoiceComment(VoiceComment voiceComment) throws SQLException {
        String sql = "INSERT INTO voice_comments (post_id, user_id, audio_path) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, voiceComment.getPostId());
            stmt.setInt(2, voiceComment.getUserId());
            stmt.setString(3, voiceComment.getAudioPath());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating voice comment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    voiceComment.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating voice comment failed, no ID obtained.");
                }
            }

            // Get the created timestamp from the database
            String getTimestampSql = "SELECT created_at FROM voice_comments WHERE id = ?";
            try (PreparedStatement timeStmt = cnx.prepareStatement(getTimestampSql)) {
                timeStmt.setInt(1, voiceComment.getId());
                ResultSet rs = timeStmt.executeQuery();
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    voiceComment.setCreatedAt(timestamp != null ? timestamp.toLocalDateTime() : null);
                }
            }
        }

        return voiceComment;
    }

    /**
     * Retrieves a voice comment by its ID.
     *
     * @param id The ID of the voice comment to retrieve
     * @return The voice comment, or null if not found
     * @throws SQLException If a database error occurs
     */
    public VoiceComment getVoiceCommentById(int id) throws SQLException {
        String sql = "SELECT * FROM voice_comments WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVoiceComment(rs);
                }
            }
        }

        return null;
    }

    /**
     * Retrieves all voice comments for a specific post.
     *
     * @param postId The ID of the post
     * @return List of voice comments for the post
     * @throws SQLException If a database error occurs
     */
    public List<VoiceComment> getVoiceCommentsByPostId(int postId) throws SQLException {
        List<VoiceComment> voiceComments = new ArrayList<>();
        String sql = "SELECT * FROM voice_comments WHERE post_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    voiceComments.add(mapResultSetToVoiceComment(rs));
                }
            }
        }

        return voiceComments;
    }

    /**
     * Retrieves all voice comments by a specific user.
     *
     * @param userId The ID of the user
     * @return List of voice comments by the user
     * @throws SQLException If a database error occurs
     */
    public List<VoiceComment> getVoiceCommentsByUserId(int userId) throws SQLException {
        List<VoiceComment> voiceComments = new ArrayList<>();
        String sql = "SELECT * FROM voice_comments WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    voiceComments.add(mapResultSetToVoiceComment(rs));
                }
            }
        }

        return voiceComments;
    }

    /**
     * Updates a voice comment in the database.
     * Note: This method only updates the audio path as other fields typically shouldn't change.
     *
     * @param voiceComment The voice comment to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateVoiceComment(VoiceComment voiceComment) throws SQLException {
        String sql = "UPDATE voice_comments SET audio_path = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, voiceComment.getAudioPath());
            stmt.setInt(2, voiceComment.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Deletes a voice comment from the database.
     *
     * @param id The ID of the voice comment to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteVoiceComment(int id) throws SQLException {
        String sql = "DELETE FROM voice_comments WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Maps a ResultSet row to a VoiceComment object.
     *
     * @param rs The ResultSet to map
     * @return A new VoiceComment object
     * @throws SQLException If a database error occurs
     */
    private VoiceComment mapResultSetToVoiceComment(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int postId = rs.getInt("post_id");
        int userId = rs.getInt("user_id");
        String audioPath = rs.getString("audio_path");

        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new VoiceComment(id, postId, userId, audioPath, createdAt);
    }

    /**
     * Count the number of voice comments for a specific post.
     *
     * @param postId The ID of the post
     * @return The number of voice comments
     * @throws SQLException If a database error occurs
     */
    public int countVoiceCommentsByPostId(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM voice_comments WHERE post_id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }
}