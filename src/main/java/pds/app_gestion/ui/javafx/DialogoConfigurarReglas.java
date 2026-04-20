package pds.app_gestion.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pds.app_gestion.application.dto.ConfigurarReglasListaRequest;
import pds.app_gestion.application.service.ServicioLista;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para configurar reglas de una lista.
 * 
 * Permite especificar:
 * - Límite máximo de tarjetas
 * - Listas previas que deben completarse antes
 */
public class DialogoConfigurarReglas extends Stage {
    
    private final String idTablero;
    private final String idLista;
    private final String nombreLista;
    private final String emailUsuario;
    private final ServicioLista servicioLista;
    private final List<String> listasDisponibles;
    
    private Spinner<Integer> spinnerLimite;
    private ListView<String> listaViewListasPrevias;
    private List<String> listasSeleccionadas;

    /**
     * Crea un nuevo diálogo para configurar reglas de lista.
     * 
     * @param idTablero ID del tablero
     * @param idLista ID de la lista
     * @param nombreLista nombre de la lista
     * @param emailUsuario email del usuario
     * @param servicioLista servicio para guardar reglas
     * @param listasDisponibles lista de IDs de listas disponibles (excluyendo la actual)
     */
    public DialogoConfigurarReglas(String idTablero, String idLista, String nombreLista,
                                  String emailUsuario, ServicioLista servicioLista,
                                  List<String> listasDisponibles) {
        super();
        this.idTablero = idTablero;
        this.idLista = idLista;
        this.nombreLista = nombreLista;
        this.emailUsuario = emailUsuario;
        this.servicioLista = servicioLista;
        this.listasDisponibles = new ArrayList<>(listasDisponibles);
        this.listasSeleccionadas = new ArrayList<>();
        
        inicializarUI();
        this.setTitle("Configurar Reglas: " + nombreLista);
        this.setWidth(450);
        this.setHeight(400);
    }

    /**
     * Inicializa la interfaz gráfica del diálogo.
     */
    private void inicializarUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        
        // Sección: Límite máximo
        VBox sectionLimite = crearSeccionLimite();
        root.getChildren().add(sectionLimite);
        
        // Separador
        Separator sep1 = new Separator();
        root.getChildren().add(sep1);
        
        // Sección: Listas previas
        VBox sectionListasPrevias = crearSeccionListasPrevias();
        VBox.setVgrow(sectionListasPrevias, javafx.scene.layout.Priority.ALWAYS);
        root.getChildren().add(sectionListasPrevias);
        
        // Separador
        Separator sep2 = new Separator();
        root.getChildren().add(sep2);
        
        // Botones
        HBox botonesBox = crearBotones();
        root.getChildren().add(botonesBox);
        
        Scene scene = new Scene(root);
        this.setScene(scene);
    }

    /**
     * Crea la sección para configurar el límite máximo.
     */
    private VBox crearSeccionLimite() {
        VBox section = new VBox(10);
        
        Label titulo = new Label("Límite Máximo de Tarjetas");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        
        HBox contenedor = new HBox(10);
        
        Label labelLimit = new Label("Máximo (0 = sin límite):");
        spinnerLimite = new Spinner<>(0, 100, 0, 1);
        spinnerLimite.setPrefWidth(80);
        spinnerLimite.setEditable(true);
        
        contenedor.getChildren().addAll(labelLimit, spinnerLimite);
        
        section.getChildren().addAll(titulo, contenedor);
        return section;
    }

    /**
     * Crea la sección para configurar listas previas.
     */
    private VBox crearSeccionListasPrevias() {
        VBox section = new VBox(10);
        
        Label titulo = new Label("Listas Previas (Prerequisitos)");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        
        Label instrucciones = new Label("Selecciona las listas que deben completarse antes:");
        instrucciones.setStyle("-fx-font-size: 11;");
        instrucciones.setWrapText(true);
        
        // ListView para seleccionar listas previas
        listaViewListasPrevias = new ListView<>();
        listaViewListasPrevias.getItems().addAll(listasDisponibles);
        listaViewListasPrevias.setMaxHeight(Double.MAX_VALUE);
        listaViewListasPrevias.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        VBox.setVgrow(listaViewListasPrevias, javafx.scene.layout.Priority.ALWAYS);
        
        section.getChildren().addAll(titulo, instrucciones, listaViewListasPrevias);
        return section;
    }

    /**
     * Crea los botones de acción.
     */
    private HBox crearBotones() {
        HBox box = new HBox(10);
        box.setStyle("-fx-alignment: center-right;");
        
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setPrefWidth(100);
        btnGuardar.setStyle("-fx-font-size: 11;");
        btnGuardar.setOnAction(e -> guardarReglas());
        
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefWidth(100);
        btnCancelar.setStyle("-fx-font-size: 11;");
        btnCancelar.setOnAction(e -> this.close());
        
        box.getChildren().addAll(btnGuardar, btnCancelar);
        return box;
    }

    /**
     * Guarda las reglas configuradas.
     */
    private void guardarReglas() {
        try {
            // Obtener límite máximo
            Integer limite = spinnerLimite.getValue();
            if (limite == 0) {
                limite = null; // Sin límite
            }
            
            // Obtener listas seleccionadas
            listasSeleccionadas = new ArrayList<>(
                listaViewListasPrevias.getSelectionModel().getSelectedItems()
            );
            
            // Crear request
            ConfigurarReglasListaRequest request = ConfigurarReglasListaRequest.builder()
                .limiteMaximo(limite)
                .listasPrevias(listasSeleccionadas)
                .build();
            
            // Guardar reglas
            servicioLista.configurarReglas(idTablero, idLista, emailUsuario, request);
            
            mostrarExito("Reglas guardadas exitosamente");
            this.close();
        } catch (Exception e) {
            mostrarError("Error al guardar reglas: " + e.getMessage());
        }
    }

    /**
     * Muestra un mensaje de éxito.
     */
    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
