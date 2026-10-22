package com.vethd.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@SpringBootApplication
public class BackendApplication {

    static {
        loadDotenv();
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    private static void loadDotenv() {
        File envFile = new File(".env");
        if (envFile.exists()) {
            try {
                Files.readAllLines(envFile.toPath()).forEach(line -> {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int index = line.indexOf('=');
                        String key = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                });
            } catch (IOException ignored) {
            }
        }
    }
}
