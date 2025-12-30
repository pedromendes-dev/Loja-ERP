package com.example.erp_.service;

import com.example.erp_.dao.impl.SqliteUserDao;
import com.example.erp_.model.User;
import com.example.erp_.security.Session;
import com.example.erp_.utils.PasswordUtils;

public class AuthService {
    private SqliteUserDao userDao = new SqliteUserDao();

    public boolean login(String username, String password) throws Exception {
        User user = userDao.findByUsername(username);
        if (user == null) return false;
        if (!user.isActive()) return false;
        boolean ok = PasswordUtils.verifyPassword(password, user.getPasswordHash());
        if (ok) {
            Session.getInstance().setCurrentUser(user);
        }
        return ok;
    }

    public void registerDefaultAdminIfNeeded() throws Exception {
        User admin = userDao.findByUsername("admin");
        if (admin == null) {
            createAdmin("admin", "admin123");
        }
    }

    public boolean isAdminMissing() throws Exception {
        return userDao.findByUsername("admin") == null;
    }

    public void createAdmin(String username, String password) throws Exception {
        User u = new User();
        u.setUsername(username);
        byte[] salt = PasswordUtils.generateSalt();
        u.setPasswordHash(PasswordUtils.hashPassword(password, salt));
        u.setRole("ADMIN");
        u.setActive(true);
        userDao.save(u);
    }
}
