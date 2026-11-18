package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.servicio.EmpleadoService;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Controlador CRUD del modulo Empleados.
 */
public class EmpleadosController implements Initializable {

    private static final String MODULO = "Empleados";
    private final EmpleadoService service = new EmpleadoService();

    @FXML private CheckBox chkIncluirInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtConsultarId;
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, String> colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colPuesto;
    @FXML private TableColumn<Empleado, String> colTelefono;
    @FXML private TableColumn<Empleado, String> colCorreo;
    @FXML private TableColumn<Empleado, String> colEstadoReg;

    @FXML private Label lblModoFormulario;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cmbPuesto;
    @FXML private TextField txtCorreo;
    @FXML private ImageView imgEmpleado;
    @FXML private Label lblRutaImagen;

    private Empleado seleccionado;
    private String rutaImagenActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbPuesto.getItems().addAll("Administrador", "Agente de Ventas", "Técnico de Levantamiento",
                "Técnico Instalador", "Compras y Almacén");

        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdEmpleado())));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colPuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPuesto()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreo()));
        colEstadoReg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));

        tblEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, oldV, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });

        cargarTabla(null);
    }

    @FXML
    private void cargarTabla(ActionEvent event) {
        try {
            tblEmpleados.setItems(FXCollections.observableArrayList(
                    service.listar(chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_EMPLEADOS", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblEmpleados.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_EMPLEADOS", ex);
        }
    }

    @FXML
    private void consultarPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarId.getText(), "ID de empleado", MODULO, "CONSULTAR_POR_ID");
            Empleado emp = service.consultarPorId(id);
            if (emp == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un empleado con el ID " + id + ".");
                return;
            }
            cargarEnFormulario(emp);
            AlertaUtil.info("Empleado encontrado",
                    "Nombre: " + emp.getNombre()
                    + "\nPuesto: " + emp.getPuesto()
                    + "\nTeléfono: " + emp.getTelefono()
                    + "\nCorreo: " + emp.getCorreo()
                    + "\nEstado: " + emp.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void seleccionarImagen(ActionEvent event) {
        try {
            String ruta = GestorImagenes.elegirYGuardarImagen(
                    imgEmpleado.getScene().getWindow(), "Seleccionar fotografía de empleado", "empleados", MODULO);
            if (ruta != null) {
                rutaImagenActual = ruta;
                lblRutaImagen.setText(ruta);
                imgEmpleado.setImage(GestorImagenes.cargarImagen(ruta));
            }
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "SELECCIONAR_IMAGEN", ex);
        }
    }

    @FXML
    private void quitarImagen(ActionEvent event) {
        rutaImagenActual = null;
        lblRutaImagen.setText("Sin imagen seleccionada");
        imgEmpleado.setImage(null);
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            Empleado creado = service.crear(construirDesdeFormulario(new Empleado()));
            AlertaUtil.info("Empleado registrado", "Se guardó a " + creado.getNombre() + ".");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void actualizar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Elige un empleado de la tabla para actualizarlo.");
            return;
        }
        try {
            Empleado editado = construirDesdeFormulario(seleccionado);
            service.actualizar(editado);
            AlertaUtil.info("Empleado actualizado", "Se actualizó la información de " + editado.getNombre() + ".");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void desactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Elige un empleado de la tabla.");
            return;
        }
        try {
            service.desactivar(seleccionado.getIdEmpleado());
            AlertaUtil.info("Empleado desactivado", "El empleado quedó inactivo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void reactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Marca 'Mostrar inactivos' y elige un empleado.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdEmpleado());
            AlertaUtil.info("Empleado reactivado", "El empleado volvió a estar activo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_EMPLEADO", ex);
        }
    }

    @FXML
    private void eliminarFisico(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un empleado", "Elige un empleado de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar empleado definitivamente",
                "¿Eliminar permanentemente a " + seleccionado.getNombre() + "?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdEmpleado());
            AlertaUtil.info("Empleado eliminado", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_EMPLEADO", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        lblModoFormulario.setText("Registro de Empleado");
        txtNombre.clear();
        txtTelefono.clear();
        cmbPuesto.setValue(null);
        cmbPuesto.getEditor().clear();
        txtCorreo.clear();
        quitarImagen(null);
        tblEmpleados.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(Empleado e) {
        seleccionado = e;
        lblModoFormulario.setText("Editando Empleado ID #" + e.getIdEmpleado());
        txtNombre.setText(e.getNombre());
        txtTelefono.setText(e.getTelefono());
        cmbPuesto.setValue(e.getPuesto());
        cmbPuesto.getEditor().setText(e.getPuesto());
        txtCorreo.setText(e.getCorreo());
        rutaImagenActual = e.getImagen();
        if (rutaImagenActual != null && !rutaImagenActual.isBlank()) {
            lblRutaImagen.setText(rutaImagenActual);
            imgEmpleado.setImage(GestorImagenes.cargarImagen(rutaImagenActual));
        } else {
            quitarImagen(null);
        }
    }

    private Empleado construirDesdeFormulario(Empleado base) {
        base.setNombre(txtNombre.getText());
        base.setTelefono(txtTelefono.getText());
        String puesto = cmbPuesto.getEditor().getText();
        if ((puesto == null || puesto.isBlank()) && cmbPuesto.getValue() != null) {
            puesto = cmbPuesto.getValue();
        }
        base.setPuesto(puesto);
        base.setCorreo(txtCorreo.getText());
        base.setImagen(rutaImagenActual);
        return base;
    }
}
