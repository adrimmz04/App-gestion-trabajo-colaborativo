package pds.app_gestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import pds.app_gestion.ui.javafx.VentanaPrincipal;

/**
 * Clase principal que inicia la aplicación Spring Boot e integra JavaFX.
 * 
 * Esta clase actúa como punto de entrada de la aplicación,
 * permitiendo que SpringBoot y JavaFX convivan en la misma aplicación.
 */
@SpringBootApplication
public class Application {

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        // Iniciar Spring Boot context en un hilo separado
        Thread springThread = new Thread(() -> {
            applicationContext = SpringApplication.run(Application.class, args);
        });
        springThread.setDaemon(true);
        springThread.start();

        // Dar tiempo a Spring para inicializar
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Lanzar JavaFX
        javafx.application.Application.launch(VentanaPrincipal.class);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
