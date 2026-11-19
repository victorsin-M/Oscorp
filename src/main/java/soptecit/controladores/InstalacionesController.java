package soptecit.controladores;

import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import soptecit.util.Mensajes;
import soptecit.util.Tablas;

/**
 * Controlador estilizado de Instalaciones conectado a proyectos y técnicos reales de MySQL.
 */
public class InstalacionesController implements Initializable {

    private static final String MODULO = "Instalaciones";
    private final ProyectoService proyectoService = new ProyectoService();
    private final EmpleadoService empleadoService = new EmpleadoService();

    @FXML private TableView<String[]> tblInstalaciones;
    @FXML private TableView<String[]> tblHistorial;
    @FXML private Label lblProyectoHistorial;

    @FXML private ComboBox<Proyecto> cmbProyectoAvance;
    @FXML private DatePicker dpFechaAvance;
    @FXML private Spinner<Integer> spnHoras;
    @FXML private ComboBox<String> cmbTarea;
    @FXML private ComboBox<Empleado> cmbTecnico;
    @FXML private Slider sldAvance;
    @FXML private Label lblPorcentaje;
    @FXML private ComboBox<String> cmbEstadoAvance;
    @FXML private TextField txtMaterialUsado;
    @FXML private TextArea txtObservacionesAvance;
    @FXML private Label lblEvidencias;
    @FXML private CheckBox chkTerminado;

    private List<Proyecto> proyectosCargados = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpFechaAvance.setValue(LocalDate.now());
        spnHoras.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 8));
        cmbEstadoAvance.setValue("En proceso");

        sldAvance.valueProperty().addListener((o, a, n) -> lblPorcentaje.setText(Math.round(n.doubleValue()) + " %"));
        chkTerminado.selectedProperty().addListener((o, a, terminado) -> {
            if (terminado) {
                sldAvance.setValue(100);
                cmbEstadoAvance.setValue("En revisión");
            }
        });

        Tablas.cargar(tblHistorial, new String[][]{});

        tblInstalaciones.getSelectionModel().selectedItemProperty().addListener((o, a, fila) -> {
            if (fila != null) {
                lblProyectoHistorial.setText(fila[0] + " · " + fila[2] + " (" + fila[1] + ")");
                try {
                    sldAvance.setValue(Double.parseDouble(fila[3]));
                } catch (NumberFormatException ignored) {
                }
                int idx = tblInstalaciones.getSelectionModel().getSelectedIndex();
                if (idx >= 0 && idx < proyectosCargados.size()) {
                    Proyecto p = proyectosCargados.get(idx);
                    cmbProyectoAvance.setValue(p);
                    if (p.getEstado() != null) {
                        cmbEstadoAvance.setValue(p.getEstado());
                    }
                }
            }
        });

        cargarDatos(null);
    }

    @FXML
    private void cargarDatos(ActionEvent event) {
        try {
            proyectosCargados = proyectoService.listar(false);
            cmbProyectoAvance.setItems(FXCollections.observableArrayList(proyectosCargados));

            List<Empleado> tecnicos = empleadoService.listar(false);
            cmbTecnico.setItems(FXCollections.observableArrayList(tecnicos));

            String[][] filas = new String[proyectosCargados.size()][6];
            for (int i = 0; i < proyectosCargados.size(); i++) {
                Proyecto p = proyectosCargados.get(i);
                filas[i] = new String[]{
                    "PRY-" + String.format("%04d", p.getIdProyecto()),
                    p.getClienteNombre() != null ? p.getClienteNombre() : "Sin cliente",
                    p.getNombreProyecto(),
                    String.valueOf(InicioController.porcentajePorEstado(p.getEstado())),
                    p.getFechaEstimadaFin() != null ? p.getFechaEstimadaFin().toString() : "—",
                    p.getEstado() != null ? p.getEstado() : "Planeado"
                };
            }
            Tablas.cargar(tblInstalaciones, filas);
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MODULO, "InstalacionesController", "CARGAR_DATOS", ex);
            Tablas.cargar(tblInstalaciones, new String[0][0]);
        }
    }

    @FXML
    private void adjuntarEvidencia(ActionEvent event) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar fotos de evidencia");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        List<File> fotos = selector.showOpenMultipleDialog(lblEvidencias.getScene().getWindow());
        if (fotos != null && !fotos.isEmpty()) {
            lblEvidencias.setText(fotos.size() + (fotos.size() == 1 ? " foto adjunta" : " fotos adjuntas"));
        }
    }

    @FXML
    private void guardarAvance(ActionEvent event) {
        Proyecto proyecto = cmbProyectoAvance.getValue();
        Empleado tecnico = cmbTecnico.getValue();
        String tarea = cmbTarea.getEditor().getText() != null ? cmbTarea.getEditor().getText().trim() : "";
        if (proyecto == null || tarea.isEmpty() || tecnico == null) {
            Mensajes.error("Faltan datos", "Selecciona proyecto, tarea realizada y técnico responsable.");
            return;
        }
        int avance = (int) Math.round(sldAvance.getValue());
        String nuevoEstado = chkTerminado.isSelected()
                ? "En revisión"
                : (cmbEstadoAvance.getValue() != null ? cmbEstadoAvance.getValue() : "En proceso");
        try {
            proyecto.setEstado(nuevoEstado);
            proyectoService.actualizar(proyecto);

            tblHistorial.getItems().add(0, new String[]{
                dpFechaAvance.getValue() != null ? dpFechaAvance.getValue().toString() : LocalDate.now().toString(),
                tarea,
                avance + " %",
                tecnico.getNombre(),
                txtObservacionesAvance.getText()
            });

            String mensaje = chkTerminado.isSelected()
                    ? "El proyecto pasó a estado 'En revisión' en MySQL para Administración."
                    : "Se registró el avance del " + avance + " % y se actualizó el estado a '" + nuevoEstado + "' en MySQL.";
            Mensajes.info("Avance guardado", mensaje);
            txtMaterialUsado.clear();
            txtObservacionesAvance.clear();
            chkTerminado.setSelected(false);
            cargarDatos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "GUARDAR_AVANCE", ex);
        }
    }
}
