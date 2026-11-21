package soptecit.controladores;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.ClienteTelefono;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.Pago;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.modelo.ProyectoMaterial;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.ClienteTelefonoService;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.PagoService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.ComboBuscadorUtil;
import com.mycompany.soptec.it.util.GestorImagenes;
import com.mycompany.soptec.it.util.Validador;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import soptecit.util.Mensajes;
import soptecit.util.Tablas;

/**
 * Controlador estilizado del área de Ventas.
 * Integra CRUD real de Clientes, Teléfonos de Cliente, Proyectos (con alta transaccional integral)
 * y Facturación/Pagos sobre la tabla pago en MySQL.
 */
public class VentasController implements Initializable {

    private static final String MOD_CLIENTES = "Clientes";
    private static final String MOD_TELEFONOS = "TelefonosCliente";
    private static final String MOD_PROYECTOS = "Proyectos";
    private static final String MOD_PAGOS = "Pagos";
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteService clienteService = new ClienteService();
    private final ClienteTelefonoService telefonoService = new ClienteTelefonoService();
    private final ProyectoService proyectoService = new ProyectoService();
    private final PagoService pagoService = new PagoService();
    private final MaterialService materialService = new MaterialService();
    private final EmpleadoService empleadoService = new EmpleadoService();

    // ==================== 1. CLIENTES ====================
    @FXML private CheckBox chkInactivosCliente;
    @FXML private TextField txtBuscarCliente;
    @FXML private TextField txtConsultarIdCliente;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colCliId;
    @FXML private TableColumn<Cliente, String> colCliRfc;
    @FXML private TableColumn<Cliente, String> colCliNombre;
    @FXML private TableColumn<Cliente, String> colCliCorreo;
    @FXML private TableColumn<Cliente, String> colCliDireccion;
    @FXML private TableColumn<Cliente, String> colCliEstado;

    @FXML private Label lblModoCliente;
    @FXML private TextField txtRfcCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtCorreoCliente;
    @FXML private TextField txtTelefonoInicial;
    @FXML private TextField txtCalleCliente;
    @FXML private TextField txtNumeroCliente;
    @FXML private TextField txtColoniaCliente;
    @FXML private TextField txtEstadoGeoCliente;
    @FXML private ImageView imgCliente;
    @FXML private Label lblRutaImgCliente;

    private Cliente clienteSeleccionado;
    private String rutaImgCliente;

    // ==================== 2. TELÉFONOS ====================
    @FXML private CheckBox chkInactivosTel;
    @FXML private TextField txtBuscarTel;
    @FXML private TableView<ClienteTelefono> tblTelefonos;
    @FXML private TableColumn<ClienteTelefono, String> colTelCliente;
    @FXML private TableColumn<ClienteTelefono, String> colTelNumero;
    @FXML private TableColumn<ClienteTelefono, String> colTelEstado;
    @FXML private TextField txtFiltroClienteTel;
    @FXML private ComboBox<Cliente> cmbClienteTel;
    @FXML private TextField txtTelefonoCli;

    private ClienteTelefono telefonoSeleccionado;

    // ==================== 3. PROYECTOS ====================
    @FXML private CheckBox chkInactivosProy;
    @FXML private TextField txtBuscarProy;
    @FXML private TextField txtConsultarIdProy;
    @FXML private TableView<Proyecto> tblProyectos;
    @FXML private TableColumn<Proyecto, String> colProyId;
    @FXML private TableColumn<Proyecto, String> colProyNombre;
    @FXML private TableColumn<Proyecto, String> colProyCliente;
    @FXML private TableColumn<Proyecto, String> colProyInicio;
    @FXML private TableColumn<Proyecto, String> colProyFin;
    @FXML private TableColumn<Proyecto, String> colProyCosto;
    @FXML private TableColumn<Proyecto, String> colProyEstado;
    @FXML private TableColumn<Proyecto, String> colProyActivo;

    @FXML private Label lblModoProyecto;
    @FXML private TextField txtFiltroClienteProy;
    @FXML private ComboBox<Cliente> cmbClienteProy;
    @FXML private TextField txtNombreProyecto;
    @FXML private DatePicker dpInicioProy;
    @FXML private DatePicker dpFinProy;
    @FXML private ComboBox<String> cmbEstadoProy;
    @FXML private TextField txtCostoProy;
    @FXML private ComboBox<Material> cmbMaterialProy;
    @FXML private TextField txtCantMatProy;
    @FXML private ComboBox<Empleado> cmbTecnicoProy;
    @FXML private TextField txtAnticipoProy;

    private Proyecto proyectoSeleccionado;

