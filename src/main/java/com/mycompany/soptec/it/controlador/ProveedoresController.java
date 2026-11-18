package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.servicio.ProveedorService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.GestorImagenes;
import com.mycompany.soptec.it.util.Validador;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Controlador CRUD del modulo Proveedores.
 */
public class ProveedoresController implements Initializable {

    private static final String MODULO = "Proveedores";
    private final ProveedorService service = new ProveedorService();

    @FXML private CheckBox chkIncluirInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtConsultarId;
    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, String> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colCorreo;
    @FXML private TableColumn<Proveedor, String> colEstadoReg;

    @FXML private Label lblModoFormulario;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ImageView imgProveedor;
    @FXML private Label lblRutaImagen;

    private Proveedor seleccionado;
    private String rutaImagenActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdProveedor())));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProveedor()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefonoProveedor()));
        colCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreoProveedor()));
        colEstadoReg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));

        tblProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldV, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });

        cargarTabla(null);
    }

    @FXML
    private void cargarTabla(ActionEvent event) {
        try {
            tblProveedores.setItems(FXCollections.observableArrayList(
                    service.listar(chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_PROVEEDORES", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblProveedores.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_PROVEEDORES", ex);
        }
    }

    @FXML
    private void consultarPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarId.getText(), "ID de proveedor", MODULO, "CONSULTAR_POR_ID");
            Proveedor p = service.consultarPorId(id);
            if (p == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un proveedor con el ID " + id + ".");
                return;
            }
            cargarEnFormulario(p);
            AlertaUtil.info("Proveedor encontrado",
                    "Nombre: " + p.getNombreProveedor()
                    + "\nTeléfono: " + p.getTelefonoProveedor()
                    + "\nCorreo: " + p.getCorreoProveedor()
                    + "\nEstado: " + p.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void seleccionarImagen(ActionEvent event) {
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    imgProveedor.getScene().getWindow(), "Seleccionar imagen del proveedor", "proveedores", MODULO);
            if (ruta != null) {
                rutaImagenActual = ruta;
                lblRutaImagen.setText(ruta);
                imgProveedor.setImage(GestorImagenes.cargarImagen(ruta));
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "SELECCIONAR_IMAGEN", ex);
        }
    }

    @FXML
    private void quitarImagen(ActionEvent event) {
        rutaImagenActual = null;
        lblRutaImagen.setText("Sin imagen seleccionada");
        imgProveedor.setImage(null);
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            Proveedor creado = service.crear(construirDesdeFormulario(new Proveedor()));
            AlertaUtil.info("Proveedor registrado", "Se guardó a " + creado.getNombreProveedor() + ".");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void actualizar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Elige un proveedor de la tabla para actualizarlo.");
            return;
        }
        try {
            Proveedor editado = construirDesdeFormulario(seleccionado);
            service.actualizar(editado);
            AlertaUtil.info("Proveedor actualizado", "Se actualizó a " + editado.getNombreProveedor() + ".");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void desactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Elige un proveedor de la tabla.");
            return;
        }
        try {
            service.desactivar(seleccionado.getIdProveedor());
            AlertaUtil.info("Proveedor desactivado", "El proveedor quedó inactivo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void reactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Marca 'Mostrar inactivos' y elige un proveedor.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdProveedor());
            AlertaUtil.info("Proveedor reactivado", "El proveedor volvió a estar activo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_PROVEEDOR", ex);
        }
    }

    @FXML
    private void eliminarFisico(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proveedor", "Elige un proveedor de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar proveedor definitivamente",
                "¿Eliminar permanentemente a " + seleccionado.getNombreProveedor() + "?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdProveedor());
            AlertaUtil.info("Proveedor eliminado", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_PROVEEDOR", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        lblModoFormulario.setText("Registro de Proveedor");
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        quitarImagen(null);
        tblProveedores.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(Proveedor p) {
        seleccionado = p;
        lblModoFormulario.setText("Editando Proveedor ID #" + p.getIdProveedor());
        txtNombre.setText(p.getNombreProveedor());
        txtTelefono.setText(p.getTelefonoProveedor());
        txtCorreo.setText(p.getCorreoProveedor());
        rutaImagenActual = p.getImagen();
        if (rutaImagenActual != null && !rutaImagenActual.isBlank()) {
            lblRutaImagen.setText(rutaImagenActual);
            imgProveedor.setImage(GestorImagenes.cargarImagen(rutaImagenActual));
        } else {
            quitarImagen(null);
        }
    }

    private Proveedor construirDesdeFormulario(Proveedor base) {
        base.setNombreProveedor(txtNombre.getText());
        base.setTelefonoProveedor(txtTelefono.getText());
        base.setCorreoProveedor(txtCorreo.getText());
        base.setImagen(rutaImagenActual);
        return base;
    }
}
