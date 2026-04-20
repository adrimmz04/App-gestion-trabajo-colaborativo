package pds.app_gestion.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import pds.app_gestion.application.dto.EtiquetaResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * Panel de filtrado de tarjetas por etiquetas.
 * 
 * Proporciona una interfaz gráfica para seleccionar etiquetas y filtrar
 * las tarjetas que se muestran en la lista.
 */
public class FiltroEtiquetasPanel extends VBox {
    
    private final FlowPane flowPane;
    private final Set<String> etiquetasSeleccionadas;
    private Runnable onFiltroChanged;

    /**
     * Crea un nuevo panel de filtrado de etiquetas.
     */
    public FiltroEtiquetasPanel() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1;");
        
        this.etiquetasSeleccionadas = new HashSet<>();
        
        // Título
        Text titulo = new Text("Filtrar por etiquetas:");
        titulo.setFont(new Font(14));
        this.getChildren().add(titulo);
        
        // ScrollPane para el contenido
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(120);
        
        // FlowPane para mostrar los checkboxes
        flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.setPadding(new Insets(5));
        
        scrollPane.setContent(flowPane);
        this.getChildren().add(scrollPane);
    }

    /**
     * Actualiza las etiquetas disponibles para filtrado.
     * 
     * @param etiquetas conjunto de etiquetas a mostrar
     */
    public void actualizarEtiquetas(Set<EtiquetaResponse> etiquetas) {
        flowPane.getChildren().clear();
        
        for (EtiquetaResponse etiqueta : etiquetas) {
            CheckBox checkbox = crearCheckboxEtiqueta(etiqueta);
            flowPane.getChildren().add(checkbox);
        }
    }

    /**
     * Crea un checkbox para una etiqueta.
     * 
     * @param etiqueta etiqueta para la cual crear el checkbox
     * @return checkbox configurado con la etiqueta
     */
    private CheckBox crearCheckboxEtiqueta(EtiquetaResponse etiqueta) {
        CheckBox checkbox = new CheckBox(etiqueta.getNombre());
        
        // Estilo del checkbox con el color de la etiqueta
        String colorFondo = etiqueta.getColor();
        checkbox.setStyle("-fx-text-fill: white; -fx-padding: 5px; " +
                         "-fx-background-color: " + colorFondo + "; " +
                         "-fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Event listener para cuando se selecciona/deselecciona
        checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                etiquetasSeleccionadas.add(etiqueta.getNombre());
            } else {
                etiquetasSeleccionadas.remove(etiqueta.getNombre());
            }
            
            if (onFiltroChanged != null) {
                onFiltroChanged.run();
            }
        });
        
        return checkbox;
    }

    /**
     * Obtiene el conjunto de etiquetas seleccionadas para filtrado.
     * 
     * @return conjunto de nombres de etiquetas seleccionadas
     */
    public Set<String> obtenerEtiquetasSeleccionadas() {
        return new HashSet<>(etiquetasSeleccionadas);
    }

    /**
     * Limpia todos los filtros seleccionados.
     */
    public void limpiarFiltros() {
        etiquetasSeleccionadas.clear();
        flowPane.getChildren().forEach(node -> {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);
            }
        });
    }

    /**
     * Establece un callback que se ejecuta cuando cambia el filtro.
     * 
     * @param callback función a ejecutar cuando cambian los filtros
     */
    public void setOnFiltroChanged(Runnable callback) {
        this.onFiltroChanged = callback;
    }
}
