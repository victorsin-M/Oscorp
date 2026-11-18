package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.servicio.CitaService;
import com.mycompany.soptec.it.servicio.ClienteService;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controlador CRUD del modulo Citas (relacionado con Cliente por combo descriptivo).
 */
public class CitasController implements Initializable {

    private static final String MODULO = "Citas";
    private final CitaService service = new CitaService();
    private final ClienteService clienteService = new ClienteService();

    @FXML private CheckBox chkIncluirInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtConsultarId;
    @FXML private TableView<Cita> tblCitas;
    @FXML private TableColumn<Cita, String> colId;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colCliente;
    @FXML private TableColumn<Cita, String> colEstadoCita;
    @FXML private TableColumn<Cita, String> colObs;
    @FXML private TableColumn<Cita, String> colActivo;

    @FXML private Label lblModoFormulario;
    @FXML private TextField txtFiltroCliente;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private DatePicker dpFechaCita;
    @FXML private ComboBox<String> cmbHora;
    @FXML private ComboBox<String> cmbEstadoCita;
    @FXML private TextArea txtObservaciones;

    private Cita seleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (int h = 8; h <= 19; h++) {
            cmbHora.getItems().add(String.format("%02d:00", h));
            cmbHora.getItems().add(String.format("%02d:30", h));
        }
        cmbHora.setValue("09:00");
        cmbEstadoCita.getItems().addAll("Programada", "Confirmada", "Atendida", "Cancelada");
        cmbEstadoCita.setValue("Programada");
        dpFechaCita.setValue(LocalDate.now());

        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdCita())));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaCita() != null ? d.getValue().getFechaCita().toString() : ""));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraCita() != null ? d.getValue().getHoraCita().toString() : ""));
        colCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClienteDescriptivo()));
        colEstadoCita.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoCita()));
        colObs.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getObservaciones() != null ? d.getValue().getObservaciones() : ""));
        colActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActivoTexto()));

        tblCitas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, nuevo) -> {
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
            tblCitas.setItems(FXCollections.observableArrayList(
                    service.listar(chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_CITAS", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblCitas.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_CITAS", ex);
        }
    }

    @FXML
    private void consultarPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarId.getText(), "ID de cita", MODULO, "CONSULTAR_POR_ID");
            Cita c = service.consultarPorId(id);
            if (c == null) {
                AlertaUtil.advertencia("No encontrada", "No existe una cita con el ID " + id + ".");
                return;
            }
            cargarEnFormulario(c);
            AlertaUtil.info("Cita encontrada",
                    "Cliente: " + c.getClienteDescriptivo()
                    + "\nFecha y hora: " + c.getFechaCita() + " " + c.getHoraCita()
                    + "\nEstado cita: " + c.getEstadoCita()
                    + "\nRegistro: " + c.getActivoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            Cita nueva = construirDesdeFormulario(new Cita(), "CREAR_CITA");
            Cita creada = service.crear(nueva);
            AlertaUtil.info("Cita agendada",
                    "Se registró la cita para " + creada.getClienteDescriptivo()
                    + " el " + creada.getFechaCita() + " a las " + creada.getHoraCita() + ".");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_CITA", ex);
        }
    }

    @FXML
    private void actualizar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona una cita", "Elige una cita de la tabla para actualizarla.");
            return;
        }
        try {
            Cita editada = construirDesdeFormulario(seleccionado, "ACTUALIZAR_CITA");
            service.actualizar(editada);
            AlertaUtil.info("Cita actualizada", "Se actualizó la cita correctamente.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_CITA", ex);
        }
    }

    @FXML
    private void desactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona una cita", "Elige una cita de la tabla.");
            return;
        }
        try {
            service.desactivar(seleccionado.getIdCita());
            AlertaUtil.info("Cita desactivada", "La cita quedó inactiva.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_CITA", ex);
        }
    }

    @FXML
    private void reactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona una cita", "Marca 'Mostrar inactivos' y elige una cita.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdCita());
            AlertaUtil.info("Cita reactivada", "La cita volvió a estar activa.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_CITA", ex);
        }
    }

    @FXML
    private void eliminarFisico(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona una cita", "Elige una cita de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar cita definitivamente",
                "¿Eliminar permanentemente la cita del " + seleccionado.getFechaCita() + "?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdCita());
            AlertaUtil.info("Cita eliminada", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_CITA", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        lblModoFormulario.setText("Registro de Cita");
        txtFiltroCliente.clear();
        cmbCliente.setValue(null);
        dpFechaCita.setValue(LocalDate.now());
        cmbHora.setValue("09:00");
        cmbHora.getEditor().setText("09:00");
        cmbEstadoCita.setValue("Programada");
        txtObservaciones.clear();
        tblCitas.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(Cita c) {
        seleccionado = c;
        lblModoFormulario.setText("Editando Cita ID #" + c.getIdCita());
        txtFiltroCliente.clear();
        ComboBuscadorUtil.seleccionarPorPredicado(cmbCliente, cli -> cli.getIdCliente() == c.getIdCliente());
        dpFechaCita.setValue(c.getFechaCita());
        String horaStr = c.getHoraCita() != null ? c.getHoraCita().toString().substring(0, 5) : "09:00";
        cmbHora.setValue(horaStr);
        cmbHora.getEditor().setText(horaStr);
        cmbEstadoCita.setValue(c.getEstadoCita());
        txtObservaciones.setText(c.getObservaciones() == null ? "" : c.getObservaciones());
    }

    private Cita construirDesdeFormulario(Cita base, String op) throws Exception {
        Cliente cli = cmbCliente.getValue();
        if (cli == null) {
            throw new com.mycompany.soptec.it.excepciones.ValidacionException(MODULO, op,
                    "Selecciona un cliente de la lista desplegable.");
        }
        base.setIdCliente(cli.getIdCliente());
        base.setNombreCliente(cli.getNombreCliente());
        base.setRfcCliente(cli.getRfc());
        base.setFechaCita(dpFechaCita.getValue());
        String horaTxt = cmbHora.getEditor().getText();
        if ((horaTxt == null || horaTxt.isBlank()) && cmbHora.getValue() != null) {
            horaTxt = cmbHora.getValue();
        }
        base.setHoraCita(Validador.parsearHora(horaTxt, MODULO, op));
        base.setEstadoCita(cmbEstadoCita.getValue());
        base.setObservaciones(txtObservaciones.getText());
        return base;
    }
}
