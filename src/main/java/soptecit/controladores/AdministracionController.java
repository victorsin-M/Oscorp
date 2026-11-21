package soptecit.controladores;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Pago;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.PagoService;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import soptecit.util.Mensajes;
import soptecit.util.Tablas;

/**
 * Controlador estilizado de Administración.
 * Integra CRUD completo de Empleados, Expedientes/Cierre de Proyectos y CRUD real de Pagos en MySQL.
 */
public class AdministracionController implements Initializable {

    private static final String MOD_EMP = "Empleados";
    private static final String MOD_ADM = "Administracion";
    private static final String MOD_PAG = "Pagos";
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final EmpleadoService empleadoService = new EmpleadoService();
    private final ProyectoService proyectoService = new ProyectoService();
    private final PagoService pagoService = new PagoService();

    // ---- 1. Empleados ----
    @FXML private CheckBox chkInactivosEmp;
    @FXML private TextField txtBuscarEmp;
    @FXML private TextField txtConsultarIdEmp;
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, String> colEmpId;
    @FXML private TableColumn<Empleado, String> colEmpNombre;
    @FXML private TableColumn<Empleado, String> colEmpPuesto;
    @FXML private TableColumn<Empleado, String> colEmpTelefono;
    @FXML private TableColumn<Empleado, String> colEmpCorreo;
    @FXML private TableColumn<Empleado, String> colEmpEstado;

    @FXML private Label lblModoEmpleado;
    @FXML private TextField txtNombreEmp;
    @FXML private ComboBox<String> cmbPuestoEmp;
    @FXML private TextField txtTelefonoEmp;
    @FXML private TextField txtCorreoEmp;
    @FXML private ImageView imgEmpleado;
    @FXML private Label lblRutaImgEmp;

    private Empleado empleadoSeleccionado;
    private String rutaImgEmpleado;

    // ---- 2. Revisión de propuestas ----
    @FXML private TableView<Proyecto> tblExpedientes;
    @FXML private TableColumn<Proyecto, String> colExpId;
    @FXML private TableColumn<Proyecto, String> colExpCliente;
    @FXML private TableColumn<Proyecto, String> colExpNombre;
    @FXML private TableColumn<Proyecto, String> colExpCosto;
    @FXML private TableColumn<Proyecto, String> colExpFecha;
    @FXML private TableColumn<Proyecto, String> colExpEstado;
    @FXML private Label lblFolioExp;
    @FXML private Label lblClienteExp;
    @FXML private Label lblServicioExp;
    @FXML private Label lblMontoExp;
    @FXML private TextArea txtComentariosExp;

    // ---- 3. Cuentas por cobrar (CRUD real de pago) ----
    @FXML private Label lblSaldoTotal;
    @FXML private Label lblFacturasPendientes;
    @FXML private Label lblPagosRecibidos;
    @FXML private Label lblPagosVencidos;
    @FXML private CheckBox chkInactivosPago;
    @FXML private TextField txtBuscarPago;
    @FXML private TableView<Pago> tblPagos;
    @FXML private TableColumn<Pago, String> colPagFolio;
    @FXML private TableColumn<Pago, String> colPagProyecto;
    @FXML private TableColumn<Pago, String> colPagCliente;
    @FXML private TableColumn<Pago, String> colPagConcepto;
    @FXML private TableColumn<Pago, String> colPagFecha;
    @FXML private TableColumn<Pago, String> colPagMonto;
    @FXML private TableColumn<Pago, String> colPagEstado;
    @FXML private TableColumn<Pago, String> colPagActivo;

    @FXML private Label lblModoPago;
    @FXML private ComboBox<Proyecto> cmbProyectoPago;
    @FXML private Label lblSaldoFactura;
    @FXML private TextField txtConceptoPago;
    @FXML private DatePicker dpFechaPago;
    @FXML private TextField txtMontoPago;
    @FXML private ComboBox<String> cmbMetodoPagoAdm;
    @FXML private ComboBox<String> cmbEstadoCuentaPago;

    private Pago pagoSeleccionado;

