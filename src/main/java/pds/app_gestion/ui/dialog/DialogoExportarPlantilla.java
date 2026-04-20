package pds.app_gestion.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pds.app_gestion.application.service.ServicioPlantillas;
import pds.app_gestion.domain.Tablero;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Diálogo para exportar un tablero como plantilla YAML.
 */
public class DialogoExportarPlantilla extends Stage {

    private final ServicioPlantillas servicioPlantillas;
    private final Tablero tablero;
    private Label labelEstado;
    private boolean exportado;

    public DialogoExportarPlantilla(Tablero tablero) {
        this.tablero = tablero;
        this.servicioPlantillas = new ServicioPlantillas();
        this.exportado = false;
        
        initUI();
    }

    private void initUI() {
        setTitle("Exportar Plantilla YAML");
        setWidth(400);
        setHeight(250);
        setResizable(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Título
        Label titulo = new Label("Exportar Tablero como Plantilla");
        titulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Información del tablero
        VBox infoBox = createInfoBox();

        // Descripción
        Label desc = new Label("Selecciona una ubicación para guardar la plantilla YAML");
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 11;");

        // Estado
        labelEstado = new Label("Listo para exportar");
        labelEstado.setStyle("-fx-text-fill: #0066cc;");

        // Botones
        HBox botonesBox = createBotonesBox();

        root.getChildren().addAll(
            titulo,
            new Separator(),
            infoBox,
            desc,
            new Separator(),
            labelEstado,
            createSpacer(),
            botonesBox
        );

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private VBox createInfoBox() {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 3; -fx-padding: 10;");

        Label labelTitulo = new Label("Tablero: " + tablero.getTitulo());
        labelTitulo.setStyle("-fx-font-weight: bold;");

        Label labelPropietario = new Label("Propietario: " + tablero.getPropietarioEmail());

        int cantidadListas = tablero.obtenerListas().size();
        int cantidadTarjetas = tablero.obtenerListas().stream()
            .mapToInt(l -> l.getTarjetas().size())
            .sum();
        Label labelCantidad = new Label(String.format("Listas: %d | Tarjetas: %d", cantidadListas, cantidadTarjetas));

        box.getChildren().addAll(labelTitulo, labelPropietario, labelCantidad);
        return box;
    }

    private HBox createBotonesBox() {
        HBox box = new HBox(10);
        box.setStyle("-fx-alignment: center-right;");

        Button btnExportar = new Button("Exportar");
        btnExportar.setPrefWidth(100);
        btnExportar.setStyle("-fx-font-size: 11;");
        btnExportar.setOnAction(e -> handleExportar());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(100);
        btnCancelar.setStyle("-fx-font-size: 11;");
        btnCancelar.setOnAction(e -> close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(spacer, btnExportar, btnCancelar);
        return box;
    }

    private void handleExportar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Plantilla YAML");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("YAML Files (*.yaml)", "*.yaml")
        );
        fileChooser.setInitialFileName(tablero.getTitulo() + ".yaml");

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            try {
                String yamlContent = servicioPlantillas.exportarTableroComoYAML(tablero);
                FileWriter writer = new FileWriter(file);
                writer.write(yamlContent);
                writer.close();

                labelEstado.setText("✓ Plantilla exportada exitosamente a: " + file.getName());
                labelEstado.setStyle("-fx-text-fill: #00aa00;");
                exportado = true;

                // Cerrar después de 2 segundos
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(this::close);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } catch (IOException ex) {
                labelEstado.setText("✗ Error al guardar: " + ex.getMessage());
                labelEstado.setStyle("-fx-text-fill: #cc0000;");
            }
        }
    }

    public boolean wasExported() {
        return exportado;
    }

    private Region createSpacer() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

}
