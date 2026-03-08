package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("./Backend").load();

        if (dotenv.get("JUMPCLOUD_CLIENT_ID") != null) {
            System.setProperty("JUMPCLOUD_CLIENT_ID", dotenv.get("JUMPCLOUD_CLIENT_ID"));
            System.setProperty("JUMPCLOUD_CLIENT_SECRET", dotenv.get("JUMPCLOUD_CLIENT_SECRET"));
        }
        System.out.println("ID JUMPCLOUD : " + System.getProperty("JUMPCLOUD_CLIENT_ID"));
        SpringApplication.run(BackendApplication.class, args);
    }
    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        System.out.println("=== INITIALISATION MANUELLE ET FORCÉE DE FLYWAY ===");
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

}
