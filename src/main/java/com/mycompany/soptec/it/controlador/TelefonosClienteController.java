package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.ClienteTelefono;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.ClienteTelefonoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.ComboBuscadorUtil;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador CRUD para telefonos de clientes mediante combo/buscador descriptivo.
 */
public class TelefonosClienteController implements Initializable {

    private static final String MODULO = "TelefonosCliente";
    private final ClienteTelefonoService service = new ClienteTelefonoService();
    private final ClienteService clienteService = new ClienteService();

    @FXML private CheckBox chkIncluirInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TableView<ClienteTelefono> tblTelefonos;
    @FXML private TableColumn<ClienteTelefono, String> colCliente;
    @FXML private TableColumn<ClienteTelefono, String> colTelefono;
    @FXML private TableColumn<ClienteTelefono, String> colEstadoReg;

    @FXML private TextField txtFiltroCliente;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TextField txtTelefono;

    private ClienteTelefono seleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClienteDescriptivo()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colEstadoReg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));

        tblTelefonos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });

        cargarComboClientes();
        cargarTabla(null);
    }

    private void cargarComboClientes() {
        try {
            List<Cliente> activos = clienteService.listar(false);
            ComboBuscadorUtil.configurar(cmbCliente, txtFiltroCliente, activos, Cliente::toString);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CARGAR_COMBO_CLIENTES", ex);
        }
    }

    @FXML
    private void cargarTabla(ActionEvent event) {
        try {
            cargarComboClientes();
            tblTelefonos.setItems(FXCollections.observableArrayList(
                    service.listar(chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_TELEFONOS", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblTelefonos.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_TELEFONOS", ex);
        }
    }

    @FXML
    private void filtrarPorClienteCombo(ActionEvent event) {
        Cliente cli = cmbCliente.getValue();
        if (cli == null) {
            AlertaUtil.advertencia("Selecciona un cliente", "Elige primero un cliente en el combo del formulario.");
            return;
        }
        try {
            tblTelefonos.setItems(FXCollections.observableArrayList(
                    service.listarPorCliente(cli.getIdCliente(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_POR_CLIENTE", ex);
        }
    }

    @FXML
    private void consultarSeleccionado(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un registro", "Elige un teléfono de la tabla para consultarlo.");
            return;
        }
        try {
            ClienteTelefono ct = service.consultarPorClave(seleccionado.getIdCliente(), seleccionado.getTelefono());
            if (ct == null) {
                AlertaUtil.advertencia("No encontrado", "El registro ya no existe.");
                return;
            }
            AlertaUtil.info("Detalle de Teléfono",
                    "Cliente: " + ct.getClienteDescriptivo()
                    + "\nTeléfono: " + ct.getTelefono()
                    + "\nEstado: " + ct.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_TELEFONO", ex);
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            Cliente cli = cmbCliente.getValue();
            if (cli == null) {
                AlertaUtil.advertencia("Cliente requerido", "Selecciona un cliente de la lista desplegable.");
                return;
            }
            ClienteTelefono nuevo = new ClienteTelefono(cli.getIdCliente(), txtTelefono.getText(), true);
            service.crear(nuevo);
            AlertaUtil.info("Teléfono registrado", "Se asoció el teléfono a " + cli.getNombreCliente() + ".");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_TELEFONO", ex);
        }
    }

    @FXML
    private void actualizar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Elige una fila de la tabla para actualizarla.");
            return;
        }
        try {
            Cliente cli = cmbCliente.getValue();
            if (cli == null) {
                AlertaUtil.advertencia("Cliente requerido", "Selecciona un cliente de la lista desplegable.");
                return;
            }
            ClienteTelefono actualizado = new ClienteTelefono(cli.getIdCliente(), txtTelefono.getText(), seleccionado.isActivo());
            service.actualizar(seleccionado.getIdCliente(), seleccionado.getTelefono(), actualizado);
            AlertaUtil.info("Teléfono actualizado", "El teléfono se actualizó correctamente.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_TELEFONO", ex);
        }
    }

    @FXML
    private void desactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Elige una fila de la tabla.");
            return;
        }
        try {
            service.desactivar(seleccionado.getIdCliente(), seleccionado.getTelefono());
            AlertaUtil.info("Teléfono desactivado", "El teléfono quedó inactivo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_TELEFONO", ex);
        }
    }

    @FXML
    private void reactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Marca 'Mostrar inactivos' y selecciona un registro.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdCliente(), seleccionado.getTelefono());
            AlertaUtil.info("Teléfono reactivado", "El teléfono volvió a estar activo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_TELEFONO", ex);
        }
    }

    @FXML
    private void eliminarFisico(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un teléfono", "Elige una fila de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar teléfono definitivamente",
                "¿Eliminar permanentemente el teléfono " + seleccionado.getTelefono() + "?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdCliente(), seleccionado.getTelefono());
            AlertaUtil.info("Teléfono eliminado", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_TELEFONO", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        txtFiltroCliente.clear();
        cmbCliente.setValue(null);
        txtTelefono.clear();
        tblTelefonos.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(ClienteTelefono ct) {
        seleccionado = ct;
        txtFiltroCliente.clear();
        ComboBuscadorUtil.seleccionarPorPredicado(cmbCliente, c -> c.getIdCliente() == ct.getIdCliente());
        txtTelefono.setText(ct.getTelefono());
    }
}
