package pds.app_gestion.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import pds.app_gestion.application.dto.EtiquetaResponse;
import pds.app_gestion.application.dto.TarjetaResponse;
import pds.app_gestion.application.service.ServicioTarjeta;
import java.util.List;
import java.util.Set;

/**
 * Panel para mostrar detalles de un tablero con filtrado de tarjetas.
 * 
 * Integra un panel de filtros de etiquetas con una lista de tarjetas.
 */
public class PanelDetallesTablero extends BorderPane {
    
    private final String idTablero;
    private final String idLista;
    private final String nombreLista;
    private final String emailUsuario;
    private final ServicioTarjeta servicioTarjeta;
    private final FiltroEtiquetasPanel filtroPanel;
    private final ListView<TarjetaResponse> listaTarjetas;

    /**
     * Crea un nuevo panel de detalles del tablero.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param nombreLista nombre de la lista
     * @param emailUsuario email del usuario
     * @param servicioTarjeta servicio para operaciones con tarjetas
     */
    public PanelDetallesTablero(String idTablero, String idLista, String nombreLista, 
                               String emailUsuario, ServicioTarjeta servicioTarjeta) {
        this.idTablero = idTablero;
        this.idLista = idLista;
        this.nombreLista = nombreLista;
        this.emailUsuario = emailUsuario;
        this.servicioTarjeta = servicioTarjeta;
        
        // Panel de filtros
        this.filtroPanel = new FiltroEtiquetasPanel();
        this.filtroPanel.setOnFiltroChanged(this::actualizarTarjetasFiltradas);
        
        // ListView para tarjetas
        this.listaTarjetas = new ListView<>();
        this.listaTarjetas.setCellFactory(lv -> new CeldaTarjeta());
        
        // Layout
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));
        
        // Título
        Text titulo = new Text("Lista: " + nombreLista);
        titulo.setFont(new Font(16));
        HBox headerBox = new HBox();
        headerBox.getChildren().add(titulo);
        centerBox.getChildren().add(headerBox);
        
        // Panel de filtros
        centerBox.getChildren().add(filtroPanel);
        
        // Lista de tarjetas
        VBox.setVgrow(listaTarjetas, javafx.scene.layout.Priority.ALWAYS);
        centerBox.getChildren().add(listaTarjetas);
        
        this.setCenter(centerBox);
        
        // Cargar datos iniciales
        cargarDatos();
    }

    /**
     * Carga los datos iniciales (etiquetas y tarjetas).
     */
    private void cargarDatos() {
        try {
            // Obtener etiquetas disponibles
            Set<EtiquetaResponse> etiquetas = servicioTarjeta.obtenerEtiquetasDeLista(
                idTablero, idLista, emailUsuario);
            filtroPanel.actualizarEtiquetas(etiquetas);
            
            // Cargar todas las tarjetas
            actualizarTarjetasFiltradas();
        } catch (Exception e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
    }

    /**
     * Actualiza la lista de tarjetas según los filtros seleccionados.
     */
    private void actualizarTarjetasFiltradas() {
        try {
            Set<String> etiquetasSeleccionadas = filtroPanel.obtenerEtiquetasSeleccionadas();
            
            List<TarjetaResponse> tarjetas;
            if (etiquetasSeleccionadas.isEmpty()) {
                tarjetas = servicioTarjeta.obtenerTodasLasTarjetas(idTablero, idLista, emailUsuario);
            } else {
                tarjetas = servicioTarjeta.obtenerTarjetasPorEtiquetas(
                    idTablero, idLista, emailUsuario, etiquetasSeleccionadas);
            }
            
            listaTarjetas.getItems().clear();
            listaTarjetas.getItems().addAll(tarjetas);
        } catch (Exception e) {
            mostrarError("Error al filtrar tarjetas: " + e.getMessage());
        }
    }

    /**
     * Celda personalizada para mostrar tarjetas.
     */
    private class CeldaTarjeta extends ListCell<TarjetaResponse> {
        @Override
        protected void updateItem(TarjetaResponse tarjeta, boolean empty) {
            super.updateItem(tarjeta, empty);
            
            if (empty || tarjeta == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox cellBox = new VBox(5);
                cellBox.setPadding(new Insets(8));
                cellBox.setStyle("-fx-border-color: #DDDDDD; -fx-border-width: 1; " +
                               "-fx-background-color: #FAFAFA;");
                
                // Título
                Label titulo = new Label(tarjeta.getTitulo());
                titulo.setFont(new Font(12));
                titulo.setStyle("-fx-font-weight: bold;");
                cellBox.getChildren().add(titulo);
                
                // Descripción
                if (tarjeta.getDescripcion() != null && !tarjeta.getDescripcion().isEmpty()) {
                    Label descripcion = new Label(tarjeta.getDescripcion());
                    descripcion.setWrapText(true);
                    cellBox.getChildren().add(descripcion);
                }
                
                // Etiquetas
                if (tarjeta.getEtiquetas() != null && !tarjeta.getEtiquetas().isEmpty()) {
                    HBox etiquetasBox = new HBox(5);
                    for (EtiquetaResponse etiqueta : tarjeta.getEtiquetas()) {
                        Label etiLabel = new Label(etiqueta.getNombre());
                        etiLabel.setStyle("-fx-background-color: " + etiqueta.getColor() + "; " +
                                        "-fx-text-fill: white; -fx-padding: 3px 6px; " +
                                        "-fx-border-radius: 3; -fx-background-radius: 3;");
                        etiLabel.setFont(new Font(10));
                        etiquetasBox.getChildren().add(etiLabel);
                    }
                    cellBox.getChildren().add(etiquetasBox);
                }
                
                // Estado
                String estado = tarjeta.isCompletada() ? "✓ Completada" : "○ Pendiente";
                Label estadoLabel = new Label(estado);
                estadoLabel.setFont(new Font(10));
                estadoLabel.setStyle("-fx-text-fill: " + (tarjeta.isCompletada() ? "#00AA00" : "#FF6600") + ";");
                cellBox.getChildren().add(estadoLabel);
                
                setGraphic(cellBox);
            }
        }
    }

    /**
     * Muestra un mensaje de error.
     * 
     * @param mensaje mensaje de error
     */
    private void mostrarError(String mensaje) {
        Label errorLabel = new Label(mensaje);
        errorLabel.setStyle("-fx-text-fill: red;");
        listaTarjetas.getItems().clear();
    }
}
