package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.ComboBuscadorUtil;
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
import javafx.scene.control.TextField;

/**
 * Controlador CRUD del modulo Proyectos (relacionado con Cliente por combo descriptivo).
 */
public class ProyectosController implements Initializable {

    private static final String MODULO = "Proyectos";
    private final ProyectoService service = new ProyectoService();
    private final ClienteService clienteService = new ClienteService();

    @FXML private CheckBox chkIncluirInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtConsultarId;
    @FXML private TableView<Proyecto> tblProyectos;
    @FXML private TableColumn<Proyecto, String> colId;
    @FXML private TableColumn<Proyecto, String> colNombre;
    @FXML private TableColumn<Proyecto, String> colCliente;
    @FXML private TableColumn<Proyecto, String> colInicio;
    @FXML private TableColumn<Proyecto, String> colFin;
    @FXML private TableColumn<Proyecto, String> colCosto;
    @FXML private TableColumn<Proyecto, String> colEstadoProy;
    @FXML private TableColumn<Proyecto, String> colActivo;

    @FXML private Label lblModoFormulario;
    @FXML private TextField txtFiltroCliente;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TextField txtNombreProyecto;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private ComboBox<String> cmbEstadoProyecto;
    @FXML private TextField txtCosto;

    private Proyecto seleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbEstadoProyecto.getItems().addAll("Planeado", "En proceso", "En revisión", "Finalizado", "Cancelado");
        cmbEstadoProyecto.setValue("Planeado");
        dpInicio.setValue(LocalDate.now());

        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdProyecto())));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProyecto()));
        colCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClienteDescriptivo()));
        colInicio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaInicio() != null ? d.getValue().getFechaInicio().toString() : ""));
        colFin.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaEstimadaFin() != null ? d.getValue().getFechaEstimadaFin().toString() : "-"));
        colCosto.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getCostoTotal().toPlainString()));
        colEstadoProy.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActivoTexto()));

        tblProyectos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, nuevo) -> {
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
            tblProyectos.setItems(FXCollections.observableArrayList(
                    service.listar(chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_PROYECTOS", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblProyectos.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_PROYECTOS", ex);
        }
    }

    @FXML
    private void consultarPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarId.getText(), "ID de proyecto", MODULO, "CONSULTAR_POR_ID");
            Proyecto p = service.consultarPorId(id);
            if (p == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un proyecto con el ID " + id + ".");
                return;
            }
            cargarEnFormulario(p);
            AlertaUtil.info("Proyecto encontrado",
                    "Proyecto: " + p.getNombreProyecto()
                    + "\nCliente: " + p.getClienteDescriptivo()
                    + "\nInicio: " + p.getFechaInicio()
                    + "\nCosto: $" + p.getCostoTotal()
                    + "\nEstado proyecto: " + p.getEstado()
                    + "\nRegistro: " + p.getActivoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            Proyecto nuevo = construirDesdeFormulario(new Proyecto(), "CREAR_PROYECTO");
            Proyecto creado = service.crear(nuevo);
            AlertaUtil.info("Proyecto registrado",
                    "Se guardó '" + creado.getNombreProyecto() + "' para " + creado.getClienteDescriptivo() + ".");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_PROYECTO", ex);
        }
    }

    @FXML
    private void actualizar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla para actualizarlo.");
            return;
        }
        try {
            Proyecto editado = construirDesdeFormulario(seleccionado, "ACTUALIZAR_PROYECTO");
            service.actualizar(editado);
            AlertaUtil.info("Proyecto actualizado", "Se actualizó '" + editado.getNombreProyecto() + "'.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_PROYECTO", ex);
        }
    }

    @FXML
    private void desactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla.");
            return;
        }
        try {
            service.desactivar(seleccionado.getIdProyecto());
            AlertaUtil.info("Proyecto desactivado", "El proyecto quedó inactivo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_PROYECTO", ex);
        }
    }

    @FXML
    private void reactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Marca 'Mostrar inactivos' y elige un proyecto.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdProyecto());
            AlertaUtil.info("Proyecto reactivado", "El proyecto volvió a estar activo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_PROYECTO", ex);
        }
    }

    @FXML
    private void eliminarFisico(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un proyecto", "Elige un proyecto de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar proyecto definitivamente",
                "¿Eliminar permanentemente el proyecto '" + seleccionado.getNombreProyecto() + "'?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdProyecto());
            AlertaUtil.info("Proyecto eliminado", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_PROYECTO", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        lblModoFormulario.setText("Registro de Proyecto");
        txtFiltroCliente.clear();
        cmbCliente.setValue(null);
        txtNombreProyecto.clear();
        dpInicio.setValue(LocalDate.now());
        dpFin.setValue(null);
        cmbEstadoProyecto.setValue("Planeado");
        txtCosto.clear();
        tblProyectos.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(Proyecto p) {
        seleccionado = p;
        lblModoFormulario.setText("Editando Proyecto ID #" + p.getIdProyecto());
        txtFiltroCliente.clear();
        ComboBuscadorUtil.seleccionarPorPredicado(cmbCliente, c -> c.getIdCliente() == p.getIdCliente());
        txtNombreProyecto.setText(p.getNombreProyecto());
        dpInicio.setValue(p.getFechaInicio());
        dpFin.setValue(p.getFechaEstimadaFin());
        cmbEstadoProyecto.setValue(p.getEstado());
        txtCosto.setText(p.getCostoTotal().toPlainString());
    }

    private Proyecto construirDesdeFormulario(Proyecto base, String op) throws Exception {
        Cliente cli = cmbCliente.getValue();
        if (cli == null) {
            throw new com.mycompany.soptec.it.excepciones.ValidacionException(MODULO, op,
                    "Selecciona un cliente de la lista desplegable.");
        }
        base.asociarCliente(cli);
        base.setNombreProyecto(txtNombreProyecto.getText());
        base.setFechaInicio(dpInicio.getValue());
        base.setFechaEstimadaFin(dpFin.getValue());
        base.setEstado(cmbEstadoProyecto.getValue());
        base.setCostoTotal(Validador.parsearDecimalNoNegativo(txtCosto.getText(), "Costo total", MODULO, op));
        return base;
    }
}
