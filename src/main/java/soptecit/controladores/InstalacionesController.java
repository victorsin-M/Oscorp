package soptecit.controladores;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.modelo.ProyectoEmpleado;
import com.mycompany.soptec.it.modelo.ProyectoMaterial;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.ProyectoEmpleadoService;
import com.mycompany.soptec.it.servicio.ProyectoMaterialService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.ComboBuscadorUtil;
import com.mycompany.soptec.it.util.GestorImagenes;
import com.mycompany.soptec.it.util.Validador;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import soptecit.util.Mensajes;
import soptecit.util.Tablas;

/**
 * Controlador estilizado de Instalaciones conectado a MySQL:
 * Avance de obra + CRUD M:N de proyecto_empleado y proyecto_material + evidencias via GestorImagenes.
 */
public class InstalacionesController implements Initializable {

    private static final String MODULO = "Instalaciones";
    private static final String MOD_PE = "ProyectoEmpleado";
    private static final String MOD_PM = "ProyectoMaterial";
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final ProyectoService proyectoService = new ProyectoService();
    private final EmpleadoService empleadoService = new EmpleadoService();
    private final MaterialService materialService = new MaterialService();
    private final ProyectoEmpleadoService peService = new ProyectoEmpleadoService();
    private final ProyectoMaterialService pmService = new ProyectoMaterialService();

    // 1. Avance de obra
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
    @FXML private TextArea txtObservacionesAvance;
    @FXML private Label lblEvidencias;
    @FXML private CheckBox chkTerminado;

    private List<Proyecto> proyectosCargados = new ArrayList<>();
    private int evidenciasGuardadas = 0;

    // 2. M:N proyecto_empleado
    @FXML private TextField txtBuscarPe;
    @FXML private TableView<ProyectoEmpleado> tblProyEmp;
    @FXML private TableColumn<ProyectoEmpleado, String> colPeProyecto;
    @FXML private TableColumn<ProyectoEmpleado, String> colPeCliente;
    @FXML private TableColumn<ProyectoEmpleado, String> colPeEmpleado;
    @FXML private TableColumn<ProyectoEmpleado, String> colPePuesto;
    @FXML private TableColumn<ProyectoEmpleado, String> colPeEstado;
    @FXML private Label lblModoPe;
    @FXML private TextField txtFiltroProyPe;
    @FXML private ComboBox<Proyecto> cmbProyectoPe;
    @FXML private TextField txtFiltroEmpPe;
    @FXML private ComboBox<Empleado> cmbEmpleadoPe;

    private ProyectoEmpleado peSeleccionado;

    // 3. M:N proyecto_material
    @FXML private TextField txtBuscarPm;
    @FXML private TableView<ProyectoMaterial> tblProyMat;
    @FXML private TableColumn<ProyectoMaterial, String> colPmProyecto;
    @FXML private TableColumn<ProyectoMaterial, String> colPmMaterial;
    @FXML private TableColumn<ProyectoMaterial, String> colPmCantidad;
    @FXML private TableColumn<ProyectoMaterial, String> colPmCosto;
    @FXML private TableColumn<ProyectoMaterial, String> colPmSubtotal;
    @FXML private TableColumn<ProyectoMaterial, String> colPmStock;
    @FXML private Label lblModoPm;
    @FXML private TextField txtFiltroProyPm;
    @FXML private ComboBox<Proyecto> cmbProyectoPm;
    @FXML private TextField txtFiltroMatPm;
    @FXML private ComboBox<Material> cmbMaterialPm;
    @FXML private TextField txtCantidadPm;
    @FXML private CheckBox chkDescontarStock;

