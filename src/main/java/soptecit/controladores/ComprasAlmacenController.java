package soptecit.controladores;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.MaterialProveedor;
import com.mycompany.soptec.it.modelo.OrdenCompra;
import com.mycompany.soptec.it.modelo.OrdenCompraMaterial;
import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.MaterialProveedorService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.OrdenCompraService;
import com.mycompany.soptec.it.servicio.ProveedorService;
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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import soptecit.util.Mensajes;
import soptecit.util.Tablas;

/**
 * Controlador estilizado de Compras-Almacen.
 * Integra CRUD completo en MySQL de Materiales, Proveedores, Movimientos (con proyecto_material),
 * Ordenes de Compra + Partidas (orden_compra + orden_compra_material M:N) y Materiales <-> Proveedores (M:N).
 */
public class ComprasAlmacenController implements Initializable {

    private static final String MOD_MAT = "Materiales";
    private static final String MOD_PROV = "Proveedores";
    private static final String MOD_ORD = "OrdenesCompra";
    private static final String MOD_MP = "MaterialProveedor";
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final MaterialService materialService = new MaterialService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final ProyectoService proyectoService = new ProyectoService();
    private final EmpleadoService empleadoService = new EmpleadoService();
    private final OrdenCompraService ordenCompraService = new OrdenCompraService();
    private final MaterialProveedorService matProvService = new MaterialProveedorService();
    private final ProyectoMaterialService proyectoMaterialService = new ProyectoMaterialService();

    // KPIs
    @FXML private Label lblTotalMateriales;
    @FXML private Label lblBajoStockAlm;
    @FXML private Label lblTotalProveedores;
    @FXML private Label lblValorInventario;

    // 1. Materiales
    @FXML private CheckBox chkInactivosMat;
    @FXML private TextField txtBuscarMaterial;
    @FXML private TextField txtConsultarIdMat;
    @FXML private TableView<Material> tblMateriales;
    @FXML private TableColumn<Material, String> colMatId;
    @FXML private TableColumn<Material, String> colMatDesc;
    @FXML private TableColumn<Material, String> colMatPrecio;
    @FXML private TableColumn<Material, String> colMatExistencias;
    @FXML private TableColumn<Material, String> colMatStock;
    @FXML private TableColumn<Material, String> colMatEstado;

    @FXML private Label lblModoMaterial;
    @FXML private TextField txtDescMaterial;
    @FXML private TextField txtPrecioMaterial;
    @FXML private TextField txtExistenciasMaterial;

    private Material materialSeleccionado;

    // 2. Proveedores
    @FXML private CheckBox chkInactivosProv;
    @FXML private TextField txtBuscarProv;
    @FXML private TextField txtConsultarIdProv;
    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, String> colProvId;
    @FXML private TableColumn<Proveedor, String> colProvNombre;
    @FXML private TableColumn<Proveedor, String> colProvTelefono;
    @FXML private TableColumn<Proveedor, String> colProvCorreo;
    @FXML private TableColumn<Proveedor, String> colProvEstado;

    @FXML private Label lblModoProveedor;
    @FXML private TextField txtNombreProveedor;
    @FXML private TextField txtTelefonoProveedor;
    @FXML private TextField txtCorreoProveedor;
    @FXML private ImageView imgProveedor;
    @FXML private Label lblRutaImgProv;

    private Proveedor proveedorSeleccionado;
    private String rutaImgProveedor;

    // 3. Movimientos
    @FXML private RadioButton rbEntrada;
    @FXML private RadioButton rbSalida;
    @FXML private ComboBox<Material> cmbMaterialMov;
    @FXML private Spinner<Integer> spnCantidadMov;
    @FXML private DatePicker dpFechaMov;
    @FXML private ComboBox<Proyecto> cmbProyectoMov;
    @FXML private ComboBox<Empleado> cmbResponsableMov;
    @FXML private TextArea txtObservacionesMov;
    @FXML private TableView<String[]> tblMovimientos;

    // 4. Ordenes de compra (M:N real)
    @FXML private CheckBox chkInactivosOrden;
    @FXML private TextField txtBuscarOrden;
    @FXML private TableView<OrdenCompra> tblOrdenes;
    @FXML private TableColumn<OrdenCompra, String> colOrdFolio;
    @FXML private TableColumn<OrdenCompra, String> colOrdProveedor;
    @FXML private TableColumn<OrdenCompra, String> colOrdProyecto;
    @FXML private TableColumn<OrdenCompra, String> colOrdFecha;
    @FXML private TableColumn<OrdenCompra, String> colOrdEntrega;
    @FXML private TableColumn<OrdenCompra, String> colOrdTotal;
    @FXML private TableColumn<OrdenCompra, String> colOrdEstado;
    @FXML private TableColumn<OrdenCompra, String> colOrdActivo;

