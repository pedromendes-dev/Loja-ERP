package com.example.ERP.tools;

import com.example.erp_.service.AuthService;
import com.example.erp_.security.Session;

public class AuthTest {
    public static void main(String[] args) throws Exception {
        AuthService auth = new AuthService();
        System.out.println("Registering default admin if needed...");
        auth.registerDefaultAdminIfNeeded();
        System.out.println("Attempting login with admin/admin123...");
        boolean ok = auth.login("admin","admin123");
        System.out.println("Login ok: " + ok);
        if (ok) {
            System.out.println("Session user: " + Session.getInstance().getUsernameSafe());
        }
    }
}
