package pds.app_gestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import pds.app_gestion.ui.javafx.VentanaPrincipal;

/**
 * Clase principal que inicia la aplicación Spring Boot e integra JavaFX.
 * 
 * Esta clase actúa como punto de entrada de la aplicación,
 * permitiendo que SpringBoot y JavaFX convivan en la misma aplicación.
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        javafx.application.Application.launch(VentanaPrincipal.class, args);
    }
}
