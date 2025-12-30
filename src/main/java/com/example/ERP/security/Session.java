package com.example.erp_.security;

import com.example.erp_.model.User;

public class Session {
    private static Session instance;
    private User currentUser;

    private Session() {
    }

    public static synchronized Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void clear() {
        this.currentUser = null;
    }

    // Utilitários defensivos
    public boolean isLoggedIn() {
        return this.currentUser != null;
    }

    public String getUsernameSafe() {
        return this.currentUser != null ? this.currentUser.getUsername() : null;
    }
}
