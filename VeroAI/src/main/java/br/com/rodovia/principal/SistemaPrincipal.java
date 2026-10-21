package br.com.rodovia.principal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "br.com.rodovia")
@EntityScan("br.com.rodovia.modelo")
@EnableJpaRepositories("br.com.rodovia.repository")
public class SistemaPrincipal {

    public static void main(String[] args) {
        SpringApplication.run(SistemaPrincipal.class, args);
    }
}
