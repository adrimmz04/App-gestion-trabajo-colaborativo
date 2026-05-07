package pds.app_gestion.ui.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pds.app_gestion.application.dto.ActualizarTableroRequest;
import pds.app_gestion.application.dto.BloquearTableroRequest;
import pds.app_gestion.application.dto.CrearTableroRequest;
import pds.app_gestion.application.dto.CrearListaRequest;
import pds.app_gestion.application.dto.ListaResponse;
import pds.app_gestion.application.dto.RegistroAccionResponse;
import pds.app_gestion.application.dto.SesionAutenticadaResponse;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoRequest;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoResponse;
import pds.app_gestion.application.dto.TableroResponse;
import pds.app_gestion.application.service.ServicioAutenticacion;
import pds.app_gestion.application.service.ServicioLista;
import pds.app_gestion.application.service.ServicioTablero;
import pds.app_gestion.application.service.ServicioTarjeta;
import pds.app_gestion.ui.dialog.DialogoExportarPlantilla;
import pds.app_gestion.ui.dialog.DialogoImportarPlantilla;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ventana principal de la aplicación JavaFX.
 * Proporciona una interfaz de usuario para gestionar tableros y tarjetas.
 */
public class VentanaPrincipal extends Application {

    private ConfigurableApplicationContext applicationContext;
    private ServicioAutenticacion servicioAutenticacion;
    private ServicioLista servicioLista;
    private ServicioTablero servicioTablero;
    private ServicioTarjeta servicioTarjeta;
    private ListView<TableroResponse> listaTableros;
    private ListView<TableroResponse> listaCompartidos;
    private String emailUsuarioActivo;
    private String codigoAccesoActivo;
    private Label labelUsuarioActivo;
    private Label labelResumenCarga;

