package soptecit.controladores;

import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.ProveedorService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
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
 * Controlador estilizado de Compras-Almacén.
 * Integra CRUD completo de Materiales y Proveedores, KPIs en vivo y ajustes reales de existencias.
 */
public class ComprasAlmacenController implements Initializable {

    private static final String MOD_MAT = "Materiales";
    private static final String MOD_PROV = "Proveedores";
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final MaterialService materialService = new MaterialService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final ProyectoService proyectoService = new ProyectoService();
    private final EmpleadoService empleadoService = new EmpleadoService();

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

    // 4. Órdenes de compra
    @FXML private TableView<String[]> tblOrdenes;
    @FXML private ComboBox<Proveedor> cmbProveedorOrden;
    @FXML private ComboBox<Proyecto> cmbProyectoOrden;
    @FXML private DatePicker dpEntregaOrden;
    @FXML private ComboBox<Material> cmbMaterialOrden;
    @FXML private TextField txtCantidadOrden;
    @FXML private TableView<String[]> tblPartidas;
    @FXML private Label lblTotalOrden;

    private double totalOrdenActual = 0;
    private int consecutivoOrden = 46;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarMateriales();
        inicializarProveedores();
        inicializarMovimientosYOrdenes();
        refrescarTodo();
    }

    private void refrescarTodo() {
        cargarTablaMateriales(null);
        cargarTablaProveedores(null);
        cargarCombosAuxiliares();
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
            // Ya reportado por los listados
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
    // 3 y 4. MOVIMIENTOS (ACTUALIZA STOCK EN MYSQL) Y ÓRDENES DE COMPRA
    // =========================================================================
    private void inicializarMovimientosYOrdenes() {
        spnCantidadMov.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1));
        dpFechaMov.setValue(LocalDate.now());
        dpEntregaOrden.setValue(LocalDate.now().plusDays(5));

        rbSalida.selectedProperty().addListener((o, a, salida) -> cmbProyectoMov.setDisable(!salida));

        Tablas.cargar(tblMovimientos, new String[][]{});
        Tablas.cargar(tblPartidas, new String[][]{});
        Tablas.cargar(tblOrdenes, new String[][]{
            {"OC-0045", "Proveedor general", "PRY-0001", LocalDate.now().plusDays(3).toString(), "$18,400.00", "Solicitada"}
        });
    }

    private void cargarCombosAuxiliares() {
        try {
            List<Material> mats = materialService.listar(false);
            cmbMaterialMov.setItems(FXCollections.observableArrayList(mats));
            cmbMaterialOrden.setItems(FXCollections.observableArrayList(mats));

            List<Proveedor> provs = proveedorService.listar(false);
            cmbProveedorOrden.setItems(FXCollections.observableArrayList(provs));

            List<Proyecto> proys = proyectoService.listar(false);
            cmbProyectoMov.setItems(FXCollections.observableArrayList(proys));
            cmbProyectoOrden.setItems(FXCollections.observableArrayList(proys));

            List<Empleado> emps = empleadoService.listar(false);
            cmbResponsableMov.setItems(FXCollections.observableArrayList(emps));
        } catch (Exception ex) {
            // Ya reportado si falla BD
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
        int nuevaExistencia = esEntrada ? mat.getExistencias() + cantidad : mat.getExistencias() - cantidad;
        if (nuevaExistencia < 0) {
            Mensajes.error("Stock insuficiente",
                    "No hay existencias suficientes de '" + mat.getDescripcionMaterial()
                    + "' (actual: " + mat.getExistencias() + ").");
            return;
        }
        try {
            mat.setExistencias(nuevaExistencia);
            materialService.actualizar(mat);

            String tipo = esEntrada ? "Entrada" : "Salida";
            String ref = esEntrada ? "Recepción almacén"
                    : (cmbProyectoMov.getValue() != null ? cmbProyectoMov.getValue().getNombreProyecto() : "Proyecto");
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

    @FXML
    private void agregarPartida(ActionEvent event) {
        Material mat = cmbMaterialOrden.getValue();
        if (mat == null || txtCantidadOrden.getText().trim().isEmpty()) {
            return;
        }
        try {
            int cant = Integer.parseInt(txtCantidadOrden.getText().trim());
            if (cant <= 0) {
                return;
            }
            double precio = mat.getPrecioUnitario();
            double importe = cant * precio;
            totalOrdenActual += importe;
            tblPartidas.getItems().add(new String[]{
                mat.getDescripcionMaterial(), String.valueOf(cant), MONEDA.format(precio), MONEDA.format(importe)
            });
            lblTotalOrden.setText(MONEDA.format(totalOrdenActual));
            txtCantidadOrden.clear();
        } catch (NumberFormatException ex) {
            Mensajes.error("Cantidad inválida", "Captura un número entero positivo.");
        }
    }

    @FXML
    private void generarOrden(ActionEvent event) {
        Proveedor prov = cmbProveedorOrden.getValue();
        if (prov == null || tblPartidas.getItems().isEmpty()) {
            Mensajes.error("Faltan datos", "Selecciona el proveedor y agrega al menos una partida.");
            return;
        }
        String proyTxt = cmbProyectoOrden.getValue() != null ? cmbProyectoOrden.getValue().getNombreProyecto() : "Stock general";
        String entrega = dpEntregaOrden.getValue() != null ? dpEntregaOrden.getValue().toString() : "";
        tblOrdenes.getItems().add(0, new String[]{
            String.format("OC-%04d", consecutivoOrden++),
            prov.getNombreProveedor(),
            proyTxt,
            entrega,
            MONEDA.format(totalOrdenActual),
            "Solicitada"
        });
        tblPartidas.getItems().clear();
        totalOrdenActual = 0;
        lblTotalOrden.setText("$0.00");
        Mensajes.info("Orden generada", "La orden de compra para " + prov.getNombreProveedor() + " quedó registrada.");
    }

    @FXML
    private void registrarRecepcion(ActionEvent event) {
        String[] orden = tblOrdenes.getSelectionModel().getSelectedItem();
        if (orden != null) {
            orden[5] = "Recibida";
            tblOrdenes.refresh();
        }
    }

    @FXML
    private void cancelarOrden(ActionEvent event) {
        String[] orden = tblOrdenes.getSelectionModel().getSelectedItem();
        if (orden != null && Mensajes.confirmar("Cancelar orden", "¿Cancelar la orden " + orden[0] + "?")) {
            orden[5] = "Cancelada";
            tblOrdenes.refresh();
        }
    }
}
