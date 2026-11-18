package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.util.AlertaUtil;
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

/**
 * Controlador CRUD del modulo Materiales.
 */
public class MaterialesController implements Initializable {

    private static final String MODULO = "Materiales";
    private final MaterialService service = new MaterialService();

    @FXML private CheckBox chkIncluirInactivos;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtConsultarId;
    @FXML private TableView<Material> tblMateriales;
    @FXML private TableColumn<Material, String> colId;
    @FXML private TableColumn<Material, String> colDescripcion;
    @FXML private TableColumn<Material, String> colPrecio;
    @FXML private TableColumn<Material, String> colExistencias;
    @FXML private TableColumn<Material, String> colEstadoReg;

    @FXML private Label lblModoFormulario;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencias;

    private Material seleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdMaterial())));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcionMaterial()));
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getPrecioCosto().toPlainString()));
        colExistencias.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getExistencias())));
        colEstadoReg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstadoTexto()));

        tblMateriales.getSelectionModel().selectedItemProperty().addListener((obs, oldV, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });

        cargarTabla(null);
    }

    @FXML
    private void cargarTabla(ActionEvent event) {
        try {
            tblMateriales.setItems(FXCollections.observableArrayList(
                    service.listar(chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "LISTAR_MATERIALES", ex);
        }
    }

    @FXML
    private void buscar(ActionEvent event) {
        try {
            tblMateriales.setItems(FXCollections.observableArrayList(
                    service.buscar(txtBuscar.getText(), chkIncluirInactivos.isSelected())));
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_MATERIALES", ex);
        }
    }

    @FXML
    private void consultarPorId(ActionEvent event) {
        try {
            int id = Validador.parsearIdPositivo(txtConsultarId.getText(), "ID de material", MODULO, "CONSULTAR_POR_ID");
            Material m = service.consultarPorId(id);
            if (m == null) {
                AlertaUtil.advertencia("No encontrado", "No existe un material con el ID " + id + ".");
                return;
            }
            cargarEnFormulario(m);
            AlertaUtil.info("Material encontrado",
                    "Descripción: " + m.getDescripcionMaterial()
                    + "\nPrecio costo: $" + m.getPrecioCosto()
                    + "\nExistencias: " + m.getExistencias()
                    + "\nEstado: " + m.getEstadoTexto());
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CONSULTAR_POR_ID", ex);
        }
    }

    @FXML
    private void guardar(ActionEvent event) {
        try {
            Material nuevo = construirDesdeFormulario(new Material(), "CREAR_MATERIAL");
            Material creado = service.crear(nuevo);
            AlertaUtil.info("Material registrado", "Se guardó '" + creado.getDescripcionMaterial() + "'.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "CREAR_MATERIAL", ex);
        }
    }

    @FXML
    private void actualizar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Elige un material de la tabla para actualizarlo.");
            return;
        }
        try {
            Material editado = construirDesdeFormulario(seleccionado, "ACTUALIZAR_MATERIAL");
            service.actualizar(editado);
            AlertaUtil.info("Material actualizado", "Se actualizó '" + editado.getDescripcionMaterial() + "'.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ACTUALIZAR_MATERIAL", ex);
        }
    }

    @FXML
    private void desactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Elige un material de la tabla.");
            return;
        }
        try {
            service.desactivar(seleccionado.getIdMaterial());
            AlertaUtil.info("Material desactivado", "El material quedó inactivo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "DESACTIVAR_MATERIAL", ex);
        }
    }

    @FXML
    private void reactivar(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Marca 'Mostrar inactivos' y elige un material.");
            return;
        }
        try {
            service.reactivar(seleccionado.getIdMaterial());
            AlertaUtil.info("Material reactivado", "El material volvió a estar activo.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "REACTIVAR_MATERIAL", ex);
        }
    }

    @FXML
    private void eliminarFisico(ActionEvent event) {
        if (seleccionado == null) {
            AlertaUtil.advertencia("Selecciona un material", "Elige un material de la tabla.");
            return;
        }
        if (!AlertaUtil.confirmar("Eliminar material definitivamente",
                "¿Eliminar permanentemente '" + seleccionado.getDescripcionMaterial() + "'?")) {
            return;
        }
        try {
            service.eliminarFisico(seleccionado.getIdMaterial());
            AlertaUtil.info("Material eliminado", "El registro fue eliminado de la base de datos.");
            limpiar(null);
            cargarTabla(null);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "ELIMINAR_FISICO_MATERIAL", ex);
        }
    }

    @FXML
    private void limpiar(ActionEvent event) {
        seleccionado = null;
        lblModoFormulario.setText("Registro de Material");
        txtDescripcion.clear();
        txtPrecio.clear();
        txtExistencias.clear();
        tblMateriales.getSelectionModel().clearSelection();
    }

    private void cargarEnFormulario(Material m) {
        seleccionado = m;
        lblModoFormulario.setText("Editando Material ID #" + m.getIdMaterial());
        txtDescripcion.setText(m.getDescripcionMaterial());
        txtPrecio.setText(m.getPrecioCosto().toPlainString());
        txtExistencias.setText(String.valueOf(m.getExistencias()));
    }

    private Material construirDesdeFormulario(Material base, String op) throws Exception {
        base.setDescripcionMaterial(txtDescripcion.getText());
        base.setPrecioCosto(Validador.parsearDecimalNoNegativo(txtPrecio.getText(), "Precio costo", MODULO, op));
        base.setExistencias(Validador.parsearEnteroNoNegativo(txtExistencias.getText(), "Existencias", MODULO, op));
        return base;
    }
}
