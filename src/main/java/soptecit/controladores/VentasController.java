package soptecit.controladores;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.ClienteTelefono;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.ClienteTelefonoService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.ComboBuscadorUtil;
import com.mycompany.soptec.it.util.GestorImagenes;
import com.mycompany.soptec.it.util.Validador;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
 * Integra CRUD real de Clientes, Teléfonos de Cliente, Proyectos y Facturación.
 */
public class VentasController implements Initializable {

    private static final String MOD_CLIENTES = "Clientes";
    private static final String MOD_TELEFONOS = "TelefonosCliente";
    private static final String MOD_PROYECTOS = "Proyectos";
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteService clienteService = new ClienteService();
    private final ClienteTelefonoService telefonoService = new ClienteTelefonoService();
    private final ProyectoService proyectoService = new ProyectoService();

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

    private Proyecto proyectoSeleccionado;

    // ==================== 4. FACTURACIÓN ====================
    @FXML private TableView<String[]> tblFacturas;
    @FXML private TextField txtBuscarFactura;
    @FXML private ComboBox<String> cmbFiltroFactura;
    @FXML private Label lblResumenFacturas;
    @FXML private ComboBox<Proyecto> cmbProyectoFactura;
    @FXML private TextField txtClienteFactura;
    @FXML private TextField txtRfcFactura;
    @FXML private DatePicker dpEmision;
    @FXML private DatePicker dpVencimiento;
    @FXML private TextField txtConcepto;
    @FXML private TextField txtImporte;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    private ObservableList<String[]> facturas;
    private FilteredList<String[]> facturasFiltradas;
    private int siguienteFolio = 106;

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
        cargarTablaTelefonos(null);
        cargarTablaProyectos(null);
        cargarComboProyectosFactura();
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

