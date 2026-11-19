package soptecit.controladores;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.CitaService;
import com.mycompany.soptec.it.servicio.LevantamientoService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.ComboBuscadorUtil;
import com.mycompany.soptec.it.util.GestorImagenes;
import com.mycompany.soptec.it.util.Validador;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import soptecit.util.Tablas;

/**
 * Controlador estilizado del módulo Levantamientos conectado a MySQL y GestorImagenes.
 */
public class LevantamientosController implements Initializable {

    private static final String MODULO = "Levantamientos";
    private final LevantamientoService service = new LevantamientoService();
    private final ProyectoService proyectoService = new ProyectoService();
    private final CitaService citaService = new CitaService();

    @FXML private CheckBox chkInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtConsultarId;
    @FXML private TableView<Levantamiento> tblLevantamientos;
    @FXML private TableColumn<Levantamiento, String> colId;
    @FXML private TableColumn<Levantamiento, String> colFecha;
    @FXML private TableColumn<Levantamiento, String> colProyecto;
    @FXML private TableColumn<Levantamiento, String> colCita;
    @FXML private TableColumn<Levantamiento, String> colObs;
    @FXML private TableColumn<Levantamiento, String> colActivo;

    @FXML private StackPane panelCroquis;
    @FXML private VBox vacioCroquis;
    @FXML private ImageView imgCroquis;
    @FXML private Label lblRutaCroquis;

    @FXML private Label lblModoFormulario;
    @FXML private TextField txtFiltroProyecto;
    @FXML private ComboBox<Proyecto> cmbProyecto;
    @FXML private ComboBox<Cita> cmbCita;
    @FXML private DatePicker dpFechaVisita;
    @FXML private TextArea txtObservacionesLev;

    private Levantamiento seleccionado;
    private String rutaCroquisActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpFechaVisita.setValue(LocalDate.now());

