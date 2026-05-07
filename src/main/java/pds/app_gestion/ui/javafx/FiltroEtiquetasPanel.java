package pds.app_gestion.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
        this.getStyleClass().add("filter-panel");
        
        this.etiquetasSeleccionadas = new HashSet<>();
        
        // Título
        Label titulo = new Label("Filtrar por etiquetas");
        titulo.getStyleClass().add("filter-title");
        this.getChildren().add(titulo);
        
        // ScrollPane para el contenido
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(120);
        scrollPane.getStyleClass().add("filter-scroll");
        
        // FlowPane para mostrar los checkboxes
        flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.setPadding(new Insets(5));
        flowPane.getStyleClass().add("filter-flow");
        
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
        checkbox.setStyle("-fx-text-fill: " + obtenerColorTexto(colorFondo) + "; -fx-padding: 6px 10px; " +
                         "-fx-background-color: " + colorFondo + "; " +
                         "-fx-border-radius: 999; -fx-background-radius: 999;");
        
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

    private String obtenerColorTexto(String colorHex) {
        try {
            Color color = Color.web(colorHex);
            double luminancia = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
            return luminancia > 0.62 ? "#1F2937" : "#FFFFFF";
        } catch (Exception e) {
            return "#FFFFFF";
        }
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
