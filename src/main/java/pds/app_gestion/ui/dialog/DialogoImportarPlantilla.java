package pds.app_gestion.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pds.app_gestion.application.dto.PlantillaTableroYAML;
import pds.app_gestion.application.service.ServicioPlantillas;
import pds.app_gestion.domain.Tablero;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Diálogo para importar una plantilla YAML y crear un nuevo tablero.
 */
public class DialogoImportarPlantilla extends Stage {

    private final ServicioPlantillas servicioPlantillas;
    private final String emailUsuario;
    private PlantillaTableroYAML plantillaSeleccionada;
    
    private TextField txtRuta;
    private TextField txtNombreTablero;
    private Label labelEstado;
    private Button btnCrear;
    private Tablero tableroCreado;

    public DialogoImportarPlantilla(String emailUsuario) {
        this.emailUsuario = emailUsuario;
        this.servicioPlantillas = new ServicioPlantillas();
        this.tableroCreado = null;
        this.plantillaSeleccionada = null;
        
        initUI();
    }

    private void initUI() {
        setTitle("Importar Plantilla YAML");
        setWidth(500);
        setHeight(350);
        setResizable(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Título
        Label titulo = new Label("Crear Tablero desde Plantilla");
        titulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Plantillas ejemplo
        VBox plantillasBox = createPlantillasEjemplo();

        // Separador
        Separator sep1 = new Separator();

        // Selección de archivo
        VBox archivoBox = createSeleccionArchivoBox();

        // Nombre del nuevo tablero
        VBox nombreBox = createNombreTableroBox();

        // Estado
        labelEstado = new Label("Selecciona una plantilla o archivo");
        labelEstado.setStyle("-fx-text-fill: #0066cc;");

        // Botones
        HBox botonesBox = createBotonesBox();

        ScrollPane scrollPane = new ScrollPane(new VBox(10, plantillasBox, sep1, archivoBox, nombreBox, labelEstado));
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(
            titulo,
            new Separator(),
            scrollPane,
            new Separator(),
            botonesBox
        );

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private VBox createPlantillasEjemplo() {
        VBox box = new VBox(10);
        Label label = new Label("Plantillas Disponibles:");
        label.setStyle("-fx-font-weight: bold;");

        for (PlantillaTableroYAML plantilla : servicioPlantillas.obtenerPlantillasEjemplo()) {
            Button btn = new Button(plantilla.getTitulo());
            btn.setPrefWidth(200);
            btn.setWrapText(true);
            btn.setStyle("-fx-font-size: 11; -fx-padding: 8;");
            btn.setOnAction(e -> handleSeleccionarPlantilla(plantilla));
            
            Label desc = new Label(plantilla.getDescripcion());
            desc.setStyle("-fx-font-size: 10; -fx-text-fill: #666666;");
            
            VBox plantillaBox = new VBox(3);
            plantillaBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 3; -fx-padding: 8;");
            plantillaBox.getChildren().addAll(btn, desc);
            
            box.getChildren().add(plantillaBox);
        }

        return box;
    }

    private VBox createSeleccionArchivoBox() {
        VBox box = new VBox(8);
        Label label = new Label("O carga desde archivo:");
        label.setStyle("-fx-font-weight: bold;");

        HBox rutaBox = new HBox(5);
        txtRuta = new TextField();
        txtRuta.setPromptText("Selecciona un archivo YAML...");
        txtRuta.setEditable(false);
        txtRuta.setPrefWidth(300);

        Button btnBuscar = new Button("Examinar");
        btnBuscar.setPrefWidth(80);
        btnBuscar.setStyle("-fx-font-size: 11;");
        btnBuscar.setOnAction(e -> handleExaminarArchivo());

        rutaBox.getChildren().addAll(txtRuta, btnBuscar);

        box.getChildren().addAll(label, rutaBox);
        return box;
    }

    private VBox createNombreTableroBox() {
        VBox box = new VBox(8);
        Label label = new Label("Nombre del nuevo tablero:");
        label.setStyle("-fx-font-weight: bold;");

        txtNombreTablero = new TextField();
        txtNombreTablero.setPromptText("Ingresa el nombre del tablero...");
        txtNombreTablero.setPrefWidth(300);

        box.getChildren().addAll(label, txtNombreTablero);
        return box;
    }

    private HBox createBotonesBox() {
        HBox box = new HBox(10);
        box.setStyle("-fx-alignment: center-right;");

        btnCrear = new Button("Crear Tablero");
        btnCrear.setPrefWidth(120);
        btnCrear.setStyle("-fx-font-size: 11;");
        btnCrear.setOnAction(e -> handleCrearTablero());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(120);
        btnCancelar.setStyle("-fx-font-size: 11;");
        btnCancelar.setOnAction(e -> close());

        box.getChildren().addAll(btnCrear, btnCancelar);
        return box;
    }

    private void handleSeleccionarPlantilla(PlantillaTableroYAML plantilla) {
        plantillaSeleccionada = plantilla;
        tableroCreado = null;
        txtRuta.clear();
        if (txtNombreTablero.getText().isEmpty()) {
            txtNombreTablero.setText(plantilla.getTitulo());
        }
        labelEstado.setText("Plantilla seleccionada: " + plantilla.getTitulo());
        labelEstado.setStyle("-fx-text-fill: #0066cc;");
    }

    private void handleExaminarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Plantilla YAML");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("YAML Files (*.yaml)", "*.yaml")
        );

        File file = fileChooser.showOpenDialog(this);
        if (file != null) {
            try {
                String yamlContent = new String(Files.readAllBytes(file.toPath()));
                PlantillaTableroYAML plantilla = servicioPlantillas.importarPlantillaYAML(yamlContent);
                plantillaSeleccionada = plantilla;
                tableroCreado = null;
                
                txtRuta.setText(file.getAbsolutePath());
                if (txtNombreTablero.getText().isEmpty()) {
                    txtNombreTablero.setText(plantilla.getTitulo());
                }
                
                labelEstado.setText("Archivo cargado: " + file.getName());
                labelEstado.setStyle("-fx-text-fill: #0066cc;");
            } catch (IOException ex) {
                labelEstado.setText("✗ Error al cargar archivo: " + ex.getMessage());
                labelEstado.setStyle("-fx-text-fill: #cc0000;");
            }
        }
    }

