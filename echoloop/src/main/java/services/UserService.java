package services;
import Main.DatabaseConnection;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static Connection cnx = DatabaseConnection.getInstance().getCnx();  // Using the DatabaseConnection singleton

    // Register a new user
    public boolean registerUser(String username, String password, String email) {
        String checkUserSql = "SELECT * FROM users WHERE username = ?";  // Check if username already exists
        String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";  // Insert role

        try (PreparedStatement checkStmt = cnx.prepareStatement(checkUserSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false;  // Username already exists
            }

            try (PreparedStatement insertStmt = cnx.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);  // In a real-world app, hash this!
                insertStmt.setString(3, email);// Insert the role

                int rowsAffected = insertStmt.executeUpdate();
                return rowsAffected > 0;  // If rows affected, the insert was successful
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Registration failed due to an error
        }
    }

    // Login a user by checking username and password
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Retrieve the hashed password stored in the database
                String storedHash = rs.getString("password");
                String role = rs.getString("role");  // Retrieve the role

                // Verify the entered password with the stored hash using bcrypt
                if (BCrypt.checkpw(password, storedHash)) {
                    int id = rs.getInt("id");
                    String email = rs.getString("email");
                    return new User(id, username, storedHash, email, role);  // Return the user with role
                } else {
                    // Password doesn't match
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Invalid login if username not found or password doesn't match
    }



    // Check if a user already exists by username
    public boolean userExists(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // If result exists, the username is taken
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // In case of error, assume user doesn't exist
    }

    // Get a user by their ID
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String role = rs.getString("role");
                return new User(userId, username, password, email, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // User not found
    }
    public boolean deleteUser(int userId) {
        String deleteUserSql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(deleteUserSql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String selectSql = "SELECT * FROM users";

        try (PreparedStatement stmt = cnx.prepareStatement(selectSql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String role = rs.getString("role");
                users.add(new User(id, username, "", email, role));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    public boolean updateUser(User user) {
        // First check if the user exists
        User existingUser = getUserById(user.getId());
        if (existingUser == null) {
            return false; // User not found
        }

        String sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // Already hashed in controller
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            stmt.setInt(5, user.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