    @FXML private Label lblModoOrden;
    @FXML private ComboBox<Proveedor> cmbProveedorOrden;
    @FXML private ComboBox<Proyecto> cmbProyectoOrden;
    @FXML private DatePicker dpFechaOrden;
    @FXML private TextField txtTiempoEntregaOrden;
    @FXML private ComboBox<Material> cmbMaterialOrden;
    @FXML private TextField txtCantidadOrden;
    @FXML private TableView<OrdenCompraMaterial> tblPartidas;
    @FXML private TableColumn<OrdenCompraMaterial, String> colPartMat;
    @FXML private TableColumn<OrdenCompraMaterial, String> colPartCant;
    @FXML private TableColumn<OrdenCompraMaterial, String> colPartPrecio;
    @FXML private TableColumn<OrdenCompraMaterial, String> colPartImporte;
    @FXML private Label lblTotalOrden;

    private final ObservableList<OrdenCompraMaterial> partidasActuales = FXCollections.observableArrayList();
    private OrdenCompra ordenSeleccionada;

    // 5. Materiales <-> Proveedores (M:N real)
    @FXML private TextField txtBuscarMatProv;
    @FXML private TableView<MaterialProveedor> tblMatProv;
    @FXML private TableColumn<MaterialProveedor, String> colMpMaterial;
    @FXML private TableColumn<MaterialProveedor, String> colMpPrecio;
    @FXML private TableColumn<MaterialProveedor, String> colMpProveedor;
    @FXML private TableColumn<MaterialProveedor, String> colMpTelefono;
    @FXML private TableColumn<MaterialProveedor, String> colMpCorreo;
    @FXML private Label lblModoMatProv;
    @FXML private TextField txtFiltroMatMp;
    @FXML private ComboBox<Material> cmbMaterialMp;
    @FXML private TextField txtFiltroProvMp;
    @FXML private ComboBox<Proveedor> cmbProveedorMp;

