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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import pds.app_gestion.application.dto.CrearTarjetaRequest;
import pds.app_gestion.application.dto.EtiquetaResponse;
import pds.app_gestion.application.dto.ListaResponse;
import pds.app_gestion.application.dto.PermisosTarjetaResponse;
import pds.app_gestion.application.dto.TarjetaResponse;
import pds.app_gestion.application.dto.ActualizarTarjetaRequest;
import pds.app_gestion.application.dto.ConfigurarPermisoTarjetaRequest;
import pds.app_gestion.application.dto.CrearEtiquetaRequest;
import pds.app_gestion.application.service.ServicioLista;
import pds.app_gestion.application.service.ServicioTarjeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final boolean puedeGestionarPermisos;
    private final List<String> usuariosCompartidos;
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
                                                                                 ServicioLista servicioLista, List<ListaResponse> listasTablero,
                                                                                 boolean puedeGestionarPermisos, Set<String> usuariosCompartidos) {
        this.idTablero = idTablero;
        this.idLista = idLista;
        this.nombreLista = nombreLista;
        this.emailUsuario = emailUsuario;
                this.puedeGestionarPermisos = puedeGestionarPermisos;
                this.usuariosCompartidos = usuariosCompartidos == null
                        ? List.of()
                        : usuariosCompartidos.stream().sorted().toList();
        this.servicioTarjeta = servicioTarjeta;
          this.servicioLista = servicioLista;
          this.listasTablero = listasTablero;
        
        // Panel de filtros
        this.filtroPanel = new FiltroEtiquetasPanel();
        this.filtroPanel.setOnFiltroChanged(this::actualizarTarjetasFiltradas);
        this.getStyleClass().add("detail-shell");
        
        // ListView para tarjetas
        this.listaTarjetas = new ListView<>();
        this.listaTarjetas.getStyleClass().add("task-list");
        this.listaTarjetas.setPlaceholder(new Label("No hay tarjetas en esta lista todavía"));
        this.listaTarjetas.setCellFactory(lv -> new CeldaTarjeta());
        
        // Layout
        VBox centerBox = new VBox(14);
        centerBox.setPadding(new Insets(10));
        centerBox.getStyleClass().add("workspace-section");
        
        // Título
        Label titulo = new Label(nombreLista);
        titulo.getStyleClass().add("detail-title");

        Label subtitulo = new Label("Gestiona tarjetas, etiquetas y movimientos para la lista seleccionada.");
        subtitulo.getStyleClass().add("detail-subtitle");
        subtitulo.setWrapText(true);

        Button btnAgregarTarjeta = new Button("Agregar tarjeta");
        btnAgregarTarjeta.getStyleClass().add("primary-button");
        btnAgregarTarjeta.setOnAction(event -> mostrarDialogoCrearTarjeta());

        Button btnCambiarEstado = new Button("Completar / descompletar");
        btnCambiarEstado.getStyleClass().add("secondary-button");
        btnCambiarEstado.setOnAction(event -> alternarEstadoTarjetaSeleccionada());

        Button btnEditarTarjeta = new Button("Editar descripción");
        btnEditarTarjeta.getStyleClass().add("secondary-button");
        btnEditarTarjeta.setOnAction(event -> mostrarDialogoEditarTarjeta());

        Button btnAgregarEtiqueta = new Button("Agregar etiqueta");
        btnAgregarEtiqueta.getStyleClass().add("secondary-button");
        btnAgregarEtiqueta.setOnAction(event -> mostrarDialogoAgregarEtiqueta());

        Button btnEliminarTarjeta = new Button("Eliminar tarjeta");
        btnEliminarTarjeta.getStyleClass().add("danger-button");
        btnEliminarTarjeta.setOnAction(event -> eliminarTarjetaSeleccionada());

        Button btnMoverTarjeta = new Button("Mover tarjeta");
        btnMoverTarjeta.getStyleClass().add("secondary-button");
        btnMoverTarjeta.setOnAction(event -> mostrarDialogoMoverTarjeta());

        Button btnGestionarPermisos = new Button("Permisos de tarjeta");
        btnGestionarPermisos.getStyleClass().add("secondary-button");
        btnGestionarPermisos.setOnAction(event -> mostrarDialogoPermisosTarjeta());

        listaTarjetas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) ->
            actualizarTextoBotonEstado(btnCambiarEstado, actual)
        );

        FlowPane acciones = new FlowPane(10, 10,
            titulo,
            btnAgregarTarjeta,
            btnEditarTarjeta,
            btnAgregarEtiqueta,
            btnCambiarEstado,
            btnMoverTarjeta,
            btnEliminarTarjeta
        );
        if (puedeGestionarPermisos) {
            acciones.getChildren().add(btnGestionarPermisos);
        }
        acciones.getStyleClass().add("detail-actions");

        VBox headerBox = new VBox(6, titulo, subtitulo, acciones);
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

    private void mostrarDialogoPermisosTarjeta() {
        TarjetaResponse tarjeta = listaTarjetas.getSelectionModel().getSelectedItem();
        if (tarjeta == null) {
            mostrarError("Selecciona una tarjeta para gestionar sus permisos");
            return;
        }

        if (usuariosCompartidos.isEmpty()) {
            mostrarError("Este tablero no tiene usuarios compartidos a los que asignar permisos");
            return;
        }

        try {
            PermisosTarjetaResponse permisosActuales = servicioTarjeta.obtenerPermisosTarjeta(
                idTablero,
                idLista,
                tarjeta.getId(),
                emailUsuario
            );

            Dialog<ButtonType> dialogo = new Dialog<>();
            dialogo.setTitle("Permisos de tarjeta");
            dialogo.setHeaderText("Gestionar permisos de '" + tarjeta.getTitulo() + "'");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10));

            ComboBox<String> comboUsuario = new ComboBox<>();
            comboUsuario.getItems().addAll(usuariosCompartidos);
            comboUsuario.setPrefWidth(280);

            ComboBox<String> comboPermiso = new ComboBox<>();
            comboPermiso.getItems().addAll("LECTURA", "ESCRITURA", "NINGUNO");

            Label lblPermisoActual = new Label();
            comboUsuario.valueProperty().addListener((obs, anterior, actual) -> {
                String permisoActual = resolverPermisoActual(permisosActuales.getPermisosUsuarios(), actual);
                lblPermisoActual.setText("Permiso actual: " + permisoActual);
                comboPermiso.setValue(permisoActual);
            });

            comboUsuario.getSelectionModel().selectFirst();

            grid.add(new Label("Usuario compartido:"), 0, 0);
            grid.add(comboUsuario, 1, 0);
            grid.add(new Label("Permiso:"), 0, 1);
            grid.add(comboPermiso, 1, 1);
            grid.add(lblPermisoActual, 1, 2);

            dialogo.getDialogPane().setContent(grid);
            dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialogo.showAndWait().ifPresent(resultado -> {
                if (resultado != ButtonType.OK) {
                    return;
                }

                String usuarioSeleccionado = comboUsuario.getValue();
                String permisoSeleccionado = comboPermiso.getValue();

                if (usuarioSeleccionado == null || usuarioSeleccionado.isBlank()) {
                    mostrarError("Selecciona un usuario compartido");
                    return;
                }

                if (permisoSeleccionado == null || permisoSeleccionado.isBlank()) {
                    mostrarError("Selecciona un permiso para continuar");
                    return;
                }

                try {
                    servicioTarjeta.configurarPermisoTarjeta(
                        idTablero,
                        idLista,
                        tarjeta.getId(),
                        emailUsuario,
                        ConfigurarPermisoTarjetaRequest.builder()
                            .emailUsuario(usuarioSeleccionado)
                            .permiso(permisoSeleccionado)
                            .build()
                    );
                    cargarDatos();
                } catch (Exception e) {
                    mostrarError("Error al actualizar los permisos de la tarjeta: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            mostrarError("Error al cargar los permisos de la tarjeta: " + e.getMessage());
        }
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

    private String resolverPermisoActual(Map<String, String> permisos, String emailUsuarioSeleccionado) {
        if (permisos == null || emailUsuarioSeleccionado == null) {
            return "NINGUNO";
        }

        return permisos.getOrDefault(emailUsuarioSeleccionado, "NINGUNO");
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
                cellBox.getStyleClass().add("task-card");
                
                // Título
                Label titulo = new Label(tarjeta.getTitulo());
                titulo.getStyleClass().add("task-card-title");
                cellBox.getChildren().add(titulo);

                Label tipo = new Label("Tipo: " + (tarjeta.getTipo() == null ? "Sin tipo" : tarjeta.getTipo()));
                tipo.getStyleClass().add("task-card-meta");
                cellBox.getChildren().add(tipo);
                
                // Descripción
                if (tarjeta.getDescripcion() != null && !tarjeta.getDescripcion().isEmpty()) {
                    Label descripcion = new Label(tarjeta.getDescripcion());
                    descripcion.setWrapText(true);
                    descripcion.getStyleClass().add("task-card-body");
                    cellBox.getChildren().add(descripcion);
                }
                
                // Etiquetas
                if (tarjeta.getEtiquetas() != null && !tarjeta.getEtiquetas().isEmpty()) {
                    HBox etiquetasBox = new HBox(5);
                    for (EtiquetaResponse etiqueta : tarjeta.getEtiquetas()) {
                        Label etiLabel = new Label(etiqueta.getNombre());
                        etiLabel.getStyleClass().add("task-tag");
                        etiLabel.setStyle("-fx-background-color: " + etiqueta.getColor() + "; " +
                            "-fx-text-fill: " + resolverColorTextoEtiqueta(etiqueta.getColor()) + ";");
                        etiquetasBox.getChildren().add(etiLabel);
                    }
                    cellBox.getChildren().add(etiquetasBox);
                }
                
                // Estado
                String estado = tarjeta.isCompletada() ? "✓ Completada" : "○ Pendiente";
                Label estadoLabel = new Label(estado);
                estadoLabel.getStyleClass().add(tarjeta.isCompletada() ? "task-card-state-done" : "task-card-state-pending");
                cellBox.getChildren().add(estadoLabel);
                
                setGraphic(cellBox);
            }
        }
    }

    private String resolverColorTextoEtiqueta(String colorHex) {
        try {
            Color color = Color.web(colorHex);
            double luminancia = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
            return luminancia > 0.62 ? "#1F2937" : "#FFFFFF";
        } catch (Exception e) {
            return "#FFFFFF";
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
