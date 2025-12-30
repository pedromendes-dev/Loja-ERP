package com.example.erp_.config;

import java.nio.file.Files;
import java.nio.file.Path;

public class AppConfig {
    public static final String APP_NAME = "Loja";
    public static final String APP_TITLE = APP_NAME;
    public static final String APP_VERSION = "0.1.0";

    // Allow overriding DB file via JVM property -Dminierp.db=/path/to/minierp.db
    private static final String OVERRIDE_DB = System.getProperty("minierp.db");

    private static final String LOCAL_DB_FOLDER = System.getProperty("user.dir") + "/.minierp";
    private static final String HOME_DB_FOLDER = System.getProperty("user.home") + "/.minierp";

    public static final String DB_FOLDER = OVERRIDE_DB != null ? Path.of(OVERRIDE_DB).getParent().toString()
            : (Files.exists(Path.of(LOCAL_DB_FOLDER)) ? LOCAL_DB_FOLDER : HOME_DB_FOLDER);
    public static final String DB_FILE = OVERRIDE_DB != null ? OVERRIDE_DB : (DB_FOLDER + "/minierp.db");
    public static final String SCHEMA_RESOURCE = "/db/schema.sql";
    public static final String THEME_LIGHT = "/css/theme-light.css";
    public static final String THEME_DARK = "/css/theme-dark.css";
}