    private MaterialProveedor matProvSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarMateriales();
        inicializarProveedores();
        inicializarMovimientos();
        inicializarOrdenes();
        inicializarMatProv();
        refrescarTodo();
    }

    private void refrescarTodo() {
        cargarTablaMateriales(null);
        cargarTablaProveedores(null);
        cargarCombosAuxiliares();
        cargarTablaOrdenes(null);
        cargarTablaMatProv(null);
        actualizarKpis();
    }

    private void actualizarKpis() {
        try {
            List<Material> materiales = materialService.listar(false);
            List<Proveedor> proveedores = proveedorService.listar(false);

            lblTotalMateriales.setText(String.valueOf(materiales.size()));
            long bajoStock = materiales.stream().filter(m -> m.getExistencias() <= 10).count();
            lblBajoStockAlm.setText(String.valueOf(bajoStock));
            lblTotalProveedores.setText(String.valueOf(proveedores.size()));

            BigDecimal valorTotal = materiales.stream()
                    .map(m -> m.getPrecioCosto().multiply(BigDecimal.valueOf(m.getExistencias())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblValorInventario.setText(MONEDA.format(valorTotal));
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MOD_MAT, "ComprasAlmacenController", "ACTUALIZAR_KPIS", ex);
        }
    }

    // =========================================================================
    // 1. MATERIALES (CRUD)
    // =========================================================================
    private void inicializarMateriales() {
        colMatId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdMaterial())));
        colMatDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcionMaterial()));
        colMatPrecio.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getPrecioCosto())));
        colMatExistencias.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getExistencias())));
        colMatStock.setCellValueFactory(d -> {
            int ex = d.getValue().getExistencias();
            return new SimpleStringProperty(ex == 0 ? "Agotado" : (ex <= 10 ? "Bajo stock" : "Disponible"));
        });
        colMatStock.setCellFactory(c -> Tablas.crearCeldaEstado());
        colMatEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));
        colMatEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblMateriales.setPlaceholder(new Label("No hay materiales registrados"));

        tblMateriales.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarMaterialEnFormulario(nuevo);
            }
        });
    }

    @FXML
    private void cargarTablaMateriales(ActionEvent event) {
        try {
            List<Material> lista = materialService.listar(chkInactivosMat.isSelected());
            tblMateriales.setItems(FXCollections.observableArrayList(lista));
            if (event != null) {
                cargarCombosAuxiliares();
                actualizarKpis();
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "LISTAR_MATERIALES", ex);
        }
    }

    @FXML
    private void buscarMateriales(ActionEvent event) {
        try {
            tblMateriales.setItems(FXCollections.observableArrayList(
                    materialService.buscar(txtBuscarMaterial.getText(), chkInactivosMat.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "BUSCAR_MATERIALES", ex);
        }
    }

    @FXML
    private void consultarMaterialPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarIdMat.getText(), "ID de material", MOD_MAT, "CONSULTAR_POR_ID");
            Material m = materialService.consultarPorId(id);
            if (m == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un material con el ID " + id + ".");
                return;
            }
            cargarMaterialEnFormulario(m);
            AlertaUtil.info("Material encontrado",
                    "Descripción: " + m.getDescripcionMaterial()
                    + "\nPrecio costo: " + MONEDA.format(m.getPrecioCosto())
                    + "\nExistencias: " + m.getExistencias()
                    + "\nEstado: " + m.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void guardarMaterial(ActionEvent event) {
        try {
            Material creado = materialService.crear(construirMaterialDesdeFormulario(new Material(), "CREAR_MATERIAL"));
            AlertaUtil.info("Material registrado", "Se guardó '" + creado.getDescripcionMaterial() + "'.");
            limpiarMaterial(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "CREAR_MATERIAL", ex);
        }
    }

    @FXML
    private void actualizarMaterial(ActionEvent event) {
        if (materialSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Elige un material de la tabla para actualizarlo.");
            return;
        }
        try {
            Material editado = construirMaterialDesdeFormulario(materialSeleccionado, "ACTUALIZAR_MATERIAL");
            materialService.actualizar(editado);
            AlertaUtil.info("Material actualizado", "Se actualizó '" + editado.getDescripcionMaterial() + "'.");
            limpiarMaterial(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "ACTUALIZAR_MATERIAL", ex);
        }
    }

    @FXML
    private void desactivarMaterial(ActionEvent event) {
        if (materialSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Elige un material de la tabla.");
            return;
        }
        try {
            materialService.desactivar(materialSeleccionado.getIdMaterial());
            AlertaUtil.info("Material desactivado", "El material quedó inactivo.");
            limpiarMaterial(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "DESACTIVAR_MATERIAL", ex);
        }
    }

    @FXML
    private void reactivarMaterial(ActionEvent event) {
        if (materialSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Marca 'Mostrar inactivos' y elige un material.");
            return;
        }
        try {
            materialService.reactivar(materialSeleccionado.getIdMaterial());
            AlertaUtil.info("Material reactivado", "El material volvió a estar activo.");
            limpiarMaterial(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "REACTIVAR_MATERIAL", ex);
        }
    }

    @FXML
    private void eliminarFisicoMaterial(ActionEvent event) {
        if (materialSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Elige un material de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar material definitivamente",
                "¿Eliminar permanentemente '" + materialSeleccionado.getDescripcionMaterial() + "'?")) {
            return;
        }
        try {
            materialService.eliminarFisico(materialSeleccionado.getIdMaterial());
            AlertaUtil.info("Material eliminado", "El registro fue eliminado de la base de datos.");
            limpiarMaterial(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "ELIMINAR_FISICO_MATERIAL", ex);
        }
    }

    @FXML
    private void limpiarMaterial(ActionEvent event) {
        materialSeleccionado = null;
        lblModoMaterial.setText("Registrar material");
        txtDescMaterial.clear();
        txtPrecioMaterial.clear();
        txtExistenciasMaterial.clear();
        tblMateriales.getSelectionModel().clearSelection();
    }

    private void cargarMaterialEnFormulario(Material m) {
        materialSeleccionado = m;
        lblModoMaterial.setText("Editando Material #" + m.getIdMaterial());
        txtDescMaterial.setText(m.getDescripcionMaterial());
        txtPrecioMaterial.setText(m.getPrecioCosto().toPlainString());
        txtExistenciasMaterial.setText(String.valueOf(m.getExistencias()));
    }

    private Material construirMaterialDesdeFormulario(Material base, String op) throws Exception {
        base.setDescripcionMaterial(txtDescMaterial.getText());
        base.setPrecioCosto(Validador.parsearDecimalNoNegativo(txtPrecioMaterial.getText(), "Precio costo", MOD_MAT, op));
        base.setExistencias(Validador.parsearEnteroNoNegativo(txtExistenciasMaterial.getText(), "Existencias", MOD_MAT, op));
        return base;
    }

    // =========================================================================
    // 2. PROVEEDORES (CRUD)
    // =========================================================================
    private void inicializarProveedores() {
        colProvId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdProveedor())));
        colProvNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProveedor()));
        colProvTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefonoProveedor()));
        colProvCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreoProveedor()));
        colProvEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));
        colProvEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblProveedores.setPlaceholder(new Label("No hay proveedores registrados"));

        tblProveedores.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                cargarProveedorEnFormulario(nuevo);
            }
        });
    }

    @FXML
    private void cargarTablaProveedores(ActionEvent event) {
        try {
            tblProveedores.setItems(FXCollections.observableArrayList(
                    proveedorService.listar(chkInactivosProv.isSelected())));
            if (event != null) {
                cargarCombosAuxiliares();
                actualizarKpis();
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "LISTAR_PROVEEDORES", ex);
        }
    }

    @FXML
    private void buscarProveedores(ActionEvent event) {
        try {
            tblProveedores.setItems(FXCollections.observableArrayList(
                    proveedorService.buscar(txtBuscarProv.getText(), chkInactivosProv.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "BUSCAR_PROVEEDORES", ex);
        }
    }

    @FXML
    private void consultarProveedorPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarIdProv.getText(), "ID de proveedor", MOD_PROV, "CONSULTAR_POR_ID");
            Proveedor p = proveedorService.consultarPorId(id);
            if (p == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un proveedor con el ID " + id + ".");
                return;
            }
            cargarProveedorEnFormulario(p);
            AlertaUtil.info("Proveedor encontrado",
                    "Nombre: " + p.getNombreProveedor()
                    + "\nTeléfono: " + p.getTelefonoProveedor()
                    + "\nCorreo: " + p.getCorreoProveedor()
                    + "\nEstado: " + p.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void seleccionarImagenProveedor(ActionEvent event) {
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    imgProveedor.getScene().getWindow(), "Seleccionar imagen del proveedor", "proveedores", MOD_PROV);
            if (ruta != null) {
                rutaImgProveedor = ruta;
                lblRutaImgProv.setText(ruta);
                imgProveedor.setImage(GestorImagenes.cargarImagen(ruta));
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "SELECCIONAR_IMAGEN", ex);
        }
    }

    @FXML
    private void quitarImagenProveedor(ActionEvent event) {
        rutaImgProveedor = null;
        lblRutaImgProv.setText("Sin imagen seleccionada");
        imgProveedor.setImage(null);
    }

    @FXML
    private void guardarProveedor(ActionEvent event) {
        try {
            Proveedor creado = proveedorService.crear(construirProveedorDesdeFormulario(new Proveedor()));
            AlertaUtil.info("Proveedor registrado", "Se guardó a " + creado.getNombreProveedor() + ".");
            limpiarProveedor(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "CREAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void actualizarProveedor(ActionEvent event) {
        if (proveedorSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Elige un proveedor de la tabla para actualizarlo.");
            return;
        }
        try {
            Proveedor editado = construirProveedorDesdeFormulario(proveedorSeleccionado);
            proveedorService.actualizar(editado);
            AlertaUtil.info("Proveedor actualizado", "Se actualizó a " + editado.getNombreProveedor() + ".");
            limpiarProveedor(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "ACTUALIZAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void desactivarProveedor(ActionEvent event) {
        if (proveedorSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Elige un proveedor de la tabla.");
            return;
        }
        try {
            proveedorService.desactivar(proveedorSeleccionado.getIdProveedor());
            AlertaUtil.info("Proveedor desactivado", "El proveedor quedó inactivo.");
            limpiarProveedor(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "DESACTIVAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void reactivarProveedor(ActionEvent event) {
        if (proveedorSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Marca 'Mostrar inactivos' y elige un proveedor.");
            return;
        }
        try {
            proveedorService.reactivar(proveedorSeleccionado.getIdProveedor());
            AlertaUtil.info("Proveedor reactivado", "El proveedor volvió a estar activo.");
            limpiarProveedor(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "REACTIVAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void eliminarFisicoProveedor(ActionEvent event) {
        if (proveedorSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Elige un proveedor de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar proveedor definitivamente",
                "¿Eliminar permanentemente a " + proveedorSeleccionado.getNombreProveedor() + "?")) {
            return;
        }
        try {
            proveedorService.eliminarFisico(proveedorSeleccionado.getIdProveedor());
            AlertaUtil.info("Proveedor eliminado", "El registro fue eliminado de la base de datos.");
            limpiarProveedor(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_PROV, "ELIMINAR_FISICO_PROVEEDOR", ex);
        }
    }

    @FXML
    private void limpiarProveedor(ActionEvent event) {
        proveedorSeleccionado = null;
        lblModoProveedor.setText("Registrar proveedor");
        txtNombreProveedor.clear();
        txtTelefonoProveedor.clear();
        txtCorreoProveedor.clear();
        quitarImagenProveedor(null);
        tblProveedores.getSelectionModel().clearSelection();
    }

    private void cargarProveedorEnFormulario(Proveedor p) {
        proveedorSeleccionado = p;
        lblModoProveedor.setText("Editando Proveedor #" + p.getIdProveedor());
        txtNombreProveedor.setText(p.getNombreProveedor());
        txtTelefonoProveedor.setText(p.getTelefonoProveedor());
        txtCorreoProveedor.setText(p.getCorreoProveedor());
        rutaImgProveedor = p.getImagen();
        if (rutaImgProveedor != null && !rutaImgProveedor.isBlank()) {
            lblRutaImgProv.setText(rutaImgProveedor);
            imgProveedor.setImage(GestorImagenes.cargarImagen(rutaImgProveedor));
        } else {
            quitarImagenProveedor(null);
        }
    }

    private Proveedor construirProveedorDesdeFormulario(Proveedor base) {
        base.setNombreProveedor(txtNombreProveedor.getText());
        base.setTelefonoProveedor(txtTelefonoProveedor.getText());
        base.setCorreoProveedor(txtCorreoProveedor.getText());
        base.setImagen(rutaImgProveedor);
        return base;
    }

    // =========================================================================
    // 3. MOVIMIENTOS DE STOCK (TRANSACCIONAL CON proyecto_material EN SALIDAS)
    // =========================================================================
    private void inicializarMovimientos() {
        spnCantidadMov.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1));
        dpFechaMov.setValue(LocalDate.now());
        rbSalida.selectedProperty().addListener((o, a, salida) -> cmbProyectoMov.setDisable(!salida));
        Tablas.cargar(tblMovimientos, new String[][]{});
    }

    private void cargarCombosAuxiliares() {
        try {
            List<Material> mats = materialService.listar(false);
            cmbMaterialMov.setItems(FXCollections.observableArrayList(mats));
            cmbMaterialOrden.setItems(FXCollections.observableArrayList(mats));
            ComboBuscadorUtil.configurar(cmbMaterialMp, txtFiltroMatMp, mats, Material::toString);

            List<Proveedor> provs = proveedorService.listar(false);
            cmbProveedorOrden.setItems(FXCollections.observableArrayList(provs));
            ComboBuscadorUtil.configurar(cmbProveedorMp, txtFiltroProvMp, provs, Proveedor::toString);

            List<Proyecto> proys = proyectoService.listar(false);
            cmbProyectoMov.setItems(FXCollections.observableArrayList(proys));
            cmbProyectoOrden.setItems(FXCollections.observableArrayList(proys));

            List<Empleado> emps = empleadoService.listar(false);
            cmbResponsableMov.setItems(FXCollections.observableArrayList(emps));
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MOD_MAT, "ComprasAlmacenController", "CARGAR_COMBOS_AUXILIARES", ex);
        }
    }

    @FXML
    private void registrarMovimiento(ActionEvent event) {
        Material mat = cmbMaterialMov.getValue();
        if (mat == null) {
            Mensajes.error("Faltan datos", "Selecciona el material del movimiento.");
            return;
        }
        int cantidad = spnCantidadMov.getValue();
        boolean esEntrada = rbEntrada.isSelected();
        try {
            int nuevaExistencia;
            String ref;
            if (esEntrada) {
                nuevaExistencia = mat.getExistencias() + cantidad;
                mat.setExistencias(nuevaExistencia);
                materialService.actualizar(mat);
                ref = "Recepción almacén";
            } else {
                Proyecto proyDestino = cmbProyectoMov.getValue();
                if (proyDestino != null) {
                    proyectoMaterialService.asignarConDescuentoStock(
                            proyDestino.getIdProyecto(), mat.getIdMaterial(), BigDecimal.valueOf(cantidad), false);
                    nuevaExistencia = mat.getExistencias() - cantidad;
                    ref = proyDestino.getNombreProyecto() + " (proyecto_material)";
                } else {
                    nuevaExistencia = mat.getExistencias() - cantidad;
                    if (nuevaExistencia < 0) {
                        throw new ValidacionException(MOD_MAT, "REGISTRAR_MOVIMIENTO",
                                "No hay existencias suficientes de '" + mat.getDescripcionMaterial()
                                + "' (actual: " + mat.getExistencias() + ").");
                    }
                    mat.setExistencias(nuevaExistencia);
                    materialService.actualizar(mat);
                    ref = "Salida general";
                }
            }

            String tipo = esEntrada ? "Entrada" : "Salida";
            String resp = cmbResponsableMov.getValue() != null ? cmbResponsableMov.getValue().getNombre() : "Almacén";

            tblMovimientos.getItems().add(0, new String[]{
                dpFechaMov.getValue() != null ? dpFechaMov.getValue().toString() : LocalDate.now().toString(),
                tipo, mat.getDescripcionMaterial(), String.valueOf(cantidad), ref, resp
            });
            Mensajes.info("Movimiento aplicado",
                    "Existencia actualizada de '" + mat.getDescripcionMaterial() + "': " + nuevaExistencia + " unidades.");
            txtObservacionesMov.clear();
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MAT, "REGISTRAR_MOVIMIENTO", ex);
        }
    }

    // =========================================================================
    // 4. ÓRDENES DE COMPRA + PARTIDAS (orden_compra + orden_compra_material M:N)
    // =========================================================================
    private void inicializarOrdenes() {
        dpFechaOrden.setValue(LocalDate.now());
        txtTiempoEntregaOrden.setText("5 días");

        colOrdFolio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFolio()));
        colOrdProveedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProveedorDescriptivo()));
        colOrdProyecto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProyectoDescriptivo()));
        colOrdFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaOrden() != null ? d.getValue().getFechaOrden().toString() : ""));
        colOrdEntrega.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTiempoEntrega() != null ? d.getValue().getTiempoEntrega() : "—"));
        colOrdTotal.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getTotalCompra())));
        colOrdEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoOrden()));
        colOrdEstado.setCellFactory(c -> Tablas.crearCeldaEstado());
        colOrdActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActivoTexto()));
        colOrdActivo.setCellFactory(c -> Tablas.crearCeldaEstado());
        tblOrdenes.setPlaceholder(new Label("No hay órdenes de compra registradas"));

        colPartMat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcionMaterial()));
        colPartCant.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCantidad().stripTrailingZeros().toPlainString()));
        colPartPrecio.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getPrecioCosto())));
        colPartImporte.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getImporte())));
        tblPartidas.setItems(partidasActuales);
        tblPartidas.setPlaceholder(new Label("Agrega materiales a la orden"));

        tblOrdenes.getSelectionModel().selectedItemProperty().addListener((obs, a, nueva) -> {
            if (nueva != null) {
                cargarOrdenEnFormulario(nueva.getIdOrdenCompra());
            }
        });
    }

    @FXML
    private void cargarTablaOrdenes(ActionEvent event) {
        try {
            tblOrdenes.setItems(FXCollections.observableArrayList(
                    ordenCompraService.listar(chkInactivosOrden.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "LISTAR_ORDENES_COMPRA", ex);
        }
    }

    @FXML
    private void buscarOrdenes(ActionEvent event) {
        try {
            tblOrdenes.setItems(FXCollections.observableArrayList(
                    ordenCompraService.buscar(txtBuscarOrden.getText(), chkInactivosOrden.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "BUSCAR_ORDENES_COMPRA", ex);
        }
    }

    private void cargarOrdenEnFormulario(int idOrden) {
        try {
            OrdenCompra completa = ordenCompraService.consultarPorId(idOrden);
            if (completa == null) {
                return;
            }
            ordenSeleccionada = completa;
            lblModoOrden.setText("Editando " + completa.getFolio());
            ComboBuscadorUtil.seleccionarPorPredicado(cmbProyectoOrden,
                    p -> p.getIdProyecto() == completa.getIdProyecto());
            if (completa.getIdProveedor() != null) {
                ComboBuscadorUtil.seleccionarPorPredicado(cmbProveedorOrden,
                        pr -> pr.getIdProveedor() == completa.getIdProveedor());
            } else {
                cmbProveedorOrden.setValue(null);
            }
            dpFechaOrden.setValue(completa.getFechaOrden());
            txtTiempoEntregaOrden.setText(completa.getTiempoEntrega() == null ? "" : completa.getTiempoEntrega());
            partidasActuales.setAll(completa.getPartidas());
            recalcularTotalOrden();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "CONSULTAR_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void agregarPartida(ActionEvent event) {
        try {
            Material mat = cmbMaterialOrden.getValue();
            if (mat == null) {
                throw new ValidacionException(MOD_ORD, "AGREGAR_PARTIDA", "Selecciona un material de la lista.");
            }
            BigDecimal cant = Validador.parsearDecimalPositivo(
                    txtCantidadOrden.getText(), "Cantidad", MOD_ORD, "AGREGAR_PARTIDA");

            OrdenCompraMaterial existente = null;
            for (OrdenCompraMaterial p : partidasActuales) {
                if (p.getIdMaterial() == mat.getIdMaterial()) {
                    existente = p;
                    break;
                }
            }
            if (existente != null) {
                existente.setCantidad(existente.getCantidad().add(cant));
                tblPartidas.refresh();
            } else {
                partidasActuales.add(new OrdenCompraMaterial(
                        0, mat.getIdMaterial(), cant, mat.getDescripcionMaterial(), mat.getPrecioCosto()));
            }
            txtCantidadOrden.clear();
            recalcularTotalOrden();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "AGREGAR_PARTIDA", ex);
        }
    }

    @FXML
    private void quitarPartida(ActionEvent event) {
        OrdenCompraMaterial sel = tblPartidas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            partidasActuales.remove(sel);
            recalcularTotalOrden();
        }
    }

    private void recalcularTotalOrden() {
        BigDecimal total = partidasActuales.stream()
                .map(OrdenCompraMaterial::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalOrden.setText(MONEDA.format(total));
    }

    @FXML
    private void generarOrden(ActionEvent event) {
        try {
            OrdenCompra nueva = construirOrdenDesdeFormulario(new OrdenCompra(), "CREAR_ORDEN_COMPRA");
            OrdenCompra creada = ordenCompraService.crearConPartidas(
                    nueva, new ArrayList<>(partidasActuales), false);
            AlertaUtil.info("Orden de compra registrada",
                    "Se guardó " + creada.getFolio() + " con " + creada.getPartidas().size()
                    + " partidas por un total de " + MONEDA.format(creada.getTotalCompra()) + ".");
            limpiarOrden(null);
            cargarTablaOrdenes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "CREAR_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void actualizarOrden(ActionEvent event) {
        if (ordenSeleccionada == null) {
            AlertaUtil.advertencia("Selecciona una orden", "Elige una orden de compra de la tabla para actualizarla.");
            return;
        }
        try {
            OrdenCompra editada = construirOrdenDesdeFormulario(ordenSeleccionada, "ACTUALIZAR_ORDEN_COMPRA");
            OrdenCompra actualizada = ordenCompraService.actualizarConPartidas(
                    editada, new ArrayList<>(partidasActuales));
            AlertaUtil.info("Orden actualizada",
                    "Se actualizó " + actualizada.getFolio() + " (Total: " + MONEDA.format(actualizada.getTotalCompra()) + ").");
            limpiarOrden(null);
            cargarTablaOrdenes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "ACTUALIZAR_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void registrarRecepcion(ActionEvent event) {
        OrdenCompra orden = tblOrdenes.getSelectionModel().getSelectedItem();
        if (orden == null) {
            AlertaUtil.advertencia("Selecciona una orden", "Elige la orden de compra recibida en la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Registrar recepción de orden",
                "¿Confirmas la recepción de " + orden.getFolio()
                + "? Esto incrementará automáticamente las existencias de sus materiales en MySQL.")) {
            return;
        }
        try {
            ordenCompraService.recibirOrden(orden.getIdOrdenCompra(), false);
            AlertaUtil.info("Recepción registrada",
                    "La orden " + orden.getFolio() + " pasó a 'Recibida' y el inventario fue actualizado.");
            limpiarOrden(null);
            refrescarTodo();
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "RECIBIR_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void cancelarOrden(ActionEvent event) {
        OrdenCompra orden = tblOrdenes.getSelectionModel().getSelectedItem();
        if (orden == null) {
            AlertaUtil.advertencia("Selecciona una orden", "Elige una orden de compra de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Cancelar orden", "¿Cancelar la orden " + orden.getFolio() + "?")) {
            return;
        }
        try {
            ordenCompraService.cancelarOrden(orden.getIdOrdenCompra());
            AlertaUtil.info("Orden cancelada", "La orden " + orden.getFolio() + " quedó marcada como Cancelada.");
            limpiarOrden(null);
            cargarTablaOrdenes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "CANCELAR_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void desactivarOrden(ActionEvent event) {
        OrdenCompra orden = tblOrdenes.getSelectionModel().getSelectedItem();
        if (orden == null) {
            AlertaUtil.advertencia("Selecciona una orden", "Elige una orden de la tabla.");
            return;
        }
        try {
            ordenCompraService.desactivar(orden.getIdOrdenCompra());
            AlertaUtil.info("Orden desactivada", "La orden " + orden.getFolio() + " quedó inactiva.");
            limpiarOrden(null);
            cargarTablaOrdenes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "DESACTIVAR_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void reactivarOrden(ActionEvent event) {
        OrdenCompra orden = tblOrdenes.getSelectionModel().getSelectedItem();
        if (orden == null) {
            AlertaUtil.advertencia("Selecciona una orden", "Marca 'Mostrar inactivas' y elige una orden.");
            return;
        }
        try {
            ordenCompraService.reactivar(orden.getIdOrdenCompra());
            AlertaUtil.info("Orden reactivada", "La orden " + orden.getFolio() + " volvió a estar activa.");
            limpiarOrden(null);
            cargarTablaOrdenes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "REACTIVAR_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void eliminarFisicoOrden(ActionEvent event) {
        OrdenCompra orden = tblOrdenes.getSelectionModel().getSelectedItem();
        if (orden == null) {
            AlertaUtil.advertencia("Selecciona una orden", "Elige una orden de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar orden definitivamente",
                "¿Eliminar permanentemente " + orden.getFolio() + " junto con sus partidas?")) {
            return;
        }
        try {
            ordenCompraService.eliminarFisico(orden.getIdOrdenCompra());
            AlertaUtil.info("Orden eliminada", "La orden y sus partidas fueron eliminadas de la base de datos.");
            limpiarOrden(null);
            cargarTablaOrdenes(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_ORD, "ELIMINAR_FISICO_ORDEN_COMPRA", ex);
        }
    }

    @FXML
    private void limpiarOrden(ActionEvent event) {
        ordenSeleccionada = null;
        lblModoOrden.setText("Nueva orden de compra");
        cmbProyectoOrden.setValue(null);
        cmbProveedorOrden.setValue(null);
        dpFechaOrden.setValue(LocalDate.now());
        txtTiempoEntregaOrden.setText("5 días");
        cmbMaterialOrden.setValue(null);
        txtCantidadOrden.clear();
        partidasActuales.clear();
        recalcularTotalOrden();
        tblOrdenes.getSelectionModel().clearSelection();
    }

    private OrdenCompra construirOrdenDesdeFormulario(OrdenCompra base, String op) throws ValidacionException {
        Proyecto proy = cmbProyectoOrden.getValue();
        if (proy == null) {
            throw new ValidacionException(MOD_ORD, op, "Selecciona el proyecto para la orden de compra.");
        }
        Proveedor prov = cmbProveedorOrden.getValue();
        base.setIdProyecto(proy.getIdProyecto());
        base.setIdProveedor(prov != null ? prov.getIdProveedor() : null);
        base.setFechaOrden(dpFechaOrden.getValue() != null ? dpFechaOrden.getValue() : LocalDate.now());
        base.setTiempoEntrega(txtTiempoEntregaOrden.getText());
        if (base.getEstadoOrden() == null || base.getEstadoOrden().isBlank()) {
            base.setEstadoOrden("Solicitada");
        }
        return base;
    }

    // =========================================================================
    // 5. MATERIALES <-> PROVEEDORES (material_proveedor M:N)
    // =========================================================================
    private void inicializarMatProv() {
        colMpMaterial.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcionMaterial()));
        colMpPrecio.setCellValueFactory(d -> new SimpleStringProperty(MONEDA.format(d.getValue().getPrecioCosto())));
        colMpProveedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProveedor()));
        colMpTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefonoProveedor()));
        colMpCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreoProveedor()));
        tblMatProv.setPlaceholder(new Label("No hay relaciones material-proveedor registradas"));

        tblMatProv.getSelectionModel().selectedItemProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                matProvSeleccionado = nuevo;
                lblModoMatProv.setText("Editando relación M:N");
                txtFiltroMatMp.clear();
                txtFiltroProvMp.clear();
                ComboBuscadorUtil.seleccionarPorPredicado(cmbMaterialMp, m -> m.getIdMaterial() == nuevo.getIdMaterial());
                ComboBuscadorUtil.seleccionarPorPredicado(cmbProveedorMp, p -> p.getIdProveedor() == nuevo.getIdProveedor());
            }
        });
    }

    @FXML
    private void cargarTablaMatProv(ActionEvent event) {
        try {
            tblMatProv.setItems(FXCollections.observableArrayList(matProvService.listarTodos()));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MP, "LISTAR_MATERIAL_PROVEEDOR", ex);
        }
    }

    @FXML
    private void buscarMatProv(ActionEvent event) {
        try {
            tblMatProv.setItems(FXCollections.observableArrayList(
                    matProvService.buscar(txtBuscarMatProv.getText())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MP, "BUSCAR_MATERIAL_PROVEEDOR", ex);
        }
    }

    @FXML
    private void filtrarPorMaterialMp(ActionEvent event) {
        Material m = cmbMaterialMp.getValue();
        if (m == null) {
            AlertaUtil.advertencia("Selecciona un material", "Elige primero un material en el combo del formulario.");
            return;
        }
        try {
            tblMatProv.setItems(FXCollections.observableArrayList(
                    matProvService.listarPorMaterial(m.getIdMaterial())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MP, "LISTAR_POR_MATERIAL", ex);
        }
    }

    @FXML
    private void filtrarPorProveedorMp(ActionEvent event) {
        Proveedor p = cmbProveedorMp.getValue();
        if (p == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Elige primero un proveedor en el combo del formulario.");
            return;
        }
        try {
            tblMatProv.setItems(FXCollections.observableArrayList(
                    matProvService.listarPorProveedor(p.getIdProveedor())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MP, "LISTAR_POR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void asociarMatProv(ActionEvent event) {
        try {
            Material m = cmbMaterialMp.getValue();
            Proveedor p = cmbProveedorMp.getValue();
            if (m == null || p == null) {
                throw new ValidacionException(MOD_MP, "ASOCIAR_MATERIAL_PROVEEDOR",
                        "Selecciona tanto un material como un proveedor de las listas desplegables.");
            }
            matProvService.asociar(m.getIdMaterial(), p.getIdProveedor());
            AlertaUtil.info("Relación registrada",
                    "Se asoció '" + m.getDescripcionMaterial() + "' con '" + p.getNombreProveedor() + "'.");
            limpiarMatProv(null);
            cargarTablaMatProv(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MP, "ASOCIAR_MATERIAL_PROVEEDOR", ex);
        }
    }

    @FXML
    private void actualizarMatProv(ActionEvent event) {
        if (matProvSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona una relación", "Elige una fila de la tabla para actualizarla.");
            return;
        }
        try {
            Material m = cmbMaterialMp.getValue();
            Proveedor p = cmbProveedorMp.getValue();
            if (m == null || p == null) {
                throw new ValidacionException(MOD_MP, "ACTUALIZAR_MATERIAL_PROVEEDOR",
                        "Selecciona el nuevo material y proveedor.");
            }
            matProvService.actualizarAsociacion(
                    matProvSeleccionado.getIdMaterial(), matProvSeleccionado.getIdProveedor(),
                    m.getIdMaterial(), p.getIdProveedor());
            AlertaUtil.info("Relación actualizada", "La asociación material-proveedor fue actualizada.");
            limpiarMatProv(null);
            cargarTablaMatProv(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MP, "ACTUALIZAR_MATERIAL_PROVEEDOR", ex);
        }
    }

    @FXML
    private void eliminarMatProv(ActionEvent event) {
        if (matProvSeleccionado == null) {
            AlertaUtil.advertencia("Selecciona una relación", "Elige una fila de la tabla para desvincularla.");
            return;
        }
        if (!AlertaUtil.confirmar("Desvincular material y proveedor",
                "¿Eliminar la relación entre '" + matProvSeleccionado.getDescripcionMaterial()
                + "' y '" + matProvSeleccionado.getNombreProveedor() + "'?")) {
            return;
        }
        try {
            matProvService.eliminar(matProvSeleccionado.getIdMaterial(), matProvSeleccionado.getIdProveedor());
            AlertaUtil.info("Relación eliminada", "Se desvinculó el proveedor del material.");
            limpiarMatProv(null);
            cargarTablaMatProv(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MOD_MP, "ELIMINAR_MATERIAL_PROVEEDOR", ex);
        }
    }

    @FXML
    private void limpiarMatProv(ActionEvent event) {
        matProvSeleccionado = null;
        lblModoMatProv.setText("Asociar material y proveedor");
        txtFiltroMatMp.clear();
        txtFiltroProvMp.clear();
        cmbMaterialMp.setValue(null);
        cmbProveedorMp.setValue(null);
        tblMatProv.getSelectionModel().clearSelection();
    }
}