    // ==================== 4. FACTURACIÓN Y PAGOS (BD REAL) ====================
    @FXML private CheckBox chkInactivosFactura;
    @FXML private TableView<Pago> tblFacturas;
    @FXML private TableColumn<Pago, String> colFacFolio;
    @FXML private TableColumn<Pago, String> colFacProyecto;
    @FXML private TableColumn<Pago, String> colFacCliente;
    @FXML private TableColumn<Pago, String> colFacFecha;
    @FXML private TableColumn<Pago, String> colFacTotal;
    @FXML private TableColumn<Pago, String> colFacEstado;
    @FXML private TextField txtBuscarFactura;
    @FXML private ComboBox<String> cmbFiltroFactura;
    @FXML private Label lblResumenFacturas;
    @FXML private Label lblModoFactura;
    @FXML private ComboBox<Proyecto> cmbProyectoFactura;
    @FXML private TextField txtClienteFactura;
    @FXML private TextField txtRfcFactura;
    @FXML private DatePicker dpEmision;
    @FXML private ComboBox<String> cmbEstadoFactura;
    @FXML private TextField txtConcepto;
    @FXML private TextField txtImporte;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    private Pago facturaSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarClientes();
        inicializarTelefonos();
        inicializarProyectos();
        inicializarFacturacion();
        refrescarTodo();
    }

    private void refrescarTodo() {
        cargarTablaClientes(null);
        cargarCombosClientes();
        cargarCombosProyectoIntegral();
        cargarTablaTelefonos(null);
        cargarTablaProyectos(null);
        cargarComboProyectosFactura();
        cargarTablaFacturas(null);
    }

    // =========================================================================
    // 1. CLIENTES
    // =========================================================================
    private void inicializarClientes() {
        colCliId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdCliente())));
        colCliRfc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRfc()));
        colCliNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colCliCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreo()));
        colCliDireccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDireccionResumen()));
        colCliEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));
        colCliEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblClientes.setPlaceholder(new Label("No hay clientes registrados"));

        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarClienteEnFormulario(nuevo);
            }
        });
    }

    @FXML
    private void cargarTablaClientes(ActionEvent event) {
        try {
            tblClientes.setItems(FXCollections.observableArrayList(
                    clienteService.listar(chkInactivosCliente.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "LISTAR_CLIENTES", ex);
        }
    }

    @FXML
    private void buscarClientes(ActionEvent event) {
        try {
            tblClientes.setItems(FXCollections.observableArrayList(
                    clienteService.buscar(txtBuscarCliente.getText(), chkInactivosCliente.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "BUSCAR_CLIENTES", ex);
        }
    }

    @FXML
    private void consultarClientePorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarIdCliente.getText(), "ID de cliente", MOD_CLIENTES, "CONSULTAR_POR_ID");
            Cliente encontrado = clienteService.consultarPorId(id);
            if (encontrado == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un cliente con el ID " + id + ".");
                return;
            }
            cargarClienteEnFormulario(encontrado);
            AlertaUtil.info("Cliente encontrado",
                    "Nombre: " + encontrado.getNombreCliente()
                    + "\nRFC: " + encontrado.getRfc()
                    + "\nCorreo: " + encontrado.getCorreo()
                    + "\nDirección: " + encontrado.getDireccionResumen()
                    + "\nEstado: " + encontrado.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void seleccionarImagenCliente(ActionEvent event) {
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    imgCliente.getScene().getWindow(), "Seleccionar imagen de cliente", "clientes", MOD_CLIENTES);
            if (ruta != null) {
                rutaImgCliente = ruta;
                lblRutaImgCliente.setText(ruta);
                imgCliente.setImage(GestorImagenes.cargarImagen(ruta));
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "SELECCIONAR_IMAGEN", ex);
        }
    }

    @FXML
    private void quitarImagenCliente(ActionEvent event) {
        rutaImgCliente = null;
        lblRutaImgCliente.setText("Sin imagen seleccionada");
        imgCliente.setImage(null);
    }

    @FXML
    private void guardarCliente(ActionEvent event) {
        try {
            Cliente nuevo = construirClienteDesdeFormulario(new Cliente());
            String telInicial = txtTelefonoInicial.getText();
            Cliente creado = (telInicial != null && !telInicial.isBlank())
                    ? clienteService.crearConTelefonoInicial(nuevo, telInicial, false)
                    : clienteService.crear(nuevo);
            AlertaUtil.info("Cliente registrado",
                    "Se guardó correctamente a " + creado.getNombreCliente()
                    + (telInicial != null && !telInicial.isBlank() ? " junto con su teléfono inicial." : "."));
            limpiarCliente(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "CREAR_CLIENTE", ex);
        }
    }

    @FXML
    private void actualizarCliente(ActionEvent event) {
        if (clienteSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige un cliente de la tabla para actualizarlo.");
            return;
        }
        try {
            Cliente editado = construirClienteDesdeFormulario(clienteSeleccionado);
            clienteService.actualizar(editado);
            AlertaUtil.info("Cliente actualizado", "Los datos de " + editado.getNombreCliente() + " fueron actualizados.");
            limpiarCliente(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "ACTUALIZAR_CLIENTE", ex);
        }
    }

    @FXML
    private void desactivarCliente(ActionEvent event) {
        if (clienteSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige un cliente de la tabla para desactivarlo.");
            return;
        }
        if (!AlertaUtil.confirmar("Desactivar cliente",
                "¿Deseas marcar como inactivo a " + clienteSeleccionado.getNombreCliente() + "?")) {
            return;
        }
        try {
            clienteService.desactivar(clienteSeleccionado.getIdCliente());
            AlertaUtil.info("Cliente desactivado", "El registro pasó a estado inactivo.");
            limpiarCliente(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "DESACTIVAR_CLIENTE", ex);
        }
    }

    @FXML
    private void reactivarCliente(ActionEvent event) {
        if (clienteSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Marca 'Mostrar inactivos' y elige un cliente inactivo.");
            return;
        }
        try {
            clienteService.reactivar(clienteSeleccionado.getIdCliente());
            AlertaUtil.info("Cliente reactivado", "El cliente volvió a estar activo.");
            limpiarCliente(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "REACTIVAR_CLIENTE", ex);
        }
    }

    @FXML
    private void eliminarFisicoCliente(ActionEvent event) {
        if (clienteSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige un cliente de la tabla para eliminarlo.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar cliente definitivamente",
                "Esta acción borrará permanentemente a " + clienteSeleccionado.getNombreCliente()
                + " de la base de datos. ¿Continuar?")) {
            return;
        }
        try {
            clienteService.eliminarFisico(clienteSeleccionado.getIdCliente());
            AlertaUtil.info("Cliente eliminado", "El registro fue eliminado de la base de datos.");
            limpiarCliente(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "ELIMINAR_FISICO_CLIENTE", ex);
        }
    }

    @FXML
    private void limpiarCliente(ActionEvent event) {
        clienteSeleccionado = null;
        lblModoCliente.setText("Registrar cliente");
        txtRfcCliente.clear();
        txtNombreCliente.clear();
        txtCorreoCliente.clear();
        txtCalleCliente.clear();
        txtNumeroCliente.clear();
        txtColoniaCliente.clear();
        txtEstadoGeoCliente.clear();
        txtTelefonoInicial.clear();
        txtTelefonoInicial.setDisable(false);
        quitarImagenCliente(null);
        tblClientes.getSelectionModel().clearSelection();
    }

    private void cargarClienteEnFormulario(Cliente c) {
        clienteSeleccionado = c;
        lblModoCliente.setText("Editando Cliente #" + c.getIdCliente());
        txtRfcCliente.setText(c.getRfc());
        txtNombreCliente.setText(c.getNombreCliente());
        txtCorreoCliente.setText(c.getCorreo());
        txtCalleCliente.setText(c.getCalle() == null ? "" : c.getCalle());
        txtNumeroCliente.setText(c.getNumero() == null ? "" : c.getNumero());
        txtColoniaCliente.setText(c.getColonia() == null ? "" : c.getColonia());
        txtEstadoGeoCliente.setText(c.getEstado() == null ? "" : c.getEstado());
        txtTelefonoInicial.clear();
        txtTelefonoInicial.setDisable(true);
        rutaImgCliente = c.getImagen();
        if (rutaImgCliente != null && !rutaImgCliente.isBlank()) {
            lblRutaImgCliente.setText(rutaImgCliente);
            imgCliente.setImage(GestorImagenes.cargarImagen(rutaImgCliente));
        } else {
            quitarImagenCliente(null);
        }
    }

    private Cliente construirClienteDesdeFormulario(Cliente base) {
        base.setRfc(txtRfcCliente.getText());
        base.setNombreCliente(txtNombreCliente.getText());
        base.setCorreo(txtCorreoCliente.getText());
        base.setCalle(txtCalleCliente.getText());
        base.setNumero(txtNumeroCliente.getText());
        base.setColonia(txtColoniaCliente.getText());
        base.setEstado(txtEstadoGeoCliente.getText());
        base.setImagen(rutaImgCliente);
        return base;
    }

    // =========================================================================
    // 2. TELÉFONOS DE CLIENTE
    // =========================================================================
    private void inicializarTelefonos() {
        colTelCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClienteDescriptivo()));
        colTelNumero.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colTelEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));
        colTelEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblTelefonos.setPlaceholder(new Label("No hay teléfonos registrados"));

        tblTelefonos.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                telefonoSeleccionado = nuevo;
                txtFiltroClienteTel.clear();
                ComboBuscadorUtil.seleccionarPorPredicado(cmbClienteTel, c -> c.getIdCliente() == nuevo.getIdCliente());
                txtTelefonoCli.setText(nuevo.getTelefono());
            }
        });
    }

    private void cargarCombosClientes() {
        try {
            List<Cliente> activos = clienteService.listar(false);
            ComboBuscadorUtil.configurar(cmbClienteTel, txtFiltroClienteTel, activos, Cliente::toString);
            ComboBuscadorUtil.configurar(cmbClienteProy, txtFiltroClienteProy, activos, Cliente::toString);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_CLIENTES, "CARGAR_COMBO_CLIENTES", ex);
        }
    }

    @FXML
    private void cargarTablaTelefonos(ActionEvent event) {
        try {
            if (event != null) {
                cargarCombosClientes();
            }
            tblTelefonos.setItems(FXCollections.observableArrayList(
                    telefonoService.listar(chkInactivosTel.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "LISTAR_TELEFONOS", ex);
        }
    }

    @FXML
    private void buscarTelefonos(ActionEvent event) {
        try {
            tblTelefonos.setItems(FXCollections.observableArrayList(
                    telefonoService.buscar(txtBuscarTel.getText(), chkInactivosTel.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "BUSCAR_TELEFONOS", ex);
        }
    }

    @FXML
    private void filtrarTelefonosPorCliente(ActionEvent event) {
        Cliente cli = cmbClienteTel.getValue();
        if (cli == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige primero un cliente en el combo del formulario.");
            return;
        }
        try {
            tblTelefonos.setItems(FXCollections.observableArrayList(
                    telefonoService.listarPorCliente(cli.getIdCliente(), chkInactivosTel.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "LISTAR_POR_CLIENTE", ex);
        }
    }

    @FXML
    private void consultarTelefonoSeleccionado(ActionEvent event) {
        if (telefonoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un registro", "Elige un teléfono de la tabla para consultarlo.");
            return;
        }
        try {
            ClienteTelefono ct = telefonoService.consultarPorClave(
                    telefonoSeleccionado.getIdCliente(), telefonoSeleccionado.getTelefono());
            if (ct == null) {
                AlertaUtil.advertencia("No encontrado", "El registro ya no existe.");
                return;
            }
            AlertaUtil.info("Detalle de teléfono",
                    "Cliente: " + ct.getClienteDescriptivo()
                    + "\nTeléfono: " + ct.getTelefono()
                    + "\nEstado: " + ct.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "CONSULTAR_TELEFONO", ex);
        }
    }

    @FXML
    private void guardarTelefono(ActionEvent event) {
        try {
            Cliente cli = cmbClienteTel.getValue();
            if (cli == null) {
                AlertaUtil.advertencia("Cliente requerido", "Selecciona un cliente de la lista desplegable.");
                return;
            }
            telefonoService.crear(new ClienteTelefono(cli.getIdCliente(), txtTelefonoCli.getText(), true));
            AlertaUtil.info("Teléfono registrado", "Se asoció el teléfono a " + cli.getNombreCliente() + ".");
            limpiarTelefono(null);
            cargarTablaTelefonos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "CREAR_TELEFONO", ex);
        }
    }

    @FXML
    private void actualizarTelefono(ActionEvent event) {
        if (telefonoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Elige una fila de la tabla para actualizarla.");
            return;
        }
        try {
            Cliente cli = cmbClienteTel.getValue();
            if (cli == null) {
                AlertaUtil.advertencia("Cliente requerido", "Selecciona un cliente de la lista desplegable.");
                return;
            }
            ClienteTelefono actualizado = new ClienteTelefono(
                    cli.getIdCliente(), txtTelefonoCli.getText(), telefonoSeleccionado.isActivo());
            telefonoService.actualizar(
                    telefonoSeleccionado.getIdCliente(), telefonoSeleccionado.getTelefono(), actualizado);
            AlertaUtil.info("Teléfono actualizado", "El teléfono se actualizó correctamente.");
            limpiarTelefono(null);
            cargarTablaTelefonos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "ACTUALIZAR_TELEFONO", ex);
        }
    }

    @FXML
    private void desactivarTelefono(ActionEvent event) {
        if (telefonoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Elige una fila de la tabla.");
            return;
        }
        try {
            telefonoService.desactivar(telefonoSeleccionado.getIdCliente(), telefonoSeleccionado.getTelefono());
            AlertaUtil.info("Teléfono desactivado", "El teléfono quedó inactivo.");
            limpiarTelefono(null);
            cargarTablaTelefonos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "DESACTIVAR_TELEFONO", ex);
        }
    }

    @FXML
    private void reactivarTelefono(ActionEvent event) {
        if (telefonoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Marca 'Mostrar inactivos' y selecciona un registro.");
            return;
        }
        try {
            telefonoService.reactivar(telefonoSeleccionado.getIdCliente(), telefonoSeleccionado.getTelefono());
            AlertaUtil.info("Teléfono reactivado", "El teléfono volvió a estar activo.");
            limpiarTelefono(null);
            cargarTablaTelefonos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "REACTIVAR_TELEFONO", ex);
        }
    }

    @FXML
    private void eliminarFisicoTelefono(ActionEvent event) {
        if (telefonoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Elige una fila de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar teléfono definitivamente",
                "¿Eliminar permanentemente el teléfono " + telefonoSeleccionado.getTelefono() + "?")) {
            return;
        }
        try {
            telefonoService.eliminarFisico(telefonoSeleccionado.getIdCliente(), telefonoSeleccionado.getTelefono());
            AlertaUtil.info("Teléfono eliminado", "El registro fue eliminado de la base de datos.");
            limpiarTelefono(null);
            cargarTablaTelefonos(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_TELEFONOS, "ELIMINAR_FISICO_TELEFONO", ex);
        }
    }

    @FXML
    private void limpiarTelefono(ActionEvent event) {
        telefonoSeleccionado = null;
        txtFiltroClienteTel.clear();
        cmbClienteTel.setValue(null);
        txtTelefonoCli.clear();
        tblTelefonos.getSelectionModel().clearSelection();
    }

    // =========================================================================
    // 3. PROYECTOS Y PROPUESTAS
    // =========================================================================
    private void inicializarProyectos() {
        cmbEstadoProy.getItems().addAll("Planeado", "En proceso", "En revisión", "Finalizado", "Cancelado");
        cmbEstadoProy.setValue("Planeado");
        dpInicioProy.setValue(LocalDate.now());

        colProyId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdProyecto())));
        colProyNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProyecto()));
        colProyCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClienteDescriptivo()));
        colProyInicio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaInicio() != null ? d.getValue().getFechaInicio().toString() : ""));
        colProyFin.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaEstimadaFin() != null ? d.getValue().getFechaEstimadaFin().toString() : "—"));
        colProyCosto.setCellValueFactory(d -> new SimpleStringProperty(
                MONEDA.format(d.getValue().getCostoTotal())));
        colProyEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colProyEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        colProyActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActivoTexto()));
        colProyActivo.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblProyectos.setPlaceholder(new Label("No hay proyectos registrados"));

        tblProyectos.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarProyectoEnFormulario(nuevo);
            }
        });
    }

    @FXML
    private void cargarTablaProyectos(ActionEvent event) {
        try {
            if (event != null) {
                cargarCombosClientes();
            }
            tblProyectos.setItems(FXCollections.observableArrayList(
                    proyectoService.listar(chkInactivosProy.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "LISTAR_PROYECTOS", ex);
        }
    }

    @FXML
    private void buscarProyectos(ActionEvent event) {
        try {
            tblProyectos.setItems(FXCollections.observableArrayList(
                    proyectoService.buscar(txtBuscarProy.getText(), chkInactivosProy.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "BUSCAR_PROYECTOS", ex);
        }
    }

    @FXML
    private void consultarProyectoPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarIdProy.getText(), "ID de proyecto", MOD_PROYECTOS, "CONSULTAR_POR_ID");
            Proyecto p = proyectoService.consultarPorId(id);
            if (p == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un proyecto con el ID " + id + ".");
                return;
            }
            cargarProyectoEnFormulario(p);
            AlertaUtil.info("Proyecto encontrado",
                    "Proyecto: " + p.getNombreProyecto()
                    + "\nCliente: " + p.getClienteDescriptivo()
                    + "\nInicio: " + p.getFechaInicio()
                    + "\nCosto: " + MONEDA.format(p.getCostoTotal())
                    + "\nEstado: " + p.getEstado()
                    + "\nRegistro: " + p.getActivoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "CONSULTAR_POR_ID", ex);
        }
    }

    private void cargarCombosProyectoIntegral() {
        try {
            cmbMaterialProy.setItems(FXCollections.observableArrayList(materialService.listar(false)));
            cmbTecnicoProy.setItems(FXCollections.observableArrayList(empleadoService.listar(false)));
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MOD_PROYECTOS, "VentasController", "CARGAR_COMBOS_INTEGRALES", ex);
        }
    }

    @FXML
    private void guardarProyecto(ActionEvent event) {
        try {
            Proyecto datos = construirProyectoDesdeFormulario(new Proyecto(), "CREAR_PROYECTO");
            Material matIni = cmbMaterialProy.getValue();
            Empleado tecIni = cmbTecnicoProy.getValue();
            String anticipoTxt = txtAnticipoProy.getText();
            boolean esIntegral = matIni != null || tecIni != null || (anticipoTxt != null && !anticipoTxt.isBlank());

            Proyecto creado;
            if (esIntegral) {
                List<ProyectoMaterial> listaMat = new ArrayList<>();
                if (matIni != null) {
                    String cantTxt = (txtCantMatProy.getText() == null || txtCantMatProy.getText().isBlank())
                            ? "1" : txtCantMatProy.getText();
                    BigDecimal cant = Validador.parsearDecimalPositivo(
                            cantTxt, "Cantidad de material inicial", MOD_PROYECTOS, "CREAR_PROYECTO_INTEGRAL");
                    listaMat.add(new ProyectoMaterial(0, matIni.getIdMaterial(), cant));
                }
                List<Integer> listaEmp = new ArrayList<>();
                if (tecIni != null) {
                    listaEmp.add(tecIni.getIdEmpleado());
                }
                Pago pagoInicial = null;
                if (anticipoTxt != null && !anticipoTxt.isBlank()) {
                    BigDecimal montoAnticipo = Validador.parsearDecimalPositivo(
                            anticipoTxt, "Anticipo inicial", MOD_PROYECTOS, "CREAR_PROYECTO_INTEGRAL");
                    pagoInicial = new Pago(0, 0, "Anticipo inicial - " + datos.getNombreProyecto(),
                            montoAnticipo, LocalDate.now(), "Parcial", "Transferencia", true, null, null, null);
                }
                creado = proyectoService.crearProyectoIntegral(datos, listaMat, listaEmp, pagoInicial, false);
                AlertaUtil.info("Proyecto integral registrado",
                        "Se registró '" + creado.getNombreProyecto()
                        + "' junto con sus datos relacionados de forma transaccional.");
            } else {
                creado = proyectoService.crear(datos);
                AlertaUtil.info("Proyecto registrado",
                        "Se guardó '" + creado.getNombreProyecto() + "' para " + creado.getClienteDescriptivo() + ".");
            }
            limpiarProyecto(null);
            cargarTablaProyectos(null);
            cargarComboProyectosFactura();
            cargarCombosProyectoIntegral();
            cargarTablaFacturas(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "CREAR_PROYECTO", ex);
        }
    }

    @FXML
    private void actualizarProyecto(ActionEvent event) {
        if (proyectoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla para actualizarlo.");
            return;
        }
        try {
            Proyecto editado = construirProyectoDesdeFormulario(proyectoSeleccionado, "ACTUALIZAR_PROYECTO");
            proyectoService.actualizar(editado);
            AlertaUtil.info("Proyecto actualizado", "Se actualizó '" + editado.getNombreProyecto() + "'.");
            limpiarProyecto(null);
            cargarTablaProyectos(null);
            cargarComboProyectosFactura();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "ACTUALIZAR_PROYECTO", ex);
        }
    }

    @FXML
    private void desactivarProyecto(ActionEvent event) {
        if (proyectoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla.");
            return;
        }
        try {
            proyectoService.desactivar(proyectoSeleccionado.getIdProyecto());
            AlertaUtil.info("Proyecto desactivado", "El proyecto quedó inactivo.");
            limpiarProyecto(null);
            cargarTablaProyectos(null);
            cargarComboProyectosFactura();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "DESACTIVAR_PROYECTO", ex);
        }
    }

    @FXML
    private void reactivarProyecto(ActionEvent event) {
        if (proyectoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Marca 'Mostrar inactivos' y elige un proyecto.");
            return;
        }
        try {
            proyectoService.reactivar(proyectoSeleccionado.getIdProyecto());
            AlertaUtil.info("Proyecto reactivado", "El proyecto volvió a estar activo.");
            limpiarProyecto(null);
            cargarTablaProyectos(null);
            cargarComboProyectosFactura();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "REACTIVAR_PROYECTO", ex);
        }
    }

    @FXML
    private void eliminarFisicoProyecto(ActionEvent event) {
        if (proyectoSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar proyecto definitivamente",
                "¿Eliminar permanentemente el proyecto '" + proyectoSeleccionado.getNombreProyecto() + "'?")) {
            return;
        }
        try {
            proyectoService.eliminarFisico(proyectoSeleccionado.getIdProyecto());
            AlertaUtil.info("Proyecto eliminado", "El registro fue eliminado de la base de datos.");
            limpiarProyecto(null);
            cargarTablaProyectos(null);
            cargarComboProyectosFactura();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROYECTOS, "ELIMINAR_FISICO_PROYECTO", ex);
        }
    }

    @FXML
    private void limpiarProyecto(ActionEvent event) {
        proyectoSeleccionado = null;
        lblModoProyecto.setText("Registrar proyecto");
        txtFiltroClienteProy.clear();
        cmbClienteProy.setValue(null);
        txtNombreProyecto.clear();
        dpInicioProy.setValue(LocalDate.now());
        dpFinProy.setValue(null);
        cmbEstadoProy.setValue("Planeado");
        txtCostoProy.clear();
        cmbMaterialProy.setValue(null);
        txtCantMatProy.clear();
        cmbTecnicoProy.setValue(null);
        txtAnticipoProy.clear();
        tblProyectos.getSelectionModel().clearSelection();
    }

    private void cargarProyectoEnFormulario(Proyecto p) {
        proyectoSeleccionado = p;
        lblModoProyecto.setText("Editando Proyecto #" + p.getIdProyecto());
        txtFiltroClienteProy.clear();
        ComboBuscadorUtil.seleccionarPorPredicado(cmbClienteProy, c -> c.getIdCliente() == p.getIdCliente());
        txtNombreProyecto.setText(p.getNombreProyecto());
        dpInicioProy.setValue(p.getFechaInicio());
        dpFinProy.setValue(p.getFechaEstimadaFin());
        cmbEstadoProy.setValue(p.getEstado());
        txtCostoProy.setText(p.getCostoTotal().toPlainString());
    }

    private Proyecto construirProyectoDesdeFormulario(Proyecto base, String op) throws Exception {
        Cliente cli = cmbClienteProy.getValue();
        if (cli == null) {
            throw new ValidacionException(MOD_PROYECTOS, op, "Selecciona un cliente de la lista desplegable.");
        }
        base.asociarCliente(cli);
        base.setNombreProyecto(txtNombreProyecto.getText());
        base.setFechaInicio(dpInicioProy.getValue());
        base.setFechaEstimadaFin(dpFinProy.getValue());
        base.setEstado(cmbEstadoProy.getValue());
        base.setCostoTotal(Validador.parsearDecimalNoNegativo(txtCostoProy.getText(), "Costo total", MOD_PROYECTOS, op));
        return base;
    }

    // =========================================================================
    // 4. FACTURACIÓN Y PAGOS (PERSISTENCIA REAL SOBRE TABLA pago EN MYSQL)
    // =========================================================================
    private void inicializarFacturacion() {
        dpEmision.setValue(LocalDate.now());
        cmbFiltroFactura.setValue("Todas");
        cmbEstadoFactura.setValue("Pendiente");
        cmbMetodoPago.setValue("Transferencia");

        colFacFolio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFolio()));
        colFacProyecto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProyectoDescriptivo()));
        colFacCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colFacFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaPago() != null ? d.getValue().getFechaPago().format(FECHA) : ""));
        colFacTotal.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getMonto())));
        colFacEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoCuenta()));
        colFacEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblFacturas.setPlaceholder(new Label("No hay facturas / pagos registrados en MySQL"));

        cmbProyectoFactura.valueProperty().addListener((o, a, p) -> {
            if (p != null) {
                txtClienteFactura.setText(p.getClienteNombre() != null ? p.getClienteNombre() : "");
                txtRfcFactura.setText(p.getClienteRfc() != null ? p.getClienteRfc() : "");
                if (facturaSeleccionada == null) {
                    txtConcepto.setText(p.getNombreProyecto());
                    if (p.getCostoTotal() != null) {
                        txtImporte.setText(p.getCostoTotal().toPlainString());
                    }
                }
            } else {
                txtClienteFactura.clear();
                txtRfcFactura.clear();
            }
        });
        txtImporte.textProperty().addListener((o, a, n) -> calcularTotales());
        cmbFiltroFactura.valueProperty().addListener((o, a, n) -> buscarFacturas(null));

        tblFacturas.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarFacturaEnFormulario(nuevo);
            }
        });

        tblFacturas.setRowFactory(t -> {
            TableRow<Pago> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    DetalleFacturaController.abrir(aArregloFactura(fila.getItem()), tblFacturas.getScene().getWindow());
                }
            });
            return fila;
        });
    }

    private void cargarComboProyectosFactura() {
        try {
            List<Proyecto> activos = proyectoService.listar(false);
            cmbProyectoFactura.setItems(FXCollections.observableArrayList(activos));
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MOD_PAGOS, "VentasController", "CARGAR_COMBO_PROYECTOS_FACTURA", ex);
        }
    }

    @FXML
    private void cargarTablaFacturas(ActionEvent event) {
        try {
            List<Pago> lista = aplicarFiltroEstadoFactura(pagoService.listar(chkInactivosFactura.isSelected()));
            mostrarFacturas(lista);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAGOS, "LISTAR_PAGOS", ex);
        }
    }

    @FXML
    private void buscarFacturas(ActionEvent event) {
        try {
            String criterio = txtBuscarFactura.getText();
            List<Pago> base = (criterio == null || criterio.isBlank())
                    ? pagoService.listar(chkInactivosFactura.isSelected())
                    : pagoService.buscar(criterio, chkInactivosFactura.isSelected());
            mostrarFacturas(aplicarFiltroEstadoFactura(base));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAGOS, "BUSCAR_PAGOS", ex);
        }
    }

    private List<Pago> aplicarFiltroEstadoFactura(List<Pago> lista) {
        String estado = cmbFiltroFactura.getValue();
        if (estado == null || "Todas".equals(estado)) {
            return lista;
        }
        return lista.stream()
                .filter(p -> estado.equalsIgnoreCase(p.getEstadoCuenta()))
                .collect(Collectors.toList());
    }

    private void mostrarFacturas(List<Pago> lista) {
        tblFacturas.setItems(FXCollections.observableArrayList(lista));
        BigDecimal total = lista.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        int n = lista.size();
        lblResumenFacturas.setText(n + (n == 1 ? " registro" : " registros") + "  ·  Total " + MONEDA.format(total));
    }

    private static double numero(String texto) {
        if (texto == null) {
            return 0;
        }
        try {
            return Double.parseDouble(texto.replace("$", "").replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void calcularTotales() {
        double subtotal = numero(txtImporte.getText());
        double iva = subtotal * 0.16;
        lblSubtotal.setText(MONEDA.format(subtotal));
        lblIva.setText(MONEDA.format(iva));
        lblTotal.setText(MONEDA.format(subtotal + iva));
    }

    private String[] aArregloFactura(Pago p) {
        BigDecimal total = p.getMonto();
        BigDecimal subtotal = total.divide(new BigDecimal("1.16"), 2, RoundingMode.HALF_UP);
        BigDecimal iva = total.subtract(subtotal);
        LocalDate fecha = p.getFechaPago() != null ? p.getFechaPago() : LocalDate.now();
        return new String[]{
            p.getFolio(),
            "PRY-" + String.format("%04d", p.getIdProyecto()),
            p.getNombreCliente() != null ? p.getNombreCliente() : "",
            fecha.format(FECHA),
            MONEDA.format(total),
            p.getEstadoCuenta() != null ? p.getEstadoCuenta() : "Pendiente",
            p.getRfcCliente() != null ? p.getRfcCliente() : "",
            fecha.plusDays(30).format(FECHA),
            p.getConcepto() != null ? p.getConcepto() : p.getNombreProyecto(),
            MONEDA.format(subtotal),
            MONEDA.format(iva),
            p.getMetodoPago() != null ? p.getMetodoPago() : "Transferencia"
        };
    }

    @FXML
    private void verFactura(ActionEvent event) {
        Pago pago = tblFacturas.getSelectionModel().getSelectedItem();
        if (pago == null) {
            Mensajes.info("Selecciona una factura", "Elige un registro del historial para verlo.");
            return;
        }
        DetalleFacturaController.abrir(aArregloFactura(pago), tblFacturas.getScene().getWindow());
    }

    @FXML
    private void emitirFactura(ActionEvent event) {
        try {
            Pago nuevo = construirFacturaDesdeFormulario(new Pago(), "CREAR_PAGO");
            Pago creado = pagoService.crear(nuevo);
            cargarTablaFacturas(null);
            limpiarFactura(null);
            if (Mensajes.confirmar("Factura " + creado.getFolio() + " guardada en MySQL",
                    "Total: " + MONEDA.format(creado.getMonto()) + ". Se registró en la tabla pago.\n¿Deseas ver el comprobante ahora?")) {
                DetalleFacturaController.abrir(aArregloFactura(creado), tblFacturas.getScene().getWindow());
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAGOS, "CREAR_PAGO", ex);
        }
    }

    @FXML
    private void actualizarFactura(ActionEvent event) {
        if (facturaSeleccionada == null) {
            AlertaUtil.advertencia("Selecciona una factura", "Elige una fila de la tabla para actualizarla.");
            return;
        }
        try {
            Pago editado = construirFacturaDesdeFormulario(facturaSeleccionada, "ACTUALIZAR_PAGO");
            pagoService.actualizar(editado);
            AlertaUtil.info("Registro actualizado", "Se actualizó " + editado.getFolio() + " en MySQL.");
            limpiarFactura(null);
            cargarTablaFacturas(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAGOS, "ACTUALIZAR_PAGO", ex);
        }
    }

    @FXML
    private void eliminarFactura(ActionEvent event) {
        Pago pago = tblFacturas.getSelectionModel().getSelectedItem();
        if (pago == null) {
            AlertaUtil.advertencia("Selecciona una factura", "Elige una fila de la tabla para eliminarla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar factura / pago",
                "¿Eliminar permanentemente " + pago.getFolio() + " de la base de datos?")) {
            return;
        }
        try {
            pagoService.eliminarFisico(pago.getIdPago());
            AlertaUtil.info("Registro eliminado", "La factura/pago fue eliminada de MySQL.");
            limpiarFactura(null);
            cargarTablaFacturas(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PAGOS, "ELIMINAR_FISICO_PAGO", ex);
        }
    }

    @FXML
    private void limpiarFactura(ActionEvent event) {
        facturaSeleccionada = null;
        lblModoFactura.setText("Nueva factura / pago");
        cmbProyectoFactura.setValue(null);
        txtClienteFactura.clear();
        txtRfcFactura.clear();
        dpEmision.setValue(LocalDate.now());
        cmbEstadoFactura.setValue("Pendiente");
        txtConcepto.clear();
        txtImporte.clear();
        cmbMetodoPago.setValue("Transferencia");
        tblFacturas.getSelectionModel().clearSelection();
    }

    private void cargarFacturaEnFormulario(Pago p) {
        facturaSeleccionada = p;
        lblModoFactura.setText("Editando " + p.getFolio());
        ComboBuscadorUtil.seleccionarPorPredicado(cmbProyectoFactura, pr -> pr.getIdProyecto() == p.getIdProyecto());
        dpEmision.setValue(p.getFechaPago());
        cmbEstadoFactura.setValue(p.getEstadoCuenta());
        txtConcepto.setText(p.getConcepto() == null ? "" : p.getConcepto());
        BigDecimal subtotal = p.getMonto().divide(new BigDecimal("1.16"), 2, RoundingMode.HALF_UP);
        txtImporte.setText(subtotal.toPlainString());
        cmbMetodoPago.setValue(p.getMetodoPago());
    }

    private Pago construirFacturaDesdeFormulario(Pago base, String op) throws ValidacionException {
        Proyecto proy = cmbProyectoFactura.getValue();
        if (proy == null) {
            throw new ValidacionException(MOD_PAGOS, op, "Selecciona el proyecto correspondiente.");
        }
        BigDecimal subtotal = Validador.parsearDecimalPositivo(txtImporte.getText(), "Importe", MOD_PAGOS, op);
        BigDecimal totalConIva = subtotal.multiply(new BigDecimal("1.16")).setScale(2, RoundingMode.HALF_UP);

        base.setIdProyecto(proy.getIdProyecto());
        base.setFechaPago(dpEmision.getValue() != null ? dpEmision.getValue() : LocalDate.now());
        String concepto = (txtConcepto.getText() == null || txtConcepto.getText().isBlank())
                ? proy.getNombreProyecto() : txtConcepto.getText().trim();
        base.setConcepto(concepto);
        base.setMonto(totalConIva);
        base.setEstadoCuenta(cmbEstadoFactura.getValue() != null ? cmbEstadoFactura.getValue() : "Pendiente");
        base.setMetodoPago(cmbMetodoPago.getValue() != null ? cmbMetodoPago.getValue() : "Transferencia");
        return base;
    }
}
