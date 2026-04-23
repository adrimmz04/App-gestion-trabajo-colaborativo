package pds.app_gestion.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import pds.app_gestion.application.dto.CrearTarjetaRequest;
import pds.app_gestion.application.dto.EtiquetaResponse;
import pds.app_gestion.application.dto.ListaResponse;
import pds.app_gestion.application.dto.TarjetaResponse;
import pds.app_gestion.application.dto.ActualizarTarjetaRequest;
import pds.app_gestion.application.dto.CrearEtiquetaRequest;
import pds.app_gestion.application.service.ServicioLista;
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
    private final ServicioLista servicioLista;
    private final List<ListaResponse> listasTablero;
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
      * @param servicioLista servicio para mover tarjetas entre listas
      * @param listasTablero listas disponibles en el tablero actual
     */
    public PanelDetallesTablero(String idTablero, String idLista, String nombreLista, 
                                         String emailUsuario, ServicioTarjeta servicioTarjeta,
                                         ServicioLista servicioLista, List<ListaResponse> listasTablero) {
        this.idTablero = idTablero;
        this.idLista = idLista;
        this.nombreLista = nombreLista;
        this.emailUsuario = emailUsuario;
        this.servicioTarjeta = servicioTarjeta;
          this.servicioLista = servicioLista;
          this.listasTablero = listasTablero;
        
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
        headerBox.setSpacing(10);

        Button btnAgregarTarjeta = new Button("Agregar tarjeta");
        btnAgregarTarjeta.setOnAction(event -> mostrarDialogoCrearTarjeta());

        Button btnCambiarEstado = new Button("Completar / descompletar");
        btnCambiarEstado.setOnAction(event -> alternarEstadoTarjetaSeleccionada());

        Button btnEditarTarjeta = new Button("Editar descripción");
        btnEditarTarjeta.setOnAction(event -> mostrarDialogoEditarTarjeta());

        Button btnAgregarEtiqueta = new Button("Agregar etiqueta");
        btnAgregarEtiqueta.setOnAction(event -> mostrarDialogoAgregarEtiqueta());

        Button btnEliminarTarjeta = new Button("Eliminar tarjeta");
        btnEliminarTarjeta.setOnAction(event -> eliminarTarjetaSeleccionada());

        Button btnMoverTarjeta = new Button("Mover tarjeta");
        btnMoverTarjeta.setOnAction(event -> mostrarDialogoMoverTarjeta());

        listaTarjetas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) ->
            actualizarTextoBotonEstado(btnCambiarEstado, actual)
        );

        headerBox.getChildren().addAll(
            titulo,
            btnAgregarTarjeta,
            btnEditarTarjeta,
            btnAgregarEtiqueta,
            btnCambiarEstado,
            btnMoverTarjeta,
            btnEliminarTarjeta
        );
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
     * Muestra un dialogo para crear una tarjeta en la lista actual.
     */
    private void mostrarDialogoCrearTarjeta() {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Crear tarjeta");
        dialogo.setHeaderText("Nueva tarjeta en la lista " + nombreLista);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Título");

        TextArea txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción");
        txtDescripcion.setPrefRowCount(3);

        ComboBox<String> comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll("TAREA", "CHECKLIST");
        comboTipo.setValue("TAREA");

        grid.add(new Label("Título:"), 0, 0);
        grid.add(txtTitulo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDescripcion, 1, 1);
        grid.add(new Label("Tipo:"), 0, 2);
        grid.add(comboTipo, 1, 2);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado != ButtonType.OK) {
                return;
            }

            String tituloTarjeta = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
            String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";

            if (tituloTarjeta.isEmpty()) {
                mostrarError("El título de la tarjeta no puede estar vacío");
                return;
            }

            try {
                servicioTarjeta.crearTarjeta(
                    idTablero,
                    idLista,
                    emailUsuario,
                    CrearTarjetaRequest.builder()
                        .titulo(tituloTarjeta)
                        .descripcion(descripcion)
                        .tipo(comboTipo.getValue())
                        .build()
                );
                cargarDatos();
            } catch (Exception e) {
                mostrarError("Error al crear la tarjeta: " + e.getMessage());
            }
        });
    }

    private void alternarEstadoTarjetaSeleccionada() {
        TarjetaResponse tarjeta = listaTarjetas.getSelectionModel().getSelectedItem();
        if (tarjeta == null) {
            mostrarError("Selecciona una tarjeta para cambiar su estado");
            return;
        }

        try {
            if (tarjeta.isCompletada()) {
                servicioTarjeta.marcarComoNoCompletada(idTablero, idLista, tarjeta.getId(), emailUsuario);
            } else {
                servicioTarjeta.marcarComoCompletada(idTablero, idLista, tarjeta.getId(), emailUsuario);
            }
            cargarDatos();
        } catch (Exception e) {
            mostrarError("Error al actualizar el estado de la tarjeta: " + e.getMessage());
        }
    }

    private void mostrarDialogoEditarTarjeta() {
        TarjetaResponse tarjeta = listaTarjetas.getSelectionModel().getSelectedItem();
        if (tarjeta == null) {
            mostrarError("Selecciona una tarjeta para editarla");
            return;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Editar tarjeta");
        dialogo.setHeaderText("Actualizar descripción de '" + tarjeta.getTitulo() + "'");

        TextArea txtDescripcion = new TextArea(tarjeta.getDescripcion());
        txtDescripcion.setPromptText("Descripción");
        txtDescripcion.setPrefRowCount(4);

        VBox contenido = new VBox(10, new Label("Descripción:"), txtDescripcion);
        contenido.setPadding(new Insets(10));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado != ButtonType.OK) {
                return;
            }

            try {
                servicioTarjeta.actualizarTarjeta(
                    idTablero,
                    idLista,
                    tarjeta.getId(),
                    emailUsuario,
                    ActualizarTarjetaRequest.builder()
                        .descripcion(txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "")
                        .build()
                );
                cargarDatos();
            } catch (Exception e) {
                mostrarError("Error al editar la tarjeta: " + e.getMessage());
            }
        });
    }

    private void mostrarDialogoAgregarEtiqueta() {
        TarjetaResponse tarjeta = listaTarjetas.getSelectionModel().getSelectedItem();
        if (tarjeta == null) {
            mostrarError("Selecciona una tarjeta para etiquetarla");
            return;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Agregar etiqueta");
        dialogo.setHeaderText("Nueva etiqueta para '" + tarjeta.getTitulo() + "'");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");

        ColorPicker selectorColor = new ColorPicker(Color.web("#4CAF50"));
        Label lblColorHex = new Label(convertirColorAHex(selectorColor.getValue()));
        selectorColor.valueProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                lblColorHex.setText(convertirColorAHex(actual));
            }
        });

        HBox colorBox = new HBox(10, selectorColor, lblColorHex);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorBox, 1, 1);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado != ButtonType.OK) {
                return;
            }

            String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
            String color = convertirColorAHex(selectorColor.getValue());

            if (nombre.isEmpty() || color.isEmpty()) {
                mostrarError("El nombre y el color de la etiqueta son obligatorios");
                return;
            }

            try {
                servicioTarjeta.agregarEtiqueta(
                    idTablero,
                    idLista,
                    tarjeta.getId(),
                    emailUsuario,
                    new CrearEtiquetaRequest(nombre, color)
                );
                cargarDatos();
            } catch (Exception e) {
                mostrarError("Error al agregar la etiqueta: " + e.getMessage());
            }
        });
    }

    private String convertirColorAHex(Color color) {
        if (color == null) {
            return "#808080";
        }

        return String.format("#%02X%02X%02X",
            (int) Math.round(color.getRed() * 255),
            (int) Math.round(color.getGreen() * 255),
            (int) Math.round(color.getBlue() * 255));
    }

    private void eliminarTarjetaSeleccionada() {
        TarjetaResponse tarjeta = listaTarjetas.getSelectionModel().getSelectedItem();
        if (tarjeta == null) {
            mostrarError("Selecciona una tarjeta para eliminarla");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar tarjeta");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("Se eliminará la tarjeta '" + tarjeta.getTitulo() + "'.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            servicioTarjeta.eliminarTarjeta(idTablero, idLista, tarjeta.getId(), emailUsuario);
            cargarDatos();
        } catch (Exception e) {
            mostrarError("Error al eliminar la tarjeta: " + e.getMessage());
        }
    }

    private void mostrarDialogoMoverTarjeta() {
        TarjetaResponse tarjeta = listaTarjetas.getSelectionModel().getSelectedItem();
        if (tarjeta == null) {
            mostrarError("Selecciona una tarjeta para moverla");
            return;
        }

        List<ListaResponse> destinos = listasTablero.stream()
            .filter(lista -> !lista.getId().equals(idLista))
            .toList();

        if (destinos.isEmpty()) {
            mostrarError("No hay otra lista disponible en el tablero");
            return;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Mover tarjeta");
        dialogo.setHeaderText("Mover '" + tarjeta.getTitulo() + "' a otra lista");

        ComboBox<ListaResponse> comboDestino = new ComboBox<>();
        comboDestino.getItems().addAll(destinos);
        comboDestino.setPrefWidth(260);
        comboDestino.setCellFactory(listView -> crearCeldaListaDestino());
        comboDestino.setButtonCell(crearCeldaListaDestino());
        comboDestino.getSelectionModel().selectFirst();

        VBox contenido = new VBox(10, new Label("Lista destino:"), comboDestino);
        contenido.setPadding(new Insets(10));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado != ButtonType.OK) {
                return;
            }

            ListaResponse destino = comboDestino.getSelectionModel().getSelectedItem();
            if (destino == null) {
                mostrarError("Selecciona una lista de destino");
                return;
            }

            try {
                servicioLista.moverTarjeta(idTablero, idLista, destino.getId(), tarjeta.getId(), emailUsuario);
                cargarDatos();
            } catch (Exception e) {
                mostrarError("Error al mover la tarjeta: " + e.getMessage());
            }
        });
    }

    private ListCell<ListaResponse> crearCeldaListaDestino() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ListaResponse lista, boolean empty) {
                super.updateItem(lista, empty);
                if (empty || lista == null) {
                    setText(null);
                    return;
                }
                setText(lista.getNombre());
            }
        };
    }

    private void actualizarTextoBotonEstado(Button boton, TarjetaResponse tarjeta) {
        if (tarjeta == null) {
            boton.setText("Completar / descompletar");
            return;
        }

        boton.setText(tarjeta.isCompletada() ? "Marcar pendiente" : "Marcar completada");
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

                Label tipo = new Label("Tipo: " + (tarjeta.getTipo() == null ? "Sin tipo" : tarjeta.getTipo()));
                tipo.setFont(new Font(10));
                tipo.setStyle("-fx-text-fill: #555555;");
                cellBox.getChildren().add(tipo);
                
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
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