    private ProyectoMaterial pmSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarAvance();
        inicializarTabProyectoEmpleado();
        inicializarTabProyectoMaterial();
        cargarDatos(null);
        cargarTablaPe(null);
        cargarTablaPm(null);
    }

    // =========================================================================
    // 1. AVANCE DE OBRA
    // =========================================================================
    private void inicializarAvance() {
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
                    sldAvance.setValue(0);
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
    }

    @FXML
    private void cargarDatos(ActionEvent event) {
        try {
            proyectosCargados = proyectoService.listar(false);
            cmbProyectoAvance.setItems(FXCollections.observableArrayList(proyectosCargados));
            ComboBuscadorUtil.configurar(cmbProyectoPe, txtFiltroProyPe, proyectosCargados, Proyecto::toString);
            ComboBuscadorUtil.configurar(cmbProyectoPm, txtFiltroProyPm, proyectosCargados, Proyecto::toString);

            List<Empleado> tecnicos = empleadoService.listar(false);
            cmbTecnico.setItems(FXCollections.observableArrayList(tecnicos));
            ComboBuscadorUtil.configurar(cmbEmpleadoPe, txtFiltroEmpPe, tecnicos, Empleado::toString);

            List<Material> materiales = materialService.listar(false);
            ComboBuscadorUtil.configurar(cmbMaterialPm, txtFiltroMatPm, materiales, Material::toString);

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
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    lblEvidencias.getScene().getWindow(),
                    "Seleccionar fotografía de evidencia", "evidencias", MODULO);
            if (ruta != null) {
                evidenciasGuardadas++;
                lblEvidencias.setText(evidenciasGuardadas + " guardada(s): " + ruta);
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ADJUNTAR_EVIDENCIA", ex);
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

            // Vincular automaticamente al tecnico en proyecto_empleado si aun no estaba asignado
            try {
                peService.asignar(proyecto.getIdProyecto(), tecnico.getIdEmpleado());
            } catch (ValidacionException yaAsignado) {
                // Si ya estaba asignado al proyecto, continuar normalmente
            }

            tblHistorial.getItems().add(0, new String[]{
                dpFechaAvance.getValue() != null ? dpFechaAvance.getValue().toString() : LocalDate.now().toString(),
                tarea,
                avance + " %",
                tecnico.getNombre(),
                txtObservacionesAvance.getText()
            });

            String mensaje = chkTerminado.isSelected()
                    ? "El proyecto pasó a estado 'En revisión' en MySQL y el técnico quedó vinculado."
                    : "Se registró el avance del " + avance + " % y se actualizó el estado a '" + nuevoEstado + "' en MySQL.";
            Mensajes.info("Avance guardado", mensaje);
            txtObservacionesAvance.clear();
            chkTerminado.setSelected(false);
            cargarDatos(null);
            cargarTablaPe(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "GUARDAR_AVANCE", ex);
        }
    }

    // =========================================================================
    // 2. TÉCNICOS POR PROYECTO (proyecto_empleado M:N)
    // =========================================================================
    private void inicializarTabProyectoEmpleado() {
        colPeProyecto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProyectoDescriptivo()));
        colPeCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colPeEmpleado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreEmpleado()));
        colPePuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPuestoEmpleado()));
        colPeEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoProyecto()));
        colPeEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblProyEmp.setPlaceholder(new Label("No hay empleados asignados a proyectos"));

        tblProyEmp.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                peSeleccionado = nuevo;
                lblModoPe.setText("Editando asignación M:N");
                txtFiltroProyPe.clear();
                txtFiltroEmpPe.clear();
                ComboBuscadorUtil.seleccionarPorPredicado(cmbProyectoPe, p -> p.getIdProyecto() == nuevo.getIdProyecto());
                ComboBuscadorUtil.seleccionarPorPredicado(cmbEmpleadoPe, e -> e.getIdEmpleado() == nuevo.getIdEmpleado());
            }
        });
    }

    @FXML
    private void cargarTablaPe(ActionEvent event) {
        try {
            tblProyEmp.setItems(FXCollections.observableArrayList(peService.listarTodos()));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PE, "LISTAR_PROYECTO_EMPLEADO", ex);
        }
    }

    @FXML
    private void buscarPe(ActionEvent event) {
        try {
            tblProyEmp.setItems(FXCollections.observableArrayList(peService.buscar(txtBuscarPe.getText())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PE, "BUSCAR_PROYECTO_EMPLEADO", ex);
        }
    }

    @FXML
    private void filtrarPePorProyecto(ActionEvent event) {
        Proyecto p = cmbProyectoPe.getValue();
        if (p == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige primero un proyecto en el formulario.");
            return;
        }
        try {
            tblProyEmp.setItems(FXCollections.observableArrayList(peService.listarPorProyecto(p.getIdProyecto())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PE, "LISTAR_POR_PROYECTO", ex);
        }
    }

    @FXML
    private void asignarPe(ActionEvent event) {
        try {
            Proyecto p = cmbProyectoPe.getValue();
            Empleado e = cmbEmpleadoPe.getValue();
            if (p == null || e == null) {
                throw new ValidacionException(MOD_PE, "ASIGNAR_EMPLEADO",
                        "Selecciona tanto el proyecto como el empleado de las listas desplegables.");
            }
            peService.asignar(p.getIdProyecto(), e.getIdEmpleado());
            AlertaUtil.info("Asignación registrada",
                    "Se asignó a " + e.getNombre() + " al proyecto '" + p.getNombreProyecto() + "'.");
            limpiarPe(null);
            cargarTablaPe(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PE, "ASIGNAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void actualizarPe(ActionEvent event) {
        if (peSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona una asignación", "Elige una fila de la tabla para actualizarla.");
            return;
        }
        try {
            Proyecto p = cmbProyectoPe.getValue();
            Empleado e = cmbEmpleadoPe.getValue();
            if (p == null || e == null) {
                throw new ValidacionException(MOD_PE, "ACTUALIZAR_ASIGNACION",
                        "Selecciona el proyecto y el empleado nuevos.");
            }
            peService.actualizarAsignacion(
                    peSeleccionado.getIdProyecto(), peSeleccionado.getIdEmpleado(),
                    p.getIdProyecto(), e.getIdEmpleado());
            AlertaUtil.info("Asignación actualizada", "Se actualizó la relación proyecto-empleado.");
            limpiarPe(null);
            cargarTablaPe(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PE, "ACTUALIZAR_ASIGNACION", ex);
        }
    }

    @FXML
    private void eliminarPe(ActionEvent event) {
        if (peSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona una asignación", "Elige una fila de la tabla para desvincular.");
            return;
        }
        if (!AlertaUtil.confirmar("Desvincular empleado",
                "¿Quitar a " + peSeleccionado.getNombreEmpleado() + " del proyecto '"
                + peSeleccionado.getNombreProyecto() + "'?")) {
            return;
        }
        try {
            peService.eliminar(peSeleccionado.getIdProyecto(), peSeleccionado.getIdEmpleado());
            AlertaUtil.info("Empleado desvinculado", "Se eliminó la asignación en MySQL.");
            limpiarPe(null);
            cargarTablaPe(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PE, "ELIMINAR_ASIGNACION", ex);
        }
    }

    @FXML
    private void limpiarPe(ActionEvent event) {
        peSeleccionado = null;
        lblModoPe.setText("Asignar empleado a proyecto");
        txtFiltroProyPe.clear();
        txtFiltroEmpPe.clear();
        cmbProyectoPe.setValue(null);
        cmbEmpleadoPe.setValue(null);
        tblProyEmp.getSelectionModel().clearSelection();
    }

    // =========================================================================
    // 3. MATERIALES POR PROYECTO (proyecto_material M:N)
    // =========================================================================
    private void inicializarTabProyectoMaterial() {
        colPmProyecto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProyectoDescriptivo()));
        colPmMaterial.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcionMaterial()));
        colPmCantidad.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCantidad().stripTrailingZeros().toPlainString()));
        colPmCosto.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getPrecioCosto())));
        colPmSubtotal.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getSubtotal())));
        colPmStock.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getExistenciasActuales())));
        tblProyMat.setPlaceholder(new Label("No hay materiales asignados a proyectos"));

        tblProyMat.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                pmSeleccionado = nuevo;
                lblModoPm.setText("Editando material del proyecto");
                txtFiltroProyPm.clear();
                txtFiltroMatPm.clear();
                ComboBuscadorUtil.seleccionarPorPredicado(cmbProyectoPm, p -> p.getIdProyecto() == nuevo.getIdProyecto());
                ComboBuscadorUtil.seleccionarPorPredicado(cmbMaterialPm, m -> m.getIdMaterial() == nuevo.getIdMaterial());
                txtCantidadPm.setText(nuevo.getCantidad().stripTrailingZeros().toPlainString());
            }
        });
    }

    @FXML
    private void cargarTablaPm(ActionEvent event) {
        try {
            tblProyMat.setItems(FXCollections.observableArrayList(pmService.listarTodos()));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PM, "LISTAR_PROYECTO_MATERIAL", ex);
        }
    }

    @FXML
    private void buscarPm(ActionEvent event) {
        try {
            tblProyMat.setItems(FXCollections.observableArrayList(pmService.buscar(txtBuscarPm.getText())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PM, "BUSCAR_PROYECTO_MATERIAL", ex);
        }
    }

    @FXML
    private void filtrarPmPorProyecto(ActionEvent event) {
        Proyecto p = cmbProyectoPm.getValue();
        if (p == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige primero un proyecto en el formulario.");
            return;
        }
        try {
            tblProyMat.setItems(FXCollections.observableArrayList(pmService.listarPorProyecto(p.getIdProyecto())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PM, "LISTAR_POR_PROYECTO", ex);
        }
    }

    @FXML
    private void asignarPm(ActionEvent event) {
        try {
            Proyecto p = cmbProyectoPm.getValue();
            Material m = cmbMaterialPm.getValue();
            if (p == null || m == null) {
                throw new ValidacionException(MOD_PM, "ASIGNAR_MATERIAL_PROYECTO",
                        "Selecciona el proyecto y el material de las listas desplegables.");
            }
            BigDecimal cant = Validador.parsearDecimalPositivo(
                    txtCantidadPm.getText(), "Cantidad", MOD_PM, "ASIGNAR_MATERIAL_PROYECTO");
            if (chkDescontarStock.isSelected()) {
                pmService.asignarConDescuentoStock(p.getIdProyecto(), m.getIdMaterial(), cant, false);
            } else {
                pmService.asignarSinDescuento(p.getIdProyecto(), m.getIdMaterial(), cant);
            }
            AlertaUtil.info("Material asignado",
                    "Se asignaron " + cant.stripTrailingZeros().toPlainString() + " unidad(es) de '"
                    + m.getDescripcionMaterial() + "' al proyecto '" + p.getNombreProyecto() + "'.");
            limpiarPm(null);
            cargarDatos(null);
            cargarTablaPm(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PM, "ASIGNAR_MATERIAL_PROYECTO", ex);
        }
    }

    @FXML
    private void actualizarPm(ActionEvent event) {
        if (pmSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un registro", "Elige una fila de la tabla para actualizar su cantidad.");
            return;
        }
        try {
            BigDecimal nuevaCant = Validador.parsearDecimalPositivo(
                    txtCantidadPm.getText(), "Cantidad", MOD_PM, "ACTUALIZAR_CANTIDAD");
            pmService.actualizarCantidad(pmSeleccionado.getIdProyecto(), pmSeleccionado.getIdMaterial(), nuevaCant);
            AlertaUtil.info("Cantidad actualizada", "Se actualizó la cantidad asignada en proyecto_material.");
            limpiarPm(null);
            cargarTablaPm(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PM, "ACTUALIZAR_CANTIDAD", ex);
        }
    }

    @FXML
    private void eliminarPm(ActionEvent event) {
        if (pmSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un registro", "Elige una fila de la tabla para retirarla.");
            return;
        }
        if (!AlertaUtil.confirmar("Retirar material del proyecto",
                "¿Retirar '" + pmSeleccionado.getDescripcionMaterial() + "' de '"
                + pmSeleccionado.getNombreProyecto() + "'"
                + (chkDescontarStock.isSelected() ? " y reintegrar las unidades al inventario?" : "?"))) {
            return;
        }
        try {
            pmService.eliminar(pmSeleccionado.getIdProyecto(), pmSeleccionado.getIdMaterial(),
                    chkDescontarStock.isSelected());
            AlertaUtil.info("Material retirado", "El registro fue eliminado de proyecto_material.");
            limpiarPm(null);
            cargarDatos(null);
            cargarTablaPm(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PM, "ELIMINAR_MATERIAL_PROYECTO", ex);
        }
    }

    @FXML
    private void limpiarPm(ActionEvent event) {
        pmSeleccionado = null;
        lblModoPm.setText("Asignar material a proyecto");
        txtFiltroProyPm.clear();
        txtFiltroMatPm.clear();
        cmbProyectoPm.setValue(null);
        cmbMaterialPm.setValue(null);
        txtCantidadPm.clear();
        tblProyMat.getSelectionModel().clearSelection();
    }
}