    // ---- 4. Revisión final ----
    @FXML private TableView<Proyecto> tblRevisionFinal;
    @FXML private TableColumn<Proyecto, String> colRevId;
    @FXML private TableColumn<Proyecto, String> colRevProyecto;
    @FXML private TableColumn<Proyecto, String> colRevCliente;
    @FXML private TableColumn<Proyecto, String> colRevFin;
    @FXML private TableColumn<Proyecto, String> colRevEstado;
    @FXML private Label lblProyectoRevision;
    @FXML private CheckBox chkPruebas;
    @FXML private CheckBox chkCertificacion;
    @FXML private CheckBox chkEtiquetado;
    @FXML private CheckBox chkLimpieza;
    @FXML private CheckBox chkSobrante;
    @FXML private CheckBox chkActa;
    @FXML private CheckBox chkPagos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarEmpleados();
        inicializarExpedientesYRevision();
        inicializarPagos();
        cargarTablaEmpleados(null);
        cargarExpedientes(null);
        cargarPagos(null);
    }

    // =========================================================================
    // 1. EMPLEADOS (CRUD COMPLETO)
    // =========================================================================
    private void inicializarEmpleados() {
        cmbPuestoEmp.getItems().addAll("Administrador general", "Administrador", "Agente de Ventas",
                "Técnico de Levantamiento", "Técnico Instalador", "Compras y Almacén");

        colEmpId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdEmpleado())));
        colEmpNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colEmpPuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPuesto()));
        colEmpTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colEmpCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreo()));
        colEmpEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));
        colEmpEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblEmpleados.setPlaceholder(new Label("No hay empleados registrados"));

        tblEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarEmpleadoEnFormulario(nuevo);
            }
        });
    }

    @FXML
    private void cargarTablaEmpleados(ActionEvent event) {
        try {
            tblEmpleados.setItems(FXCollections.observableArrayList(
                    empleadoService.listar(chkInactivosEmp.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "LISTAR_EMPLEADOS", ex);
        }
    }

    @FXML
    private void buscarEmpleados(ActionEvent event) {
        try {
            tblEmpleados.setItems(FXCollections.observableArrayList(
                    empleadoService.buscar(txtBuscarEmp.getText(), chkInactivosEmp.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "BUSCAR_EMPLEADOS", ex);
        }
    }

    @FXML
    private void consultarEmpleadoPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarIdEmp.getText(), "ID de empleado", MOD_EMP, "CONSULTAR_POR_ID");
            Empleado emp = empleadoService.consultarPorId(id);
            if (emp == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un empleado con el ID " + id + ".");
                return;
            }
            cargarEmpleadoEnFormulario(emp);
            AlertaUtil.info("Empleado encontrado",
                    "Nombre: " + emp.getNombre()
                    + "\nPuesto: " + emp.getPuesto()
                    + "\nTeléfono: " + emp.getTelefono()
                    + "\nCorreo: " + emp.getCorreo()
                    + "\nEstado: " + emp.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void seleccionarImagenEmpleado(ActionEvent event) {
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    imgEmpleado.getScene().getWindow(), "Seleccionar fotografía de empleado", "empleados", MOD_EMP);
            if (ruta != null) {
                rutaImgEmpleado = ruta;
                lblRutaImgEmp.setText(ruta);
                imgEmpleado.setImage(GestorImagenes.cargarImagen(ruta));
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "SELECCIONAR_IMAGEN", ex);
        }
    }

    @FXML
    private void quitarImagenEmpleado(ActionEvent event) {
        rutaImgEmpleado = null;
        lblRutaImgEmp.setText("Sin imagen seleccionada");
        imgEmpleado.setImage(null);
    }

    @FXML
    private void guardarEmpleado(ActionEvent event) {
        try {
            Empleado creado = empleadoService.crear(construirEmpleadoDesdeFormulario(new Empleado()));
            AlertaUtil.info("Empleado registrado", "Se guardó a " + creado.getNombre() + ".");
            limpiarEmpleado(null);
            cargarTablaEmpleados(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "CREAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void actualizarEmpleado(ActionEvent event) {
        if (empleadoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Elige un empleado de la tabla para actualizarlo.");
            return;
        }
        try {
            Empleado editado = construirEmpleadoDesdeFormulario(empleadoSeleccionado);
            empleadoService.actualizar(editado);
            AlertaUtil.info("Empleado actualizado", "Se actualizó la información de " + editado.getNombre() + ".");
            limpiarEmpleado(null);
            cargarTablaEmpleados(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "ACTUALIZAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void desactivarEmpleado(ActionEvent event) {
        if (empleadoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Elige un empleado de la tabla.");
            return;
        }
        try {
            empleadoService.desactivar(empleadoSeleccionado.getIdEmpleado());
            AlertaUtil.info("Empleado desactivado", "El empleado quedó inactivo.");
            limpiarEmpleado(null);
            cargarTablaEmpleados(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "DESACTIVAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void reactivarEmpleado(ActionEvent event) {
        if (empleadoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Marca 'Mostrar inactivos' y elige un empleado.");
            return;
        }
        try {
            empleadoService.reactivar(empleadoSeleccionado.getIdEmpleado());
            AlertaUtil.info("Empleado reactivado", "El empleado volvió a estar activo.");
            limpiarEmpleado(null);
            cargarTablaEmpleados(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "REACTIVAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void eliminarFisicoEmpleado(ActionEvent event) {
        if (empleadoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Elige un empleado de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar empleado definitivamente",
                "¿Eliminar permanentemente a " + empleadoSeleccionado.getNombre() + "?")) {
            return;
        }
        try {
            empleadoService.eliminarFisico(empleadoSeleccionado.getIdEmpleado());
            AlertaUtil.info("Empleado eliminado", "El registro fue eliminado de la base de datos.");
            limpiarEmpleado(null);
            cargarTablaEmpleados(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_EMP, "ELIMINAR_FISICO_EMPLEADO", ex);
        }
    }

    @FXML
    private void limpiarEmpleado(ActionEvent event) {
        empleadoSeleccionado = null;
        lblModoEmpleado.setText("Registrar empleado");
        txtNombreEmp.clear();
        txtTelefonoEmp.clear();
        cmbPuestoEmp.setValue(null);
        cmbPuestoEmp.getEditor().clear();
        txtCorreoEmp.clear();
        quitarImagenEmpleado(null);
        tblEmpleados.getSelectionModel().clearSelection();
    }

    private void cargarEmpleadoEnFormulario(Empleado e) {
        empleadoSeleccionado = e;
        lblModoEmpleado.setText("Editando Empleado #" + e.getIdEmpleado());
        txtNombreEmp.setText(e.getNombre());
        txtTelefonoEmp.setText(e.getTelefono());
        cmbPuestoEmp.setValue(e.getPuesto());
        cmbPuestoEmp.getEditor().setText(e.getPuesto());
        txtCorreoEmp.setText(e.getCorreo());
        rutaImgEmpleado = e.getImagen();
        if (rutaImgEmpleado != null && !rutaImgEmpleado.isBlank()) {
            lblRutaImgEmp.setText(rutaImgEmpleado);
            imgEmpleado.setImage(GestorImagenes.cargarImagen(rutaImgEmpleado));
        } else {
            quitarImagenEmpleado(null);
        }
    }

    private Empleado construirEmpleadoDesdeFormulario(Empleado base) {
        base.setNombre(txtNombreEmp.getText());
        base.setTelefono(txtTelefonoEmp.getText());
        String puesto = cmbPuestoEmp.getEditor().getText();
        if ((puesto == null || puesto.isBlank()) && cmbPuestoEmp.getValue() != null) {
            puesto = cmbPuestoEmp.getValue();
        }
        base.setPuesto(puesto);
        base.setCorreo(txtCorreoEmp.getText());
        base.setImagen(rutaImgEmpleado);
        return base;
    }

    // =========================================================================
    // 2 y 4. EXPEDIENTES Y REVISIÓN FINAL
    // =========================================================================
    private void inicializarExpedientesYRevision() {
        colExpId.setCellValueFactory(d -> new SimpleStringProperty("PRY-" + String.format("%04d", d.getValue().getIdProyecto())));
        colExpCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClienteDescriptivo()));
        colExpNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProyecto()));
        colExpCosto.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getCostoTotal())));
        colExpFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaInicio() != null ? d.getValue().getFechaInicio().toString() : ""));
        colExpEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colExpEstado.setCellFactory(c -> Tablas.crearCeldaEstado());

        tblExpedientes.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                lblFolioExp.setText("PRY-" + String.format("%04d", p.getIdProyecto()));
                lblClienteExp.setText(p.getClienteDescriptivo());
                lblServicioExp.setText(p.getNombreProyecto());
                lblMontoExp.setText(MONEDA.format(p.getCostoTotal()) + " + IVA");
            }
        });

        colRevId.setCellValueFactory(d -> new SimpleStringProperty("PRY-" + String.format("%04d", d.getValue().getIdProyecto())));
        colRevProyecto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProyecto()));
        colRevCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClienteDescriptivo()));
        colRevFin.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaEstimadaFin() != null ? d.getValue().getFechaEstimadaFin().toString() : "—"));
        colRevEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colRevEstado.setCellFactory(c -> Tablas.crearCeldaEstado());

        tblRevisionFinal.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                lblProyectoRevision.setText("PRY-" + String.format("%04d", p.getIdProyecto()) + " · " + p.getNombreProyecto());
            }
        });
    }

    @FXML
    private void cargarExpedientes(ActionEvent event) {
        try {
            List<Proyecto> proyectos = proyectoService.listar(false);
            tblExpedientes.setItems(FXCollections.observableArrayList(proyectos));
            tblRevisionFinal.setItems(FXCollections.observableArrayList(proyectos));
            cmbProyectoPago.setItems(FXCollections.observableArrayList(proyectos));
            actualizarKpisCobranza(proyectos);
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MOD_ADM, "AdministracionController", "CARGAR_EXPEDIENTES", ex);
        }
    }

    private void actualizarKpisCobranza(List<Proyecto> proyectos) {
        try {
            BigDecimal cartera = proyectos.stream()
                    .map(p -> p.getCostoTotal() != null ? p.getCostoTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            List<Pago> pagosActivos = pagoService.listar(false);
            BigDecimal cobrado = pagosActivos.stream()
                    .map(Pago::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pendiente = cartera.subtract(cobrado).max(BigDecimal.ZERO);

            lblSaldoTotal.setText(MONEDA.format(cartera));
            lblPagosRecibidos.setText(MONEDA.format(cobrado));
            lblPagosVencidos.setText(MONEDA.format(pendiente));
            lblFacturasPendientes.setText(String.valueOf(pagosActivos.size()));
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MOD_PAG, "AdministracionController", "ACTUALIZAR_KPIS_COBRANZA", ex);
        }
    }

    // =========================================================================
    // 3. CUENTAS POR COBRAR (CRUD REAL SOBRE TABLA pago)
    // =========================================================================
    private void inicializarPagos() {
        dpFechaPago.setValue(LocalDate.now());
        cmbMetodoPagoAdm.setValue("Transferencia");
        cmbEstadoCuentaPago.setValue("Pagado");

        colPagFolio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFolio()));
        colPagProyecto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProyectoDescriptivo()));
        colPagCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colPagConcepto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getConcepto()));
        colPagFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaPago() != null ? d.getValue().getFechaPago().toString() : ""));
        colPagMonto.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getMonto())));
        colPagEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoCuenta()));
        colPagEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        colPagActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActivoTexto()));
        colPagActivo.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblPagos.setPlaceholder(new Label("No hay pagos registrados en la base de datos"));

        cmbProyectoPago.valueProperty().addListener((o, a, p) -> actualizarSaldoProyecto(p));

        tblPagos.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarPagoEnFormulario(nuevo);
            }
        });
    }

    private void actualizarSaldoProyecto(Proyecto p) {
        if (p == null) {
            lblSaldoFactura.setText("$0.00");
            return;
        }
        try {
            BigDecimal pagado = pagoService.sumarPagosActivosPorProyecto(p.getIdProyecto());
            BigDecimal saldo = p.getCostoTotal().subtract(pagado).max(BigDecimal.ZERO);
            lblSaldoFactura.setText(MONEDA.format(saldo));
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MOD_PAG, "AdministracionController", "CALCULAR_SALDO", ex);
            lblSaldoFactura.setText(MONEDA.format(p.getCostoTotal()));
        }
    }

    @FXML
    private void cargarPagos(ActionEvent event) {
        try {
            tblPagos.setItems(FXCollections.observableArrayList(
                    pagoService.listar(chkInactivosPago.isSelected())));
            actualizarKpisCobranza(proyectoService.listar(false));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAG, "LISTAR_PAGOS", ex);
        }
    }

    @FXML
    private void buscarPagos(ActionEvent event) {
        try {
            tblPagos.setItems(FXCollections.observableArrayList(
                    pagoService.buscar(txtBuscarPago.getText(), chkInactivosPago.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAG, "BUSCAR_PAGOS", ex);
        }
    }

    @FXML
    private void registrarPago(ActionEvent event) {
        try {
            Pago nuevo = construirPagoDesdeFormulario(new Pago(), "CREAR_PAGO");
            Pago creado = pagoService.crear(nuevo);
            AlertaUtil.info("Pago registrado",
                    "Se registró el pago " + creado.getFolio() + " por " + MONEDA.format(creado.getMonto())
                    + " para '" + creado.getProyectoDescriptivo() + "'.");
            limpiarPago(null);
            cargarPagos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAG, "CREAR_PAGO", ex);
        }
    }

    @FXML
    private void actualizarPago(ActionEvent event) {
        if (pagoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un pago", "Elige un pago de la tabla para actualizarlo.");
            return;
        }
        try {
            Pago editado = construirPagoDesdeFormulario(pagoSeleccionado, "ACTUALIZAR_PAGO");
            pagoService.actualizar(editado);
            AlertaUtil.info("Pago actualizado", "Se actualizó " + editado.getFolio() + " en MySQL.");
            limpiarPago(null);
            cargarPagos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAG, "ACTUALIZAR_PAGO", ex);
        }
    }

    @FXML
    private void desactivarPago(ActionEvent event) {
        if (pagoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un pago", "Elige un pago de la tabla.");
            return;
        }
        try {
            pagoService.desactivar(pagoSeleccionado.getIdPago());
            AlertaUtil.info("Pago desactivado", "El pago pasó a estado inactivo.");
            limpiarPago(null);
            cargarPagos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAG, "DESACTIVAR_PAGO", ex);
        }
    }

    @FXML
    private void reactivarPago(ActionEvent event) {
        if (pagoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un pago", "Marca 'Mostrar inactivos' y elige un pago.");
            return;
        }
        try {
            pagoService.reactivar(pagoSeleccionado.getIdPago());
            AlertaUtil.info("Pago reactivado", "El pago volvió a estar activo.");
            limpiarPago(null);
            cargarPagos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAG, "REACTIVAR_PAGO", ex);
        }
    }

    @FXML
    private void eliminarFisicoPago(ActionEvent event) {
        if (pagoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un pago", "Elige un pago de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar pago definitivamente",
                "¿Eliminar permanentemente " + pagoSeleccionado.getFolio() + " de la base de datos?")) {
            return;
        }
        try {
            pagoService.eliminarFisico(pagoSeleccionado.getIdPago());
            AlertaUtil.info("Pago eliminado", "El pago fue eliminado de la base de datos.");
            limpiarPago(null);
            cargarPagos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAG, "ELIMINAR_FISICO_PAGO", ex);
        }
    }

    @FXML
    private void limpiarPago(ActionEvent event) {
        pagoSeleccionado = null;
        lblModoPago.setText("Registrar pago");
        cmbProyectoPago.setValue(null);
        lblSaldoFactura.setText("$0.00");
        txtConceptoPago.clear();
        dpFechaPago.setValue(LocalDate.now());
        txtMontoPago.clear();
        cmbMetodoPagoAdm.setValue("Transferencia");
        cmbEstadoCuentaPago.setValue("Pagado");
        tblPagos.getSelectionModel().clearSelection();
    }

    private void cargarPagoEnFormulario(Pago p) {
        pagoSeleccionado = p;
        lblModoPago.setText("Editando " + p.getFolio());
        ComboBuscadorUtil.seleccionarPorPredicado(cmbProyectoPago, pr -> pr.getIdProyecto() == p.getIdProyecto());
        txtConceptoPago.setText(p.getConcepto() == null ? "" : p.getConcepto());
        dpFechaPago.setValue(p.getFechaPago());
        txtMontoPago.setText(p.getMonto().toPlainString());
        cmbMetodoPagoAdm.setValue(p.getMetodoPago());
        cmbEstadoCuentaPago.setValue(p.getEstadoCuenta());
    }

    private Pago construirPagoDesdeFormulario(Pago base, String op) throws ValidacionException {
        Proyecto p = cmbProyectoPago.getValue();
        if (p == null) {
            throw new ValidacionException(MOD_PAG, op, "Selecciona un proyecto de la lista desplegable.");
        }
        base.setIdProyecto(p.getIdProyecto());
        base.setConcepto(txtConceptoPago.getText());
        base.setFechaPago(dpFechaPago.getValue() != null ? dpFechaPago.getValue() : LocalDate.now());
        base.setMonto(Validador.parsearDecimalPositivo(txtMontoPago.getText(), "Monto", MOD_PAG, op));
        base.setMetodoPago(cmbMetodoPagoAdm.getValue());
        base.setEstadoCuenta(cmbEstadoCuentaPago.getValue() != null ? cmbEstadoCuentaPago.getValue() : "Pagado");
        return base;
    }

    @FXML
    private void aprobarPropuesta(ActionEvent event) {
        Proyecto p = tblExpedientes.getSelectionModel().getSelectedItem();
        if (p == null) {
            Mensajes.info("Selecciona un expediente", "Elige el proyecto que deseas aprobar.");
            return;
        }
        try {
            p.setEstado("En proceso");
            proyectoService.actualizar(p);
            Mensajes.info("Proyecto aprobado",
                    "El proyecto '" + p.getNombreProyecto() + "' pasó a estado 'En proceso' en MySQL.");
            cargarExpedientes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ADM, "APROBAR_PROYECTO", ex);
        }
    }

    @FXML
    private void rechazarPropuesta(ActionEvent event) {
        Proyecto p = tblExpedientes.getSelectionModel().getSelectedItem();
        if (p == null) {
            Mensajes.info("Selecciona un expediente", "Elige el proyecto que deseas rechazar.");
            return;
        }
        if (!Mensajes.confirmar("Rechazar proyecto", "¿Marcar como 'Cancelado' el proyecto '" + p.getNombreProyecto() + "'?")) {
            return;
        }
        try {
            p.setEstado("Cancelado");
            proyectoService.actualizar(p);
            Mensajes.info("Proyecto rechazado", "El proyecto cambió a estado 'Cancelado' en MySQL.");
            cargarExpedientes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ADM, "RECHAZAR_PROYECTO", ex);
        }
    }

    @FXML
    private void cerrarProyecto(ActionEvent event) {
        Proyecto p = tblRevisionFinal.getSelectionModel().getSelectedItem();
        if (p == null) {
            Mensajes.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla de revisión final.");
            return;
        }
        CheckBox[] lista = {chkPruebas, chkCertificacion, chkEtiquetado, chkLimpieza, chkSobrante, chkActa, chkPagos};
        for (CheckBox c : lista) {
            if (!c.isSelected()) {
                Mensajes.error("Revisión incompleta", "Falta validar: " + c.getText());
                return;
            }
        }
        try {
            p.setEstado("Finalizado");
            proyectoService.actualizar(p);
            Mensajes.info("Proyecto cerrado", "El proyecto '" + p.getNombreProyecto() + "' se marcó como Finalizado en MySQL.");
            for (CheckBox c : lista) {
                c.setSelected(false);
            }
            cargarExpedientes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ADM, "CERRAR_PROYECTO", ex);
        }
    }

    @FXML
    private void regresarAInstalaciones(ActionEvent event) {
        Proyecto p = tblRevisionFinal.getSelectionModel().getSelectedItem();
        if (p == null) {
            Mensajes.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla.");
            return;
        }
        try {
            p.setEstado("En proceso");
            proyectoService.actualizar(p);
            Mensajes.info("Regresado a Instalaciones", "El proyecto '" + p.getNombreProyecto() + "' volvió a estado 'En proceso'.");
            cargarExpedientes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ADM, "REGRESAR_INSTALACIONES", ex);
        }
    }
}
