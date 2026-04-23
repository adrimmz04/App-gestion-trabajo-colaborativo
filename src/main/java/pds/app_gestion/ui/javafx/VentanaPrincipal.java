package pds.app_gestion.ui.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
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
import pds.app_gestion.application.dto.TableroResponse;
import pds.app_gestion.application.service.ServicioLista;
import pds.app_gestion.application.service.ServicioTablero;
import pds.app_gestion.application.service.ServicioTarjeta;
import pds.app_gestion.ui.dialog.DialogoExportarPlantilla;
import pds.app_gestion.ui.dialog.DialogoImportarPlantilla;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ventana principal de la aplicación JavaFX.
 * Proporciona una interfaz de usuario para gestionar tableros y tarjetas.
 */
public class VentanaPrincipal extends Application {

    private ConfigurableApplicationContext applicationContext;
    private ServicioLista servicioLista;
    private ServicioTablero servicioTablero;
    private ServicioTarjeta servicioTarjeta;
    private ListView<TableroResponse> listaTableros;
    private ListView<TableroResponse> listaCompartidos;
    private String emailUsuarioActivo;

    private static VentanaPrincipal instancia;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(pds.app_gestion.Application.class)
            .headless(false)
            .run(getParameters().getRaw().toArray(new String[0]));
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