    private static VentanaPrincipal instancia;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(pds.app_gestion.Application.class)
            .headless(false)
            .run(getParameters().getRaw().toArray(new String[0]));
        servicioAutenticacion = applicationContext.getBean(ServicioAutenticacion.class);
        servicioLista = applicationContext.getBean(ServicioLista.class);
        servicioTablero = applicationContext.getBean(ServicioTablero.class);
        servicioTarjeta = applicationContext.getBean(ServicioTarjeta.class);
    }

    @Override
    public void start(Stage ventanaPrincipal) {
        try {
            instancia = this;
            
            // Crear interfaz principal
            BorderPane raiz = new BorderPane();
            raiz.setPadding(new Insets(14));
            raiz.getStyleClass().add("app-shell");

            // Barra de menú
            MenuBar menuBar = crearMenuBar();
            VBox cabecera = new VBox(menuBar, crearEncabezadoAplicacion());
            cabecera.getStyleClass().add("app-top");
            raiz.setTop(cabecera);

            // Panel principal
            TabPane tabPane = crearPanelPrincipal();
            raiz.setCenter(tabPane);

            // Escena
            Scene escena = new Scene(raiz, 1280, 820);
            aplicarEstilos(escena);
            ventanaPrincipal.setTitle("App Gestión de Trabajo Colaborativo");
            ventanaPrincipal.setScene(escena);
            ventanaPrincipal.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    private MenuBar crearMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menú Archivo
        Menu menuArchivo = new Menu("Archivo");
        MenuItem salir = new MenuItem("Salir");
        salir.setOnAction(e -> System.exit(0));
        menuArchivo.getItems().add(salir);

        Menu menuSesion = new Menu("Sesion");
        MenuItem iniciarSesion = new MenuItem("Iniciar sesion");
        iniciarSesion.setOnAction(e -> mostrarDialogoCargarTableros());
        MenuItem cerrarSesion = new MenuItem("Cerrar sesion");
        cerrarSesion.setOnAction(e -> cerrarSesionActual(true));
        menuSesion.getItems().addAll(iniciarSesion, cerrarSesion);

        // Menú Tablero
        Menu menuTablero = new Menu("Tablero");
        MenuItem crearTablero = new MenuItem("Crear nuevo tablero");
        crearTablero.setOnAction(e -> mostrarDialogoCrearTablero());
        MenuItem abrirTablero = new MenuItem("Abrir por ID o URL");
        abrirTablero.setOnAction(e -> mostrarDialogoAbrirTableroPorId());
        MenuItem importarPlantilla = new MenuItem("Importar plantilla YAML");
        importarPlantilla.setOnAction(e -> mostrarDialogoImportarPlantilla());
        menuTablero.getItems().addAll(crearTablero, abrirTablero, importarPlantilla);

        // Menú Ayuda
        Menu menuAyuda = new Menu("Ayuda");
        MenuItem acercaDe = new MenuItem("Acerca de");
        acercaDe.setOnAction(e -> mostrarAcercaDe());
        menuAyuda.getItems().add(acercaDe);

        menuBar.getMenus().addAll(menuArchivo, menuSesion, menuTablero, menuAyuda);
        return menuBar;
    }

    private VBox crearEncabezadoAplicacion() {
        VBox cabecera = new VBox(8);
        cabecera.getStyleClass().add("app-hero");

        Label subtitulo = new Label("ESCRITORIO COLABORATIVO");
        subtitulo.getStyleClass().add("hero-eyebrow");

        Label titulo = new Label("Gestiona tableros, listas y tarjetas desde una vista clara y operativa");
        titulo.getStyleClass().add("hero-title");
        titulo.setWrapText(true);

        Label descripcion = new Label("Inicia sesion con un codigo temporal, consulta tableros propios y compartidos, abre enlaces privados y trabaja desde una unica pantalla.");
        descripcion.getStyleClass().add("hero-subtitle");
        descripcion.setWrapText(true);

        labelUsuarioActivo = new Label();
        labelUsuarioActivo.getStyleClass().addAll("status-chip", "status-chip-muted");

        labelResumenCarga = new Label();
        labelResumenCarga.getStyleClass().addAll("status-chip", "status-chip-light");

        HBox meta = new HBox(10, labelUsuarioActivo, labelResumenCarga);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getStyleClass().add("hero-meta");

        cabecera.getChildren().addAll(subtitulo, titulo, descripcion, meta);
        actualizarResumenUsuario();
        return cabecera;
    }

    private void aplicarEstilos(Scene escena) {
        var recursoCss = VentanaPrincipal.class.getResource("/ui/javafx/app.css");
        if (recursoCss != null) {
            escena.getStylesheets().add(recursoCss.toExternalForm());
        }
    }

    private TabPane crearPanelPrincipal() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("dashboard-tabs");

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
        vbox.getStyleClass().add("workspace-section");

        Label titulo = new Label("Mis Tableros");
        titulo.getStyleClass().add("section-title");

        Label descripcion = new Label("Acceso directo a tableros propios, importación de plantillas y apertura por ID privado.");
        descripcion.getStyleClass().add("section-subtitle");
        descripcion.setWrapText(true);

        listaTableros = new ListView<>();
        listaTableros.setPrefHeight(400);
        listaTableros.getStyleClass().add("board-list");
        listaTableros.setCellFactory(listView -> crearCeldaTablero(false));
        listaTableros.setPlaceholder(new Label("Inicia sesion para ver los tableros disponibles"));
        listaTableros.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                TableroResponse tableroSeleccionado = listaTableros.getSelectionModel().getSelectedItem();
                if (tableroSeleccionado != null) {
                    mostrarDetallesTablero(tableroSeleccionado);
                }
            }
        });

        Button btnCrear = new Button("+ Crear Tablero");
        btnCrear.getStyleClass().add("primary-button");
        btnCrear.setOnAction(e -> mostrarDialogoCrearTablero());

        Button btnCargar = new Button("Iniciar sesion");
        btnCargar.getStyleClass().add("secondary-button");
        btnCargar.setOnAction(e -> mostrarDialogoCargarTableros());

        Button btnAbrirPorId = new Button("Abrir por ID/URL");
        btnAbrirPorId.getStyleClass().add("secondary-button");
        btnAbrirPorId.setOnAction(e -> mostrarDialogoAbrirTableroPorId());

        Button btnImportarPlantilla = new Button("Importar plantilla");
        btnImportarPlantilla.getStyleClass().add("secondary-button");
        btnImportarPlantilla.setOnAction(e -> mostrarDialogoImportarPlantilla());

        Button btnVerDetalle = new Button("Ver detalle");
        btnVerDetalle.getStyleClass().add("secondary-button");
        btnVerDetalle.setOnAction(e -> {
            TableroResponse tableroSeleccionado = listaTableros.getSelectionModel().getSelectedItem();
            if (tableroSeleccionado != null) {
                mostrarDetallesTablero(tableroSeleccionado);
            }
        });

        Button btnEliminar = new Button("Eliminar tablero");
        btnEliminar.getStyleClass().add("danger-button");
        btnEliminar.setOnAction(e -> {
            TableroResponse tableroSeleccionado = listaTableros.getSelectionModel().getSelectedItem();
            if (tableroSeleccionado == null) {
                mostrarError("Error", "Selecciona un tablero para eliminarlo");
                return;
            }

            eliminarTablero(tableroSeleccionado, null);
        });

        FlowPane acciones = new FlowPane(10, 10, btnCrear, btnCargar, btnAbrirPorId, btnImportarPlantilla, btnVerDetalle, btnEliminar);
        acciones.getStyleClass().add("action-flow");

        vbox.getChildren().addAll(titulo, descripcion, listaTableros, acciones);
        return vbox;
    }

    private VBox crearVistaTablerosCompartidos() {
        VBox vbox = new VBox(10);
        vbox.getStyleClass().add("workspace-section");

        Label titulo = new Label("Tableros Compartidos");
        titulo.getStyleClass().add("section-title");

        Label descripcion = new Label("Vista rápida de tableros accesibles por compartición con el usuario activo.");
        descripcion.getStyleClass().add("section-subtitle");
        descripcion.setWrapText(true);

        listaCompartidos = new ListView<>();
        listaCompartidos.setPrefHeight(400);
        listaCompartidos.getStyleClass().add("board-list");
        listaCompartidos.setCellFactory(listView -> crearCeldaTablero(true));
        listaCompartidos.setPlaceholder(new Label("Inicia sesion para ver tableros compartidos"));
        listaCompartidos.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                TableroResponse tableroSeleccionado = listaCompartidos.getSelectionModel().getSelectedItem();
                if (tableroSeleccionado != null) {
                    mostrarDetallesTablero(tableroSeleccionado);
                }
            }
        });

        vbox.getChildren().addAll(titulo, descripcion, listaCompartidos);
        return vbox;
    }

    private ListCell<TableroResponse> crearCeldaTablero(boolean compartido) {
        return new ListCell<>() {
            @Override
            protected void updateItem(TableroResponse tablero, boolean empty) {
                super.updateItem(tablero, empty);

                if (empty || tablero == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                VBox tarjeta = new VBox(6);
                tarjeta.getStyleClass().add("board-card");
                if (isSelected()) {
                    tarjeta.getStyleClass().add("board-card-selected");
                }

                Label titulo = new Label(tablero.getTitulo());
                titulo.getStyleClass().add("board-card-title");

                String descripcionTablero = tablero.getDescripcion() == null || tablero.getDescripcion().isBlank()
                    ? "Sin descripción disponible."
                    : tablero.getDescripcion().trim();
                Label descripcion = new Label(resumirTexto(descripcionTablero, 130));
                descripcion.getStyleClass().add("board-card-description");
                descripcion.setWrapText(true);

                Label meta = new Label(compartido
                    ? "Compartido por " + tablero.getPropietarioEmail()
                    : "Propietario: " + tablero.getPropietarioEmail());
                meta.getStyleClass().add("board-card-meta");

                Label estado = new Label(tablero.isBloqueado() ? "Bloqueado" : "Activo");
                estado.getStyleClass().addAll("status-chip", tablero.isBloqueado() ? "status-chip-blocked" : "status-chip-open");

                Label identificador = new Label("ID " + resumirId(tablero.getId()));
                identificador.getStyleClass().add("board-id");

                HBox pie = new HBox(8, estado, identificador);
                pie.setAlignment(Pos.CENTER_LEFT);
                pie.getStyleClass().add("board-card-footer");

                tarjeta.getChildren().addAll(titulo, descripcion, meta, pie);

                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(tarjeta);
            }
        };
    }

    private void mostrarDialogoCargarTableros() {
        iniciarSesionInteractiva(true);
    }

    private void mostrarDialogoCrearTablero() {
        String email = resolverEmailUsuarioActivo();
        if (email == null) {
            return;
        }

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

        grid.add(new Label("Título:"), 0, 0);
        grid.add(txtTitulo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDescripcion, 1, 1);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
                    String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";

                    if (titulo.isEmpty()) {
                        throw new IllegalArgumentException("El título es obligatorio");
                    }

                    servicioTablero.crearTablero(CrearTableroRequest.builder()
                        .titulo(titulo)
                        .descripcion(descripcion)
                        .propietarioEmail(email)
                        .build());

                    cargarTablerosUsuario(email);
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

    private void mostrarDialogoAbrirTableroPorId() {
        String email = resolverEmailUsuarioActivo();
        if (email == null) {
            return;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Abrir tablero");
        dialogo.setHeaderText("Abrir tablero por ID o URL privada");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField txtId = new TextField();
        txtId.setPromptText("ID o URL del tablero");

        grid.add(new Label("ID o URL:"), 0, 0);
        grid.add(txtId, 1, 0);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado != ButtonType.OK) {
                return;
            }

            String valorAcceso = txtId.getText() != null ? txtId.getText().trim() : "";
            if (valorAcceso.isEmpty()) {
                mostrarError("Error", "Debes indicar el ID o URL del tablero");
                return;
            }

            try {
                String idTablero = extraerIdTablero(valorAcceso);
                TableroResponse tablero = servicioTablero.obtenerTablero(idTablero, email);
                cargarTablerosUsuario(email);
                mostrarDetallesTablero(tablero);
            } catch (Exception e) {
                mostrarError("Error", "No se pudo abrir el tablero: " + e.getMessage());
            }
        });
    }

    private void mostrarDialogoImportarPlantilla() {
        String emailUsuario = resolverEmailUsuarioActivo();
        if (emailUsuario == null) {
            return;
        }

        DialogoImportarPlantilla dialogo = new DialogoImportarPlantilla(emailUsuario);
        dialogo.showAndWait();

        if (dialogo.getTableroCreado() == null) {
            return;
        }

        try {
            TableroResponse tableroCreado = servicioTablero.importarTableroDesdePlantilla(dialogo.getTableroCreado(), emailUsuario);
            cargarTablerosUsuario(emailUsuario);
            mostrarAlerta("Éxito", "Tablero importado correctamente desde plantilla");
            mostrarDetallesTablero(tableroCreado);
        } catch (Exception e) {
            mostrarError("Error", "No se pudo importar la plantilla: " + e.getMessage());
        }
    }

    private void mostrarDetallesTablero(TableroResponse tablero) {
        Stage ventana = new Stage();
        ventana.setTitle("Tablero: " + tablero.getTitulo());
        AtomicReference<TableroResponse> tableroActual = new AtomicReference<>(tablero);

        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));
        contenedor.getStyleClass().add("workspace-section");

        Label titulo = new Label(tableroActual.get().getTitulo());
        titulo.getStyleClass().add("detail-title");

        Label descripcion = new Label("Descripción: " +
            (tableroActual.get().getDescripcion() == null || tableroActual.get().getDescripcion().isBlank() ? "Sin descripción" : tableroActual.get().getDescripcion()));
        descripcion.getStyleClass().add("detail-subtitle");
        Label estado = new Label("Estado: " + (tableroActual.get().isBloqueado() ? "Bloqueado" : "Activo"));
        estado.getStyleClass().add("detail-subtitle");
        Label propietario = new Label("Propietario: " + tableroActual.get().getPropietarioEmail());
        propietario.getStyleClass().add("detail-subtitle");
        Label identificador = new Label("ID privado: " + tableroActual.get().getId());
        identificador.getStyleClass().add("detail-subtitle");

        Button btnCopiarId = new Button("Copiar ID");
        btnCopiarId.getStyleClass().add("secondary-button");
        btnCopiarId.setOnAction(e -> copiarAlPortapapeles(tableroActual.get().getId()));

        ListView<ListaResponse> listas = new ListView<>();
        listas.setPrefHeight(260);
        listas.getStyleClass().add("list-stack");
        listas.setPlaceholder(new Label("Este tablero todavía no tiene listas"));
        listas.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ListaResponse lista, boolean empty) {
                super.updateItem(lista, empty);

                if (empty || lista == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                VBox tarjetaLista = new VBox(7);
                tarjetaLista.getStyleClass().add("list-card");
                if (isSelected()) {
                    tarjetaLista.getStyleClass().add("list-card-selected");
                }

                Label nombreLista = new Label(lista.getNombre());
                nombreLista.getStyleClass().add("list-card-title");

                Label resumen = new Label(resumirEstadoLista(lista));
                resumen.getStyleClass().add("list-card-summary");
                resumen.setWrapText(true);

                Label metadata = new Label(lista.getTarjetasCompletadas() + " completadas de " + lista.getTotalTarjetas() + " tarjetas");
                metadata.getStyleClass().add("list-card-metadata");

                Label chipCantidad = new Label(lista.getTotalTarjetas() == 0 ? "Vacía" : lista.getTotalTarjetas() + " en lista");
                chipCantidad.getStyleClass().addAll("list-chip", lista.getTotalTarjetas() == 0 ? "list-chip-empty" : "list-chip-neutral");

                Label chipCompletadas = new Label(lista.getTarjetasCompletadas() + " completadas");
                chipCompletadas.getStyleClass().addAll("list-chip", "list-chip-done");

                HBox footer = new HBox(8, chipCantidad, chipCompletadas);
                footer.getStyleClass().add("list-card-footer");

                if (lista.getLimiteMaximo() != null) {
                    Label chipLimite = new Label("Límite " + lista.getLimiteMaximo());
                    chipLimite.getStyleClass().addAll("list-chip", "list-chip-limit");
                    footer.getChildren().add(chipLimite);
                }

                tarjetaLista.getChildren().addAll(nombreLista, resumen, metadata, footer);

                setText(null);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(tarjetaLista);
            }
        });
        listas.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                ListaResponse listaSeleccionada = listas.getSelectionModel().getSelectedItem();
                if (listaSeleccionada != null) {
                    mostrarDetalleLista(tableroActual.get(), listaSeleccionada, listas);
                }
            }
        });

        cargarListasTablero(tableroActual.get(), listas);

        Button btnAgregarLista = new Button("Agregar lista");
        btnAgregarLista.getStyleClass().add("primary-button");
        btnAgregarLista.setOnAction(e -> mostrarDialogoAgregarLista(tableroActual.get(), listas));

        Button btnAbrirLista = new Button("Abrir lista");
        btnAbrirLista.getStyleClass().add("secondary-button");
        btnAbrirLista.setOnAction(e -> {
            ListaResponse listaSeleccionada = listas.getSelectionModel().getSelectedItem();
            if (listaSeleccionada != null) {
                mostrarDetalleLista(tableroActual.get(), listaSeleccionada, listas);
            }
        });

        boolean esPropietario = tableroActual.get().getPropietarioEmail().equals(obtenerEmailContexto(tableroActual.get()));

        Button btnEditarTablero = new Button("Editar tablero");
        btnEditarTablero.getStyleClass().add("secondary-button");
        btnEditarTablero.setDisable(!esPropietario);
        btnEditarTablero.setOnAction(e -> mostrarDialogoEditarTablero(tableroActual, ventana, titulo, descripcion, estado, propietario, listas));

        Button btnCompartirTablero = new Button("Compartir tablero");
        btnCompartirTablero.getStyleClass().add("secondary-button");
        btnCompartirTablero.setDisable(!esPropietario);
        btnCompartirTablero.setOnAction(e -> mostrarDialogoCompartirTablero(tableroActual.get()));

        Button btnBloquearTablero = new Button(tableroActual.get().isBloqueado() ? "Desbloquear tablero" : "Bloquear tablero");
        btnBloquearTablero.getStyleClass().add("secondary-button");
        btnBloquearTablero.setDisable(!esPropietario);
        btnBloquearTablero.setOnAction(e -> alternarBloqueoTablero(tableroActual, ventana, titulo, descripcion, estado, propietario, listas, btnBloquearTablero));

        Button btnEliminarLista = new Button("Eliminar lista");
        btnEliminarLista.getStyleClass().add("danger-button");
        btnEliminarLista.setOnAction(e -> {
            ListaResponse listaSeleccionada = listas.getSelectionModel().getSelectedItem();
            if (listaSeleccionada == null) {
                mostrarError("Error", "Selecciona una lista para eliminarla");
                return;
            }

            eliminarLista(tableroActual.get(), listaSeleccionada, listas);
        });

        Button btnConfigurarReglas = new Button("Configurar reglas");
        btnConfigurarReglas.getStyleClass().add("secondary-button");
        btnConfigurarReglas.setOnAction(e -> {
            ListaResponse listaSeleccionada = listas.getSelectionModel().getSelectedItem();
            if (listaSeleccionada == null) {
                mostrarError("Error", "Selecciona una lista para configurar sus reglas");
                return;
            }

            mostrarDialogoConfigurarReglas(tableroActual.get(), listaSeleccionada, listas);
        });

        Button btnEliminarTablero = new Button("Eliminar tablero");
        btnEliminarTablero.getStyleClass().add("danger-button");
        btnEliminarTablero.setDisable(!esPropietario);
        btnEliminarTablero.setOnAction(e -> eliminarTablero(tableroActual.get(), ventana));

        Button btnVerHistorial = new Button("Ver historial");
        btnVerHistorial.getStyleClass().add("secondary-button");
        btnVerHistorial.setOnAction(e -> mostrarHistorialTablero(tableroActual.get()));

        Button btnExportarPlantilla = new Button("Exportar plantilla");
        btnExportarPlantilla.getStyleClass().add("secondary-button");
        btnExportarPlantilla.setOnAction(e -> mostrarDialogoExportarPlantilla(tableroActual.get(), listas));

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.getStyleClass().add("secondary-button");
        btnCerrar.setOnAction(e -> ventana.close());

        Label encabezadoListas = new Label("Listas del tablero");
        encabezadoListas.getStyleClass().add("board-card-meta");

        FlowPane acciones = new FlowPane(10, 10,
            btnAgregarLista, btnAbrirLista, btnEditarTablero, btnCompartirTablero, btnBloquearTablero,
            btnConfigurarReglas, btnVerHistorial, btnExportarPlantilla, btnEliminarLista, btnEliminarTablero, btnCerrar
        );
        acciones.getStyleClass().add("detail-actions");

        contenedor.getChildren().addAll(
            titulo,
            descripcion,
            propietario,
            estado,
            new HBox(10, identificador, btnCopiarId),
            new Separator(),
            encabezadoListas,
            listas,
            acciones
        );

        Scene escena = new Scene(contenedor, 900, 620);
        aplicarEstilos(escena);
        ventana.setScene(escena);
        ventana.show();
    }

    private void mostrarDialogoExportarPlantilla(TableroResponse tablero, ListView<ListaResponse> listas) {
        try {
            DialogoExportarPlantilla dialogo = new DialogoExportarPlantilla(
                tablero,
                obtenerEmailContexto(tablero),
                listas.getItems().size(),
                servicioTablero
            );
            dialogo.showAndWait();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo exportar la plantilla: " + e.getMessage());
        }
    }

    private void mostrarDialogoEditarTablero(AtomicReference<TableroResponse> tableroActual, Stage ventana,
                                             Label titulo, Label descripcion, Label estado,
                                             Label propietario, ListView<ListaResponse> listas) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Editar tablero");
        dialogo.setHeaderText("Actualizar datos del tablero");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField txtTitulo = new TextField(tableroActual.get().getTitulo());
        TextArea txtDescripcion = new TextArea(tableroActual.get().getDescripcion());
        txtDescripcion.setPrefRowCount(3);

        grid.add(new Label("Título:"), 0, 0);
        grid.add(txtTitulo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(txtDescripcion, 1, 1);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado != ButtonType.OK) {
                return;
            }

            try {
                servicioTablero.actualizarTablero(
                    tableroActual.get().getId(),
                    obtenerEmailContexto(tableroActual.get()),
                    ActualizarTableroRequest.builder()
                        .titulo(txtTitulo.getText() != null ? txtTitulo.getText().trim() : "")
                        .descripcion(txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "")
                        .build()
                );
                refrescarDetallesTablero(tableroActual, ventana, titulo, descripcion, estado, propietario, null, listas, null);
            } catch (Exception e) {
                mostrarError("Error", "No se pudo actualizar el tablero: " + e.getMessage());
            }
        });
    }

    private void mostrarDialogoCompartirTablero(TableroResponse tablero) {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Compartir tablero");
        dialogo.setHeaderText("Compartir '" + tablero.getTitulo() + "'");
        dialogo.setContentText("Email del usuario:");

        dialogo.showAndWait().ifPresent(emailUsuario -> {
            String emailNormalizado = emailUsuario != null ? emailUsuario.trim() : "";
            if (emailNormalizado.isEmpty()) {
                mostrarError("Error", "Debes indicar un email válido");
                return;
            }

            try {
                servicioTablero.compartirTablero(tablero.getId(), obtenerEmailContexto(tablero), emailNormalizado);
                mostrarAlerta("Éxito", "Tablero compartido correctamente");
                recargarTablerosSesionActiva();
            } catch (Exception e) {
                mostrarError("Error", "No se pudo compartir el tablero: " + e.getMessage());
            }
        });
    }

    private void alternarBloqueoTablero(AtomicReference<TableroResponse> tableroActual, Stage ventana,
                                        Label titulo, Label descripcion, Label estado,
                                        Label propietario, ListView<ListaResponse> listas,
                                        Button btnBloquearTablero) {
        try {
            if (tableroActual.get().isBloqueado()) {
                servicioTablero.desbloquearTablero(tableroActual.get().getId(), obtenerEmailContexto(tableroActual.get()));
            } else {
                TextInputDialog dialogo = new TextInputDialog("10");
                dialogo.setTitle("Bloquear tablero");
                dialogo.setHeaderText("Duración del bloqueo en minutos");
                dialogo.setContentText("Minutos:");

                var resultado = dialogo.showAndWait();
                if (resultado.isEmpty()) {
                    return;
                }

                int duracionMinutos = Integer.parseInt(resultado.get().trim());
                servicioTablero.bloquearTablero(
                    tableroActual.get().getId(),
                    obtenerEmailContexto(tableroActual.get()),
                    new BloquearTableroRequest(duracionMinutos)
                );
            }

            refrescarDetallesTablero(tableroActual, ventana, titulo, descripcion, estado, propietario, null, listas, btnBloquearTablero);
        } catch (NumberFormatException e) {
            mostrarError("Error", "La duración debe ser un número entero positivo");
        } catch (Exception e) {
            mostrarError("Error", "No se pudo actualizar el bloqueo del tablero: " + e.getMessage());
        }
    }

    private void refrescarDetallesTablero(AtomicReference<TableroResponse> tableroActual, Stage ventana,
                                          Label titulo, Label descripcion, Label estado,
                                          Label propietario, Label identificador,
                                          ListView<ListaResponse> listas,
                                          Button btnBloquearTablero) {
        TableroResponse tableroRefrescado = servicioTablero.obtenerTablero(
            tableroActual.get().getId(),
            obtenerEmailContexto(tableroActual.get())
        );
        tableroActual.set(tableroRefrescado);

        ventana.setTitle("Tablero: " + tableroRefrescado.getTitulo());
        titulo.setText(tableroRefrescado.getTitulo());
        descripcion.setText("Descripción: " +
            (tableroRefrescado.getDescripcion() == null || tableroRefrescado.getDescripcion().isBlank()
                ? "Sin descripción"
                : tableroRefrescado.getDescripcion()));
        estado.setText("Estado: " + (tableroRefrescado.isBloqueado() ? "Bloqueado" : "Activo"));
        propietario.setText("Propietario: " + tableroRefrescado.getPropietarioEmail());
        if (identificador != null) {
            identificador.setText("ID privado: " + tableroRefrescado.getId());
        }

        if (btnBloquearTablero != null) {
            btnBloquearTablero.setText(tableroRefrescado.isBloqueado() ? "Desbloquear tablero" : "Bloquear tablero");
        }

        cargarListasTablero(tableroRefrescado, listas);
        recargarTablerosSesionActiva();
    }

    private void mostrarDialogoAgregarLista(TableroResponse tablero, ListView<ListaResponse> listas) {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Agregar lista");
        dialogo.setHeaderText("Nueva lista para " + tablero.getTitulo());
        dialogo.setContentText("Nombre de la lista:");

        dialogo.showAndWait().ifPresent(nombreLista -> {
            String nombreNormalizado = nombreLista != null ? nombreLista.trim() : "";
            if (nombreNormalizado.isEmpty()) {
                mostrarError("Error", "El nombre de la lista no puede estar vacío");
                return;
            }

            try {
                servicioTablero.agregarLista(tablero.getId(), obtenerEmailContexto(tablero),
                    CrearListaRequest.builder().nombre(nombreNormalizado).build());
                cargarListasTablero(tablero, listas);
                recargarTablerosSesionActiva();
            } catch (Exception e) {
                mostrarError("Error", "No se pudo agregar la lista: " + e.getMessage());
            }
        });
    }

    private void mostrarDialogoConfigurarReglas(TableroResponse tablero, ListaResponse lista, ListView<ListaResponse> listas) {
        List<ListaResponse> listasDisponibles = servicioTablero.obtenerListas(tablero.getId(), obtenerEmailContexto(tablero)).stream()
            .filter(listaDisponible -> !listaDisponible.getId().equals(lista.getId()))
            .toList();

        DialogoConfigurarReglas dialogo = new DialogoConfigurarReglas(
            tablero.getId(),
            lista.getId(),
            lista.getNombre(),
            obtenerEmailContexto(tablero),
            servicioLista,
            listasDisponibles
        );
        dialogo.setOnHidden(evento -> cargarListasTablero(tablero, listas));
        dialogo.show();
    }

    private void mostrarHistorialTablero(TableroResponse tablero) {
        Stage ventana = new Stage();
        ventana.setTitle("Historial: " + tablero.getTitulo());

        ListView<RegistroAccionResponse> listaHistorial = new ListView<>();
        listaHistorial.setPlaceholder(new Label("No hay acciones registradas"));
        listaHistorial.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(RegistroAccionResponse registro, boolean empty) {
                super.updateItem(registro, empty);
                if (empty || registro == null) {
                    setText(null);
                    return;
                }

                setText(registro.getFecha() + " | " + registro.getTipo() + " | " + registro.getDetalles());
            }
        });

        try {
            listaHistorial.getItems().setAll(
                servicioTablero.obtenerHistorialTablero(tablero.getId(), obtenerEmailContexto(tablero))
            );
        } catch (Exception e) {
            mostrarError("Error", "No se pudo cargar el historial: " + e.getMessage());
            return;
        }

        Label titulo = new Label("Historial de acciones");
        titulo.getStyleClass().add("detail-title");

        VBox contenedor = new VBox(10, titulo, listaHistorial);
        contenedor.setPadding(new Insets(10));
        contenedor.getStyleClass().add("workspace-section");
        listaHistorial.getStyleClass().add("board-list");
        VBox.setVgrow(listaHistorial, javafx.scene.layout.Priority.ALWAYS);

        Scene escena = new Scene(contenedor, 760, 420);
        aplicarEstilos(escena);
        ventana.setScene(escena);
        ventana.show();
    }

    private void mostrarDetalleLista(TableroResponse tablero, ListaResponse lista, ListView<ListaResponse> listas) {
        Stage ventana = new Stage();
        ventana.setTitle(tablero.getTitulo() + " / " + lista.getNombre());
        Scene escena = new Scene(
            new PanelDetallesTablero(
                tablero.getId(),
                lista.getId(),
                lista.getNombre(),
                obtenerEmailContexto(tablero),
                servicioTarjeta,
                servicioLista,
                servicioTablero.obtenerListas(tablero.getId(), obtenerEmailContexto(tablero)),
                tablero.getPropietarioEmail().equals(obtenerEmailContexto(tablero)),
                Set.copyOf(tablero.getUsuariosCompartidos())
            ),
            820,
            560
        );
        aplicarEstilos(escena);
        ventana.setScene(escena);
        ventana.setOnHidden(evento -> cargarListasTablero(tablero, listas));
        ventana.show();
    }

    private void eliminarLista(TableroResponse tablero, ListaResponse lista, ListView<ListaResponse> listas) {
        if (!confirmarAccion("Eliminar lista", "Se eliminará la lista '" + lista.getNombre() + "'.")) {
            return;
        }

        try {
            servicioTablero.eliminarLista(tablero.getId(), lista.getId(), obtenerEmailContexto(tablero));
            cargarListasTablero(tablero, listas);
        } catch (Exception e) {
            mostrarError("Error", "No se pudo eliminar la lista: " + e.getMessage());
        }
    }

    private void eliminarTablero(TableroResponse tablero, Stage ventanaDetalle) {
        if (!confirmarAccion("Eliminar tablero", "Se eliminará el tablero '" + tablero.getTitulo() + "'.")) {
            return;
        }

        try {
            servicioTablero.eliminarTablero(tablero.getId(), obtenerEmailContexto(tablero));
            recargarTablerosSesionActiva();
            if (ventanaDetalle != null) {
                ventanaDetalle.close();
            }
        } catch (Exception e) {
            mostrarError("Error", "No se pudo eliminar el tablero: " + e.getMessage());
        }
    }

    private void cargarTablerosUsuario(String emailUsuario) {
        emailUsuarioActivo = emailUsuario;
        actualizarLista(listaTableros, servicioTablero.obtenerTablerosPropietario(emailUsuario), false);
        actualizarLista(listaCompartidos, servicioTablero.obtenerTablerosCompartidos(emailUsuario), true);
        actualizarResumenUsuario();
    }

    private void cargarListasTablero(TableroResponse tablero, ListView<ListaResponse> listas) {
        listas.getItems().setAll(servicioTablero.obtenerListas(tablero.getId(), obtenerEmailContexto(tablero)));
    }

    private void actualizarLista(ListView<TableroResponse> lista, List<TableroResponse> tableros, boolean compartidos) {
        if (lista == null) {
            return;
        }

        lista.getItems().setAll(tableros);
    }

    private void actualizarResumenUsuario() {
        if (labelUsuarioActivo == null || labelResumenCarga == null) {
            return;
        }

        String textoUsuario = emailUsuarioActivo == null || emailUsuarioActivo.isBlank()
            ? "Sin sesion iniciada"
            : "Sesión: " + emailUsuarioActivo;

        labelUsuarioActivo.setText(textoUsuario);
        labelUsuarioActivo.getStyleClass().removeAll("status-chip-live", "status-chip-muted");
        labelUsuarioActivo.getStyleClass().add(emailUsuarioActivo == null || emailUsuarioActivo.isBlank()
            ? "status-chip-muted"
            : "status-chip-live");

        int totalPropios = listaTableros != null ? listaTableros.getItems().size() : 0;
        int totalCompartidos = listaCompartidos != null ? listaCompartidos.getItems().size() : 0;
        labelResumenCarga.setText(totalPropios + " propios · " + totalCompartidos + " compartidos");
    }

    private String resumirTexto(String texto, int maximoCaracteres) {
        if (texto == null || texto.length() <= maximoCaracteres) {
            return texto;
        }

        return texto.substring(0, Math.max(0, maximoCaracteres - 1)) + "…";
    }

    private String resumirId(String idTablero) {
        if (idTablero == null || idTablero.isBlank()) {
            return "sin-id";
        }

        return idTablero.length() <= 8 ? idTablero : idTablero.substring(0, 8);
    }

    private String resumirEstadoLista(ListaResponse lista) {
        if (lista.getTotalTarjetas() == 0) {
            return "Lista preparada para recibir trabajo nuevo.";
        }

        if (lista.getTarjetasCompletadas() == lista.getTotalTarjetas()) {
            return "Todo el contenido actual de la lista está completado.";
        }

        if (lista.getTarjetasCompletadas() > 0) {
            return "La lista combina trabajo en curso con tarjetas ya cerradas.";
        }

        return "La lista tiene actividad abierta y aún no hay tarjetas completadas.";
    }

    private String obtenerEmailContexto(TableroResponse tablero) {
        return emailUsuarioActivo != null ? emailUsuarioActivo : resolverEmailUsuarioActivo();
    }

    private String iniciarSesionInteractiva(boolean mostrarConfirmacion) {
        TextInputDialog dialogo = new TextInputDialog(emailUsuarioActivo != null ? emailUsuarioActivo : "");
        dialogo.setTitle("Iniciar sesion");
        dialogo.setHeaderText("Solicita un codigo temporal de acceso");
        dialogo.setContentText("Email:");

        var resultadoEmail = dialogo.showAndWait();
        if (resultadoEmail.isEmpty()) {
            return null;
        }

        String email = resultadoEmail.get().trim();
        if (email.isEmpty()) {
            mostrarError("Error", "Debes indicar un email para iniciar sesion");
            return null;
        }

        try {
            SolicitarCodigoAccesoResponse solicitud = servicioAutenticacion.solicitarCodigo(
                SolicitarCodigoAccesoRequest.builder()
                    .email(email)
                    .build()
            );

            mostrarInstruccionesCodigo(solicitud);

            TextInputDialog dialogoCodigo = new TextInputDialog(
                solicitud.getCodigoDesarrollo() != null ? solicitud.getCodigoDesarrollo() : ""
            );
            dialogoCodigo.setTitle("Validar codigo");
            dialogoCodigo.setHeaderText("Introduce el codigo temporal para " + solicitud.getEmail());
            dialogoCodigo.setContentText("Codigo:");

            var resultadoCodigo = dialogoCodigo.showAndWait();
            if (resultadoCodigo.isEmpty()) {
                return null;
            }

            String codigo = resultadoCodigo.get().trim();
            if (codigo.isEmpty()) {
                mostrarError("Error", "Debes indicar el codigo de acceso");
                return null;
            }

            SesionAutenticadaResponse sesion = servicioAutenticacion.obtenerSesionActiva(codigo);
            codigoAccesoActivo = sesion.getCodigoAcceso();
            cargarTablerosUsuario(sesion.getEmail());

            if (mostrarConfirmacion) {
                mostrarAlerta("Sesion iniciada", "Acceso concedido para " + sesion.getEmail());
            }

            return sesion.getEmail();
        } catch (Exception e) {
            mostrarError("Error", "No se pudo iniciar sesion: " + e.getMessage());
            return null;
        }
    }

    private void mostrarInstruccionesCodigo(SolicitarCodigoAccesoResponse solicitud) {
        StringBuilder mensaje = new StringBuilder();
        if ("email".equalsIgnoreCase(solicitud.getModoEntrega())) {
            mensaje.append("Se ha enviado un codigo temporal al correo indicado.");
        } else {
            mensaje.append("Se ha generado un codigo temporal en modo desarrollo.");
        }

        if (solicitud.getCodigoDesarrollo() != null && !solicitud.getCodigoDesarrollo().isBlank()) {
            mensaje.append("\nCodigo: ").append(solicitud.getCodigoDesarrollo());
        }

        if (solicitud.getExpiraEn() != null) {
            mensaje.append("\nExpira: ").append(solicitud.getExpiraEn());
        }

        mostrarAlerta("Codigo de acceso", mensaje.toString());
    }

    private void cerrarSesionActual(boolean mostrarConfirmacion) {
        if (codigoAccesoActivo != null && !codigoAccesoActivo.isBlank()) {
            servicioAutenticacion.cerrarSesion(codigoAccesoActivo);
        }

        codigoAccesoActivo = null;
        emailUsuarioActivo = null;
        if (listaTableros != null) {
            listaTableros.getItems().clear();
        }
        if (listaCompartidos != null) {
            listaCompartidos.getItems().clear();
        }
        actualizarResumenUsuario();

        if (mostrarConfirmacion) {
            mostrarAlerta("Sesion cerrada", "La sesion actual se ha cerrado correctamente");
        }
    }

    private void recargarTablerosSesionActiva() {
        if (codigoAccesoActivo == null || codigoAccesoActivo.isBlank()) {
            return;
        }

        String email = resolverEmailUsuarioActivo();
        if (email != null) {
            cargarTablerosUsuario(email);
        }
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

    private boolean confirmarAccion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private String resolverEmailUsuarioActivo() {
        if (codigoAccesoActivo != null && !codigoAccesoActivo.isBlank()) {
            try {
                SesionAutenticadaResponse sesion = servicioAutenticacion.obtenerSesionActiva(codigoAccesoActivo);
                codigoAccesoActivo = sesion.getCodigoAcceso();
                emailUsuarioActivo = sesion.getEmail();
                actualizarResumenUsuario();
                return emailUsuarioActivo;
            } catch (Exception e) {
                cerrarSesionActual(false);
                mostrarError("Sesion expirada", "El codigo de acceso ya no es valido. Debes iniciar sesion de nuevo.");
            }
        }

        if (emailUsuarioActivo != null && !emailUsuarioActivo.isBlank()) {
            return emailUsuarioActivo;
        }

        return iniciarSesionInteractiva(false);
    }

    private String extraerIdTablero(String valorAcceso) {
        String normalizado = valorAcceso.trim();
        int indiceQuery = normalizado.indexOf('?');
        if (indiceQuery >= 0) {
            normalizado = normalizado.substring(0, indiceQuery);
        }
        while (normalizado.endsWith("/")) {
            normalizado = normalizado.substring(0, normalizado.length() - 1);
        }
        int ultimoSeparador = normalizado.lastIndexOf('/');
        return ultimoSeparador >= 0 ? normalizado.substring(ultimoSeparador + 1) : normalizado;
    }

    private void copiarAlPortapapeles(String texto) {
        ClipboardContent contenido = new ClipboardContent();
        contenido.putString(texto);
        Clipboard.getSystemClipboard().setContent(contenido);
        mostrarAlerta("Copiado", "El identificador del tablero se ha copiado al portapapeles");
    }

    public static VentanaPrincipal getInstance() {
        return instancia;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
