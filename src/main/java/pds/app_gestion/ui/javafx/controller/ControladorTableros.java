package pds.app_gestion.ui.javafx.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pds.app_gestion.application.dto.CrearTableroRequest;
import pds.app_gestion.application.dto.CrearTarjetaRequest;
import pds.app_gestion.application.dto.CrearListaRequest;
import pds.app_gestion.application.dto.TableroResponse;
import pds.app_gestion.application.service.ServicioTablero;
import pds.app_gestion.application.service.ServicioTarjeta;

/**
 * Controlador JavaFX para gestionar vistas de tableros.
 * 
 * Proporciona métodos para:
 * - Crear y eliminar tableros
 * - Agregar y gestionar tarjetas
 * - Gestionar usuarios compartidos
 */
@Component
public class ControladorTableros {

    @Autowired
    private ServicioTablero servicioTablero;

    @Autowired
    private ServicioTarjeta servicioTarjeta;

    /**
     * Obtiene todos los tableros del propietario especificado.
     * 
     * @param propietarioEmail email del propietario
     * @return lista observable de tableros
     */
    public ObservableList<TableroResponse> obtenerTablerosPropietario(String propietarioEmail) {
        try {
            var tableros = servicioTablero.obtenerTablerosPropietario(propietarioEmail);
            return FXCollections.observableArrayList(tableros);
        } catch (Exception e) {
            mostrarError("Error", "No se pudieron cargar los tableros: " + e.getMessage());
            return FXCollections.observableArrayList();
        }
    }

/**
     * Obtiene todos los tableros compartidos con el usuario especificado.
     * 
     * @param usuarioEmail email del usuario
     * @return lista observable de tableros compartidos
     */
    public ObservableList<TableroResponse> obtenerTablerosCompartidos(String usuarioEmail) {
        try {
            var tableros = servicioTablero.obtenerTablerosCompartidos(usuarioEmail);
            return FXCollections.observableArrayList(tableros);
        } catch (Exception e) {
            mostrarError("Error", "No se pudieron cargar los tableros compartidos: " + e.getMessage());
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Crea un nuevo tablero.
     * 
     * @param titulo título del tablero
     * @param descripcion descripción del tablero
     * @param propietarioEmail email del propietario
     * @return true si se creó correctamente
     */
    public boolean crearTablero(String titulo, String descripcion, String propietarioEmail) {
        try {
            CrearTableroRequest request = CrearTableroRequest.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .propietarioEmail(propietarioEmail)
                .build();
            
            servicioTablero.crearTablero(request);
            mostrarAlerta("Éxito", "Tablero creado correctamente");
            return true;
        } catch (Exception e) {
            mostrarError("Error", "No se pudo crear el tablero: " + e.getMessage());
            return false;
        }
    }

    /**
     * Comparte un tablero con otro usuario.
     * 
     * @param tableroid identificador del tablero
     * @param propietarioEmail email del propietario
     * @param emailUsuario email del usuario a agregar
     * @return true si se compartió correctamente
     */
    public boolean compartirTablero(String tableroid, String propietarioEmail, String emailUsuario) {
        try {
            servicioTablero.compartirTablero(tableroid, propietarioEmail, emailUsuario);
            mostrarAlerta("Éxito", "Tablero compartido correctamente");
            return true;
        } catch (Exception e) {
            mostrarError("Error", "No se pudo compartir el tablero: " + e.getMessage());
            return false;
        }
    }

    /**
     * Agrega una lista a un tablero.
     * 
     * @param tableroid identificador del tablero
     * @param nombreLista nombre de la nueva lista
     * @param emailUsuario email del usuario
     * @return true si se agregó correctamente
     */
    public boolean agregarLista(String tableroid, String nombreLista, String emailUsuario) {
        try {
            CrearListaRequest request = CrearListaRequest.builder()
                .nombre(nombreLista)
                .build();
            
            servicioTablero.agregarLista(tableroid, emailUsuario, request);
            mostrarAlerta("Éxito", "Lista agregada correctamente");
            return true;
        } catch (Exception e) {
            mostrarError("Error", "No se pudo agregar la lista: " + e.getMessage());
            return false;
        }
    }

    /**
     * Muestra una ventana con los detalles de un tablero.
     * 
     * @param tablero el tablero a mostrar
     */
    public void mostrarDetallesTablero(TableroResponse tablero) {
        Stage ventana = new Stage();
        ventana.setTitle("Detalles: " + tablero.getTitulo());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label lblTitulo = new Label("Título: " + tablero.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label lblDescripcion = new Label("Descripción: " + (tablero.getDescripcion() != null ? tablero.getDescripcion() : "N/A"));
        Label lblPropietario = new Label("Propietario: " + tablero.getPropietarioEmail());
        Label lblFechaCreacion = new Label("Creado: " + tablero.getFechaCreacion());
        Label lblEstado = new Label("Estado: " + (tablero.isBloqueado() ? "Bloqueado" : "Activo"));
        Label lblTotalTarjetas = new Label("Total de tarjetas: " + tablero.getTotalTarjetas());
        Label lblTarjetasCompletadas = new Label("Tarjetas completadas: " + tablero.getTarjetasCompletadas());

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(e -> ventana.close());

        vbox.getChildren().addAll(
            lblTitulo, lblDescripcion, lblPropietario,
            lblFechaCreacion, lblEstado, new Separator(),
            lblTotalTarjetas, lblTarjetasCompletadas, btnCerrar
        );

        Scene escena = new Scene(vbox, 500, 400);
        ventana.setScene(escena);
        ventana.show();
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
}
