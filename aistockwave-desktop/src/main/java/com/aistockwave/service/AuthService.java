package com.aistockwave.service;

import com.aistockwave.database.UserDAO;
import com.aistockwave.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private static User currentUser; // Tracks the currently logged-in user

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public boolean register(String fullName, String email, String password) {
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }

        // Check if user already exists
        if (userDAO.getUserByEmail(email) != null) {
            return false;
        }

        // Hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User newUser = new User();
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPasswordHash(hashedPassword);
        newUser.setBalance(100000.0); // Default virtual cash ₹100,000
        newUser.setAdmin(false);

        return userDAO.createUser(newUser);
    }

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.getUserByEmail(email.trim().toLowerCase());
        if (user == null) {
            return null;
        }

        // Check password matching
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            currentUser = user;
            return user;
        }

        return null;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean updatePassword(int userId, String currentPassword, String newPassword) {
        // Fetch user from DB to verify current password
        User user = null;
        for (User u : userDAO.getAllUsers()) {
            if (u.getId() == userId) {
                user = u;
                break;
            }
        }
        if (user == null) return false;

        if (BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            boolean success = userDAO.updatePassword(userId, newHash);
            if (success && currentUser != null && currentUser.getId() == userId) {
                currentUser.setPasswordHash(newHash);
            }
            return success;
        }
        return false;
    }
}