        listaTableros = new ListView<>();
        listaTableros.setPrefHeight(400);
        listaTableros.setCellFactory(listView -> crearCeldaTablero(false));
        listaTableros.setPlaceholder(new Label("Carga un email para ver los tableros disponibles"));
        listaTableros.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                TableroResponse tableroSeleccionado = listaTableros.getSelectionModel().getSelectedItem();
                if (tableroSeleccionado != null) {
                    mostrarDetallesTablero(tableroSeleccionado);
                }
            }
        });

        Button btnCrear = new Button("+ Crear Tablero");
        btnCrear.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnCrear.setOnAction(e -> mostrarDialogoCrearTablero());

        Button btnCargar = new Button("Cargar por email");
        btnCargar.setOnAction(e -> mostrarDialogoCargarTableros());

        Button btnAbrirPorId = new Button("Abrir por ID/URL");
        btnAbrirPorId.setOnAction(e -> mostrarDialogoAbrirTableroPorId());

        Button btnImportarPlantilla = new Button("Importar plantilla");
        btnImportarPlantilla.setOnAction(e -> mostrarDialogoImportarPlantilla());

        Button btnVerDetalle = new Button("Ver detalle");
        btnVerDetalle.setOnAction(e -> {
            TableroResponse tableroSeleccionado = listaTableros.getSelectionModel().getSelectedItem();
            if (tableroSeleccionado != null) {
                mostrarDetallesTablero(tableroSeleccionado);
            }
        });

        Button btnEliminar = new Button("Eliminar tablero");
        btnEliminar.setOnAction(e -> {
            TableroResponse tableroSeleccionado = listaTableros.getSelectionModel().getSelectedItem();
            if (tableroSeleccionado == null) {
                mostrarError("Error", "Selecciona un tablero para eliminarlo");
                return;
            }

            eliminarTablero(tableroSeleccionado, null);
        });

        vbox.getChildren().addAll(titulo, listaTableros, new HBox(10, btnCrear, btnCargar, btnAbrirPorId, btnImportarPlantilla, btnVerDetalle, btnEliminar));
        return vbox;
    }

    private VBox crearVistaTablerosCompartidos() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titulo = new Label("Tableros Compartidos");
        titulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        listaCompartidos = new ListView<>();
        listaCompartidos.setPrefHeight(400);
        listaCompartidos.setCellFactory(listView -> crearCeldaTablero(true));
        listaCompartidos.setPlaceholder(new Label("Carga un email para ver tableros compartidos"));
        listaCompartidos.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                TableroResponse tableroSeleccionado = listaCompartidos.getSelectionModel().getSelectedItem();
                if (tableroSeleccionado != null) {
                    mostrarDetallesTablero(tableroSeleccionado);
                }
            }
        });

        vbox.getChildren().addAll(titulo, listaCompartidos);
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

                String sufijo = compartido
                    ? " (propietario: " + tablero.getPropietarioEmail() + ")"
                    : (tablero.isBloqueado() ? " [bloqueado]" : "");
                setText(tablero.getTitulo() + sufijo);
            }
        };
    }

    private void mostrarDialogoCargarTableros() {
        TextInputDialog dialogo = new TextInputDialog(emailUsuarioActivo != null ? emailUsuarioActivo : "");
        dialogo.setTitle("Cargar tableros");
        dialogo.setHeaderText("Introduce el email del usuario");
        dialogo.setContentText("Email:");

        dialogo.showAndWait().ifPresent(email -> {
            if (email == null || email.trim().isEmpty()) {
                mostrarError("Error", "Debes indicar un email para cargar tableros");
                return;
            }

            try {
                cargarTablerosUsuario(email.trim());
            } catch (Exception e) {
                mostrarError("Error", "No se pudieron cargar los tableros: " + e.getMessage());
            }
        });
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
                    String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
                    String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
                    String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";

                    if (titulo.isEmpty() || email.isEmpty()) {
                        throw new IllegalArgumentException("El título y el email son obligatorios");
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
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Abrir tablero");
        dialogo.setHeaderText("Abrir tablero por ID o URL privada");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField txtEmail = new TextField(emailUsuarioActivo != null ? emailUsuarioActivo : "");
        txtEmail.setPromptText("Tu email");

        TextField txtId = new TextField();
        txtId.setPromptText("ID o URL del tablero");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(txtEmail, 1, 0);
        grid.add(new Label("ID o URL:"), 0, 1);
        grid.add(txtId, 1, 1);

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado != ButtonType.OK) {
                return;
            }

            String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
            String valorAcceso = txtId.getText() != null ? txtId.getText().trim() : "";
            if (email.isEmpty() || valorAcceso.isEmpty()) {
                mostrarError("Error", "Debes indicar el email y el ID o URL del tablero");
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

        Label titulo = new Label(tableroActual.get().getTitulo());
        titulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label descripcion = new Label("Descripción: " +
            (tableroActual.get().getDescripcion() == null || tableroActual.get().getDescripcion().isBlank() ? "Sin descripción" : tableroActual.get().getDescripcion()));
        Label estado = new Label("Estado: " + (tableroActual.get().isBloqueado() ? "Bloqueado" : "Activo"));
        Label propietario = new Label("Propietario: " + tableroActual.get().getPropietarioEmail());
        Label identificador = new Label("ID privado: " + tableroActual.get().getId());

        Button btnCopiarId = new Button("Copiar ID");
        btnCopiarId.setOnAction(e -> copiarAlPortapapeles(tableroActual.get().getId()));

        ListView<ListaResponse> listas = new ListView<>();
        listas.setPrefHeight(260);
        listas.setPlaceholder(new Label("Este tablero todavía no tiene listas"));
        listas.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ListaResponse lista, boolean empty) {
                super.updateItem(lista, empty);

                if (empty || lista == null) {
                    setText(null);
                    return;
                }

                String sufijoLimite = lista.getLimiteMaximo() != null ? ", límite " + lista.getLimiteMaximo() : "";
                setText(lista.getNombre() + " (" + lista.getTotalTarjetas() + " tarjetas" + sufijoLimite + ")");
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
        btnAgregarLista.setOnAction(e -> mostrarDialogoAgregarLista(tableroActual.get(), listas));

        Button btnAbrirLista = new Button("Abrir lista");
        btnAbrirLista.setOnAction(e -> {
            ListaResponse listaSeleccionada = listas.getSelectionModel().getSelectedItem();
            if (listaSeleccionada != null) {
                mostrarDetalleLista(tableroActual.get(), listaSeleccionada, listas);
            }
        });

        boolean esPropietario = tableroActual.get().getPropietarioEmail().equals(obtenerEmailContexto(tableroActual.get()));

        Button btnEditarTablero = new Button("Editar tablero");
        btnEditarTablero.setDisable(!esPropietario);
        btnEditarTablero.setOnAction(e -> mostrarDialogoEditarTablero(tableroActual, ventana, titulo, descripcion, estado, propietario, listas));

        Button btnCompartirTablero = new Button("Compartir tablero");
        btnCompartirTablero.setDisable(!esPropietario);
        btnCompartirTablero.setOnAction(e -> mostrarDialogoCompartirTablero(tableroActual.get()));

        Button btnBloquearTablero = new Button(tableroActual.get().isBloqueado() ? "Desbloquear tablero" : "Bloquear tablero");
        btnBloquearTablero.setDisable(!esPropietario);
        btnBloquearTablero.setOnAction(e -> alternarBloqueoTablero(tableroActual, ventana, titulo, descripcion, estado, propietario, listas, btnBloquearTablero));

        Button btnEliminarLista = new Button("Eliminar lista");
        btnEliminarLista.setOnAction(e -> {
            ListaResponse listaSeleccionada = listas.getSelectionModel().getSelectedItem();
            if (listaSeleccionada == null) {
                mostrarError("Error", "Selecciona una lista para eliminarla");
                return;
            }

            eliminarLista(tableroActual.get(), listaSeleccionada, listas);
        });

        Button btnConfigurarReglas = new Button("Configurar reglas");
        btnConfigurarReglas.setOnAction(e -> {
            ListaResponse listaSeleccionada = listas.getSelectionModel().getSelectedItem();
            if (listaSeleccionada == null) {
                mostrarError("Error", "Selecciona una lista para configurar sus reglas");
                return;
            }

            mostrarDialogoConfigurarReglas(tableroActual.get(), listaSeleccionada, listas);
        });

        Button btnEliminarTablero = new Button("Eliminar tablero");
        btnEliminarTablero.setDisable(!esPropietario);
        btnEliminarTablero.setOnAction(e -> eliminarTablero(tableroActual.get(), ventana));

        Button btnVerHistorial = new Button("Ver historial");
        btnVerHistorial.setOnAction(e -> mostrarHistorialTablero(tableroActual.get()));

        Button btnExportarPlantilla = new Button("Exportar plantilla");
        btnExportarPlantilla.setOnAction(e -> mostrarDialogoExportarPlantilla(tableroActual.get(), listas));

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(e -> ventana.close());

        contenedor.getChildren().addAll(
            titulo,
            descripcion,
            propietario,
            estado,
            new HBox(10, identificador, btnCopiarId),
            new Separator(),
            new Label("Listas"),
            listas,
            new HBox(10, btnAgregarLista, btnAbrirLista, btnEditarTablero, btnCompartirTablero, btnBloquearTablero, btnConfigurarReglas, btnVerHistorial, btnExportarPlantilla, btnEliminarLista, btnEliminarTablero, btnCerrar)
        );

        ventana.setScene(new Scene(contenedor, 640, 520));
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
                if (emailUsuarioActivo != null) {
                    cargarTablerosUsuario(emailUsuarioActivo);
                }
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
        if (emailUsuarioActivo != null) {
            cargarTablerosUsuario(emailUsuarioActivo);
        }
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
                if (emailUsuarioActivo != null) {
                    cargarTablerosUsuario(emailUsuarioActivo);
                }
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

        VBox contenedor = new VBox(10, new Label("Historial de acciones"), listaHistorial);
        contenedor.setPadding(new Insets(10));
        VBox.setVgrow(listaHistorial, javafx.scene.layout.Priority.ALWAYS);

        ventana.setScene(new Scene(contenedor, 760, 420));
        ventana.show();
    }

    private void mostrarDetalleLista(TableroResponse tablero, ListaResponse lista, ListView<ListaResponse> listas) {
        Stage ventana = new Stage();
        ventana.setTitle(tablero.getTitulo() + " / " + lista.getNombre());
        ventana.setScene(new Scene(
            new PanelDetallesTablero(
                tablero.getId(),
                lista.getId(),
                lista.getNombre(),
                obtenerEmailContexto(tablero),
                servicioTarjeta,
                servicioLista,
                servicioTablero.obtenerListas(tablero.getId(), obtenerEmailContexto(tablero))
            ),
            820,
            560
        ));
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
            if (emailUsuarioActivo != null) {
                cargarTablerosUsuario(emailUsuarioActivo);
            }
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

    private String obtenerEmailContexto(TableroResponse tablero) {
        return emailUsuarioActivo != null ? emailUsuarioActivo : tablero.getPropietarioEmail();
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
        if (emailUsuarioActivo != null && !emailUsuarioActivo.isBlank()) {
            return emailUsuarioActivo;
        }

        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Email del usuario");
        dialogo.setHeaderText("Indica el email para importar la plantilla");
        dialogo.setContentText("Email:");

        var resultado = dialogo.showAndWait();
        if (resultado.isEmpty() || resultado.get().trim().isEmpty()) {
            mostrarError("Error", "Debes indicar un email para continuar");
            return null;
        }

        return resultado.get().trim();
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