        panelCroquis.setMinSize(0, 180);
        imgCroquis.fitWidthProperty().bind(panelCroquis.widthProperty().subtract(20));
        imgCroquis.fitHeightProperty().bind(panelCroquis.heightProperty().subtract(20));

        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdLevantamiento())));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaVisita() != null ? d.getValue().getFechaVisita().toString() : ""));
        colProyecto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProyectoDescriptivo()));
        colCita.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaHoraCita() != null && !d.getValue().getFechaHoraCita().isBlank()
                        ? d.getValue().getFechaHoraCita() : "Sin cita"));
        colObs.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getObservacionesTecnicas() != null ? d.getValue().getObservacionesTecnicas() : ""));
        colActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActivoTexto()));
        colActivo.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblLevantamientos.setPlaceholder(new Label("No hay levantamientos registrados"));

        tblLevantamientos.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });

        cargarCombos();
        cargarTabla(null);
    }

    private void cargarCombos() {
        try {
            List<Proyecto> proyectos = proyectoService.listar(false);
            ComboBuscadorUtil.configurar(cmbProyecto, txtFiltroProyecto, proyectos, Proyecto::toString);

            List<Cita> citas = citaService.listar(false);
            ComboBuscadorUtil.configurar(cmbCita, null, citas, Cita::toString);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CARGAR_COMBOS", ex);
        }
    }

    @FXML
    private void cargarTabla(ActionEvent event) {
        try {
            if (event != null) {
                cargarCombos();
            }
            tblLevantamientos.setItems(FXCollections.observableArrayList(
                    service.listar(chkInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_LEVANTAMIENTOS", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblLevantamientos.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_LEVANTAMIENTOS", ex);
        }
    }

    @FXML
    private void consultarPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarId.getText(), "ID de levantamiento", MODULO, "CONSULTAR_POR_ID");
            Levantamiento l = service.consultarPorId(id);
            if (l == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un levantamiento con el ID " + id + ".");
                return;
            }
            cargarEnFormulario(l);
            AlertaUtil.info("Levantamiento encontrado",
                    "Proyecto: " + l.getProyectoDescriptivo()
                    + "\nFecha visita: " + l.getFechaVisita()
                    + "\nCroquis: " + (l.getCroquis() != null ? l.getCroquis() : "Sin croquis")
                    + "\nEstado: " + l.getActivoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void cargarCroquis(ActionEvent event) {
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    panelCroquis.getScene().getWindow(), "Seleccionar imagen de croquis", "croquis", MODULO);
            if (ruta != null) {
                mostrarCroquis(ruta);
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "SELECCIONAR_CROQUIS", ex);
        }
    }

    @FXML
    private void quitarCroquis(ActionEvent event) {
        rutaCroquisActual = null;
        lblRutaCroquis.setText("Sin croquis seleccionado");
        imgCroquis.setImage(null);
        vacioCroquis.setVisible(true);
    }

    private void mostrarCroquis(String ruta) {
        rutaCroquisActual = ruta;
        lblRutaCroquis.setText(ruta);
        Image img = GestorImagenes.cargarImagen(ruta);
        imgCroquis.setImage(img);
        vacioCroquis.setVisible(img == null);
    }

    @FXML
    private void guardarLevantamiento(ActionEvent event) {
        try {
            Levantamiento creado = service.crear(construirDesdeFormulario(new Levantamiento(), "CREAR_LEVANTAMIENTO"));
            AlertaUtil.info("Levantamiento registrado",
                    "Se guardó el levantamiento del proyecto '" + creado.getNombreProyecto() + "'.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_LEVANTAMIENTO", ex);
        }
    }

    @FXML
    private void actualizarLevantamiento(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un levantamiento", "Elige una fila de la tabla para actualizarla.");
            return;
        }
        try {
            Levantamiento editado = construirDesdeFormulario(seleccionado, "ACTUALIZAR_LEVANTAMIENTO");
            service.actualizar(editado);
            AlertaUtil.info("Levantamiento actualizado", "Se actualizaron los datos del levantamiento.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_LEVANTAMIENTO", ex);
        }
    }

    @FXML
    private void desactivarLevantamiento(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un levantamiento", "Elige una fila de la tabla.");
            return;
        }
        try {
            service.desactivar(seleccionado.getIdLevantamiento());
            AlertaUtil.info("Levantamiento desactivado", "El levantamiento quedó inactivo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_LEVANTAMIENTO", ex);
        }
    }

    @FXML
    private void reactivarLevantamiento(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un levantamiento", "Marca 'Mostrar inactivos' y elige un registro.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdLevantamiento());
            AlertaUtil.info("Levantamiento reactivado", "El levantamiento volvió a estar activo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_LEVANTAMIENTO", ex);
        }
    }

    @FXML
    private void eliminarFisicoLevantamiento(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un levantamiento", "Elige una fila de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar levantamiento definitivamente",
                "¿Eliminar permanentemente el levantamiento seleccionado?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdLevantamiento());
            AlertaUtil.info("Levantamiento eliminado", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_LEVANTAMIENTO", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        lblModoFormulario.setText("Registrar levantamiento");
        txtFiltroProyecto.clear();
        cmbProyecto.setValue(null);
        cmbCita.setValue(null);
        dpFechaVisita.setValue(LocalDate.now());
        txtObservacionesLev.clear();
        quitarCroquis(null);
        tblLevantamientos.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(Levantamiento l) {
        seleccionado = l;
        lblModoFormulario.setText("Editando Levantamiento #" + l.getIdLevantamiento());
        txtFiltroProyecto.clear();
        ComboBuscadorUtil.seleccionarPorPredicado(cmbProyecto, p -> p.getIdProyecto() == l.getIdProyecto());
        if (l.getIdCita() != null) {
            ComboBuscadorUtil.seleccionarPorPredicado(cmbCita, c -> c.getIdCita() == l.getIdCita());
        } else {
            cmbCita.setValue(null);
        }
        dpFechaVisita.setValue(l.getFechaVisita());
        txtObservacionesLev.setText(l.getObservacionesTecnicas() == null ? "" : l.getObservacionesTecnicas());
        if (l.getCroquis() != null && !l.getCroquis().isBlank()) {
            mostrarCroquis(l.getCroquis());
        } else {
            quitarCroquis(null);
        }
    }

    private Levantamiento construirDesdeFormulario(Levantamiento base, String op) throws ValidacionException {
        Proyecto proy = cmbProyecto.getValue();
        if (proy == null) {
            throw new ValidacionException(MODULO, op, "Debes seleccionar un proyecto de la lista desplegable.");
        }
        base.setIdProyecto(proy.getIdProyecto());
        base.setNombreProyecto(proy.getNombreProyecto());
        Cita cita = cmbCita.getValue();
        base.setIdCita(cita != null ? cita.getIdCita() : null);
        base.setFechaVisita(dpFechaVisita.getValue());
        base.setObservacionesTecnicas(txtObservacionesLev.getText());
        base.setCroquis(rutaCroquisActual);
        return base;
    }
}