    @FXML
    private void guardarProyecto(ActionEvent event) {
        try {
            Proyecto creado = proyectoService.crear(construirProyectoDesdeFormulario(new Proyecto(), "CREAR_PROYECTO"));
            AlertaUtil.info("Proyecto registrado",
                    "Se guardó '" + creado.getNombreProyecto() + "' para " + creado.getClienteDescriptivo() + ".");
            limpiarProyecto(null);
            cargarTablaProyectos(null);
            cargarComboProyectosFactura();
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
    // 4. FACTURACIÓN
    // =========================================================================
    private void inicializarFacturacion() {
        dpEmision.setValue(LocalDate.now());
        dpVencimiento.setValue(LocalDate.now().plusDays(30));

        cmbProyectoFactura.valueProperty().addListener((o, a, p) -> {
            if (p != null) {
                txtClienteFactura.setText(p.getClienteNombre() != null ? p.getClienteNombre() : "");
                txtRfcFactura.setText(p.getClienteRfc() != null ? p.getClienteRfc() : "");
                txtConcepto.setText(p.getNombreProyecto());
                if (p.getCostoTotal() != null) {
                    txtImporte.setText(p.getCostoTotal().toPlainString());
                }
            } else {
                txtClienteFactura.clear();
                txtRfcFactura.clear();
            }
        });
        txtImporte.textProperty().addListener((o, a, n) -> calcularTotales());

        Tablas.cargar(tblFacturas, new String[][]{
            {"F-0105", "PRY-0001", "Corporativo Avante", "15/10/2026", "$100,224.00", "Parcial",
                "CAV150301AB2", "14/11/2026", "Instalación de red Cat6 - 24 nodos", "$86,400.00", "$13,824.00", "Transferencia"},
            {"F-0104", "PRY-0002", "Oficinas LTJO", "08/10/2026", "$58,000.00", "Pagada",
                "OLT120815KJ3", "07/11/2026", "Red de voz y datos - 16 nodos", "$50,000.00", "$8,000.00", "Transferencia"}
        });

        facturas = tblFacturas.getItems();
        facturasFiltradas = new FilteredList<>(facturas, f -> true);
        tblFacturas.setItems(facturasFiltradas);

        txtBuscarFactura.textProperty().addListener((o, a, n) -> filtrarFacturas());
        cmbFiltroFactura.valueProperty().addListener((o, a, n) -> filtrarFacturas());
        facturasFiltradas.addListener((javafx.collections.ListChangeListener<String[]>) c -> actualizarResumenFacturas());

        tblFacturas.setRowFactory(t -> {
            TableRow<String[]> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    DetalleFacturaController.abrir(fila.getItem(), tblFacturas.getScene().getWindow());
                }
            });
            return fila;
        });
        actualizarResumenFacturas();
    }

    private void cargarComboProyectosFactura() {
        try {
            List<Proyecto> activos = proyectoService.listar(false);
            cmbProyectoFactura.setItems(FXCollections.observableArrayList(activos));
        } catch (Exception ex) {
            // Ya reportado en proyectos
        }
    }

    private void filtrarFacturas() {
        String texto = txtBuscarFactura.getText() == null ? "" : txtBuscarFactura.getText().trim().toLowerCase();
        String estado = cmbFiltroFactura.getValue();
        facturasFiltradas.setPredicate(f -> {
            boolean coincideTexto = texto.isEmpty() || f[0].toLowerCase().contains(texto)
                    || f[1].toLowerCase().contains(texto) || f[2].toLowerCase().contains(texto);
            boolean coincideEstado = estado == null || estado.equals("Todas") || f[5].equals(estado);
            return coincideTexto && coincideEstado;
        });
    }

    private void actualizarResumenFacturas() {
        double total = 0;
        for (String[] f : facturasFiltradas) {
            total += numero(f[4]);
        }
        int n = facturasFiltradas.size();
        lblResumenFacturas.setText(n + (n == 1 ? " factura" : " facturas") + "  ·  Total facturado " + MONEDA.format(total));
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

    @FXML
    private void verFactura(ActionEvent event) {
        String[] factura = tblFacturas.getSelectionModel().getSelectedItem();
        if (factura == null) {
            Mensajes.info("Selecciona una factura", "Elige una factura del historial para verla.");
            return;
        }
        DetalleFacturaController.abrir(factura, tblFacturas.getScene().getWindow());
    }

    @FXML
    private void emitirFactura(ActionEvent event) {
        Proyecto proy = cmbProyectoFactura.getValue();
        if (proy == null || txtImporte.getText().trim().isEmpty()) {
            Mensajes.error("Faltan datos", "Selecciona el proyecto e indica el importe.");
            return;
        }
        double subtotal = numero(txtImporte.getText());
        if (subtotal <= 0) {
            Mensajes.error("Importe no válido", "Escribe el importe con números, por ejemplo 86400.");
            return;
        }
        double iva = subtotal * 0.16;
        String[] factura = {
            String.format("F-%04d", siguienteFolio++),
            "PRY-" + String.format("%04d", proy.getIdProyecto()),
            txtClienteFactura.getText(),
            dpEmision.getValue() == null ? "" : dpEmision.getValue().format(FECHA),
            MONEDA.format(subtotal + iva),
            "Pendiente",
            txtRfcFactura.getText(),
            dpVencimiento.getValue() == null ? "" : dpVencimiento.getValue().format(FECHA),
            txtConcepto.getText().trim().isEmpty() ? proy.getNombreProyecto() : txtConcepto.getText().trim(),
            MONEDA.format(subtotal),
            MONEDA.format(iva),
            cmbMetodoPago.getValue() == null ? "Por definir" : cmbMetodoPago.getValue()
        };

        txtBuscarFactura.clear();
        cmbFiltroFactura.setValue(null);
        facturas.add(0, factura);
        tblFacturas.getSelectionModel().select(factura);
        tblFacturas.scrollTo(0);
        limpiarFactura(null);

        if (Mensajes.confirmar("Factura " + factura[0] + " emitida",
                "Total: " + factura[4] + ". Se guardó en el historial.\n¿Deseas verla ahora?")) {
            DetalleFacturaController.abrir(factura, tblFacturas.getScene().getWindow());
        }
    }

    @FXML
    private void limpiarFactura(ActionEvent event) {
        cmbProyectoFactura.setValue(null);
        txtClienteFactura.clear();
        txtRfcFactura.clear();
        txtConcepto.clear();
        txtImporte.clear();
        cmbMetodoPago.setValue(null);
    }
}
