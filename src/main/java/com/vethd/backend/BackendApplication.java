package com.vethd.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@SpringBootApplication
public class BackendApplication {

    static {
        loadDotenv();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8081");

        System.out.println("\n=======================================================");
        System.out.println("  Backend de VetHD corriendo exitosamente!");
        System.out.println("  URL Local: http://localhost:" + port);
        System.out.println("=======================================================\n");

        log.info("Backend de VetHD corriendo exitosamente en http://localhost:{}", port);
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
