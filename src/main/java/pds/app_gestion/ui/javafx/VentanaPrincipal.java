package pds.app_gestion.ui.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pds.app_gestion.application.service.ServicioTablero;
import pds.app_gestion.application.service.ServicioTarjeta;

/**
 * Ventana principal de la aplicación JavaFX.
 * Proporciona una interfaz de usuario para gestionar tableros y tarjetas.
 */
public class VentanaPrincipal extends Application {

    private ServicioTablero servicioTablero;
    private ServicioTarjeta servicioTarjeta;

    private static VentanaPrincipal instancia;

    @Override
    public void start(Stage ventanaPrincipal) {
        try {
            instancia = this;
            
            // Crear interfaz principal
            BorderPane raiz = new BorderPane();
            raiz.setPadding(new Insets(10));

            // Barra de menú
            MenuBar menuBar = crearMenuBar();
            raiz.setTop(menuBar);

            // Panel principal
            TabPane tabPane = crearPanelPrincipal();
            raiz.setCenter(tabPane);

            // Escena
            Scene escena = new Scene(raiz, 1200, 800);
            ventanaPrincipal.setTitle("App Gestión de Trabajo Colaborativo");
            ventanaPrincipal.setScene(escena);
            ventanaPrincipal.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MenuBar crearMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menú Archivo
        Menu menuArchivo = new Menu("Archivo");
        MenuItem salir = new MenuItem("Salir");
        salir.setOnAction(e -> System.exit(0));
        menuArchivo.getItems().add(salir);

        // Menú Tablero
        Menu menuTablero = new Menu("Tablero");
        MenuItem crearTablero = new MenuItem("Crear nuevo tablero");
        crearTablero.setOnAction(e -> mostrarDialogoCrearTablero());
        menuTablero.getItems().add(crearTablero);

        // Menú Ayuda
        Menu menuAyuda = new Menu("Ayuda");
        MenuItem acercaDe = new MenuItem("Acerca de");
        acercaDe.setOnAction(e -> mostrarAcercaDe());
        menuAyuda.getItems().add(acercaDe);

        menuBar.getMenus().addAll(menuArchivo, menuTablero, menuAyuda);
        return menuBar;
    }

    private TabPane crearPanelPrincipal() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab de Mis Tableros
        Tab tabMisTableros = new Tab("Mis Tableros");
        tabMisTableros.setContent(crearVistaMisTableros());

        // Tab de Tableros Compartidos
        Tab tabCompartidos = new Tab("Compartidos");
        tabCompartidos.setContent(crearVistaTablerosCompartidos());

        tabPane.getTabs().addAll(tabMisTableros, tabCompartidos);
        return tabPane;
    }

    private VBox crearVistaMisTableros() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titulo = new Label("Mis Tableros");
        titulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        ListView<String> listaTableros = new ListView<>();
        listaTableros.setPrefHeight(400);
        listaTableros.getItems().addAll(
            "Proyecto A",
            "Proyecto B",
            "Tareas Personales"
        );

        Button btnCrear = new Button("+ Crear Tablero");
        btnCrear.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnCrear.setOnAction(e -> mostrarDialogoCrearTablero());

        vbox.getChildren().addAll(titulo, listaTableros, btnCrear);
        return vbox;
    }

    private VBox crearVistaTablerosCompartidos() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titulo = new Label("Tableros Compartidos");
        titulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        ListView<String> listaCompartidos = new ListView<>();
        listaCompartidos.setPrefHeight(400);
        listaCompartidos.getItems().addAll(
            "Proyecto Equipo (compartido por juan@example.com)",
            "Tareas Comunes (compartido por maria@example.com)"
        );

        vbox.getChildren().addAll(titulo, listaCompartidos);
        return vbox;
    }

    private void mostrarDialogoCrearTablero() {
        Dialog<String> dialogo = new Dialog<>();
        dialogo.setTitle("Crear Nuevo Tablero");
        dialogo.setHeaderText("Ingresa los datos del nuevo tablero");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Título");
        TextArea txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción");
        txtDescripcion.setPrefRowCount(3);
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Tu email");

        grid.add(new Label("Título:"), 0, 0);
        grid.add(txtTitulo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDescripcion, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // Llamar al servicio para crear tablero
                    // servicioTablero.crearTablero(new CrearTableroRequest(...));
                    mostrarAlerta("Éxito", "Tablero creado correctamente");
                    return "Tablero creado";
                } catch (Exception e) {
                    mostrarError("Error", "No se pudo crear el tablero: " + e.getMessage());
                }
            }
            return null;
        });

        dialogo.showAndWait();
    }

    private void mostrarAcercaDe() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Acerca de");
        alerta.setHeaderText("App Gestión de Trabajo Colaborativo");
        alerta.setContentText("Versión 1.0.0\n\nAplicación de gestión de tableros de tareas.\n\n" +
            "Desarrollado por: Adrian Martinez Zamora\n" +
            "Arquitectura: Hexagonal + DDD\n" +
            "Framework: Spring Boot + JavaFX");
        alerta.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public static VentanaPrincipal getInstance() {
        return instancia;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
