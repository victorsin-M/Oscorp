package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.servicio.ClienteService;
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
 * Controlador CRUD del modulo Clientes.
 */
public class ClientesController implements Initializable {

    private static final String MODULO = "Clientes";
    private final ClienteService service = new ClienteService();

    @FXML private CheckBox chkIncluirInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtConsultarId;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colId;
    @FXML private TableColumn<Cliente, String> colRfc;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TableColumn<Cliente, String> colEstadoReg;

    @FXML private Label lblModoFormulario;
    @FXML private TextField txtRfc;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtColonia;
    @FXML private TextField txtEstadoGeo;
    @FXML private TextField txtTelefonoInicial;
    @FXML private ImageView imgCliente;
    @FXML private Label lblRutaImagen;

    private Cliente seleccionado;
    private String rutaImagenActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdCliente())));
        colRfc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRfc()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreo()));
        colDireccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDireccionResumen()));
        colEstadoReg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));

        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });

        cargarTabla(null);
    }

    @FXML
    private void cargarTabla(ActionEvent event) {
        try {
            tblClientes.setItems(FXCollections.observableArrayList(
                    service.listar(chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_CLIENTES", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblClientes.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_CLIENTES", ex);
        }
    }

    @FXML
    private void consultarPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarId.getText(), "ID de cliente", MODULO, "CONSULTAR_POR_ID");
            Cliente encontrado = service.consultarPorId(id);
            if (encontrado == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un cliente con el ID " + id + ".");
                return;
            }
            cargarEnFormulario(encontrado);
            AlertaUtil.info("Cliente encontrado",
                    "Nombre: " + encontrado.getNombreCliente()
                    + "\nRFC: " + encontrado.getRfc()
                    + "\nCorreo: " + encontrado.getCorreo()
                    + "\nEstado del registro: " + encontrado.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void seleccionarImagen(ActionEvent event) {
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    imgCliente.getScene().getWindow(), "Seleccionar imagen de cliente", "clientes", MODULO);
            if (ruta != null) {
                rutaImagenActual = ruta;
                lblRutaImagen.setText(ruta);
                imgCliente.setImage(GestorImagenes.cargarImagen(ruta));
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "SELECCIONAR_IMAGEN", ex);
        }
    }

    @FXML
    private void quitarImagen(ActionEvent event) {
        rutaImagenActual = null;
        lblRutaImagen.setText("Sin imagen seleccionada");
        imgCliente.setImage(null);
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            Cliente nuevo = construirDesdeFormulario(new Cliente());
            String telInicial = txtTelefonoInicial.getText();
            Cliente creado = (telInicial != null && !telInicial.isBlank())
                    ? service.crearConTelefonoInicial(nuevo, telInicial, false)
                    : service.crear(nuevo);
            AlertaUtil.info("Cliente registrado",
                    "Se guardó correctamente a " + creado.getNombreCliente()
                    + (telInicial != null && !telInicial.isBlank() ? " junto con su teléfono inicial." : "."));
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_CLIENTE", ex);
        }
    }

    @FXML
    private void actualizar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige un cliente de la tabla para actualizarlo.");
            return;
        }
        try {
            Cliente editado = construirDesdeFormulario(seleccionado);
            service.actualizar(editado);
            AlertaUtil.info("Cliente actualizado", "Los datos de " + editado.getNombreCliente() + " fueron actualizados.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_CLIENTE", ex);
        }
    }

    @FXML
    private void desactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige un cliente de la tabla para desactivarlo.");
            return;
        }
        if (!AlertaUtil.confirmar("Desactivar cliente",
                "¿Deseas marcar como inactivo a " + seleccionado.getNombreCliente() + "?")) {
            return;
        }
        try {
            service.desactivar(seleccionado.getIdCliente());
            AlertaUtil.info("Cliente desactivado", "El registro pasó a estado inactivo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_CLIENTE", ex);
        }
    }

    @FXML
    private void reactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Marca 'Mostrar inactivos' y elige un cliente inactivo.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdCliente());
            AlertaUtil.info("Cliente reactivado", "El cliente volvió a estar activo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_CLIENTE", ex);
        }
    }

    @FXML
    private void eliminarFisico(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige un cliente de la tabla para eliminarlo.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar cliente definitivamente",
                "Esta acción borrará permanentemente a " + seleccionado.getNombreCliente()
                + " de la base de datos. ¿Continuar?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdCliente());
            AlertaUtil.info("Cliente eliminado", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_CLIENTE", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        lblModoFormulario.setText("Registro de Cliente");
        txtRfc.clear();
        txtNombre.clear();
        txtCorreo.clear();
        txtCalle.clear();
        txtNumero.clear();
        txtColonia.clear();
        txtEstadoGeo.clear();
        txtTelefonoInicial.clear();
        txtTelefonoInicial.setDisable(false);
        quitarImagen(null);
        tblClientes.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(Cliente c) {
        seleccionado = c;
        lblModoFormulario.setText("Editando Cliente ID #" + c.getIdCliente());
        txtRfc.setText(c.getRfc());
        txtNombre.setText(c.getNombreCliente());
        txtCorreo.setText(c.getCorreo());
        txtCalle.setText(c.getCalle() == null ? "" : c.getCalle());
        txtNumero.setText(c.getNumero() == null ? "" : c.getNumero());
        txtColonia.setText(c.getColonia() == null ? "" : c.getColonia());
        txtEstadoGeo.setText(c.getEstado() == null ? "" : c.getEstado());
        txtTelefonoInicial.clear();
        txtTelefonoInicial.setDisable(true);
        rutaImagenActual = c.getImagen();
        if (rutaImagenActual != null && !rutaImagenActual.isBlank()) {
            lblRutaImagen.setText(rutaImagenActual);
            imgCliente.setImage(GestorImagenes.cargarImagen(rutaImagenActual));
        } else {
            quitarImagen(null);
        }
    }

    private Cliente construirDesdeFormulario(Cliente base) {
        base.setRfc(txtRfc.getText());
        base.setNombreCliente(txtNombre.getText());
        base.setCorreo(txtCorreo.getText());
        base.setCalle(txtCalle.getText());
        base.setNumero(txtNumero.getText());
        base.setColonia(txtColonia.getText());
        base.setEstado(txtEstadoGeo.getText());
        base.setImagen(rutaImagenActual);
        return base;
    }
}
