package com.example.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.backend"})
public class BackendApplication {

    public static void main(String[] args) {
        System.out.println("ID JUMPCLOUD : " + System.getenv("JUMPCLOUD_CLIENT_ID"));
        SpringApplication.run(BackendApplication.class, args);
    }
    @Bean
    public CommandLineRunner beanLister(ApplicationContext ctx) {
        return args -> {
            System.out.println("--- LISTE DES ENDPOINTS DÉTECTÉS ---");
            ctx.getBeansWithAnnotation(RestController.class).forEach((name, bean) -> {
                System.out.println("Bean trouvé : " + name + " (Classe: " + bean.getClass().getName() + ")");
            });
            System.out.println("------------------------------------");
        };
    }

}