    private void crearTableroDesdePlantilla(PlantillaTableroYAML plantilla) {
        if (txtNombreTablero.getText().isEmpty()) {
            labelEstado.setText("⚠ Ingresa un nombre para el tablero");
            labelEstado.setStyle("-fx-text-fill: #ff9900;");
            return;
        }

        String idTablero = UUID.randomUUID().toString();
        tableroCreado = servicioPlantillas.crearTableroDesdePlantilla(
            idTablero,
            txtNombreTablero.getText(),
            emailUsuario,
            plantilla
        );

        labelEstado.setText("✓ Tablero preparado: " + tableroCreado.getTitulo());
        labelEstado.setStyle("-fx-text-fill: #00aa00;");
    }

    private void handleCrearTablero() {
        if (plantillaSeleccionada == null) {
            labelEstado.setText("✗ Selecciona una plantilla primero");
            labelEstado.setStyle("-fx-text-fill: #cc0000;");
            return;
        }

        if (txtNombreTablero.getText() == null || txtNombreTablero.getText().trim().isEmpty()) {
            labelEstado.setText("✗ Debes indicar un nombre para el tablero");
            labelEstado.setStyle("-fx-text-fill: #cc0000;");
            return;
        }

        crearTableroDesdePlantilla(plantillaSeleccionada);
        if (tableroCreado == null) {
            return;
        }

        close();
    }

    public Tablero getTableroCreado() {
        return tableroCreado;
    }
}
