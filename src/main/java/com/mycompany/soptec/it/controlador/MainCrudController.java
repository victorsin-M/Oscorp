package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.dao.Conexion;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/**
 * Controlador principal de navegacion entre los 8 modulos CRUD y el visor de logs.
 */
public class MainCrudController implements Initializable {

    @FXML private ToggleGroup grupoMenu;
    @FXML private Label lblTituloModulo;
    @FXML private Label lblSubtituloModulo;
    @FXML private Label lblEstadoConexion;
    @FXML private ScrollPane contenedorModulo;

    private static final Map<String, String[]> TITULOS = Map.of(
            "ClientesView", new String[]{"1. Clientes", "Gestión de clientes, direcciones, imágenes y alta transaccional con teléfono"},
            "TelefonosClienteView", new String[]{"2. Teléfonos de Cliente", "Administración de teléfonos relacionados mediante selector descriptivo de cliente"},
            "EmpleadosView", new String[]{"3. Empleados", "Gestión de personal, puestos, correos e imagen de perfil"},
            "MaterialesView", new String[]{"4. Materiales", "Catálogo de materiales, costos unitarios y existencias"},
            "ProveedoresView", new String[]{"5. Proveedores", "Gestión de proveedores, contacto e imagen/logotipo"},
            "ProyectosView", new String[]{"6. Proyectos", "Gestión de proyectos vinculados a clientes mediante selector descriptivo"},
            "CitasView", new String[]{"7. Citas", "Agenda de citas relacionadas con clientes y control de estado"},
            "LevantamientosView", new String[]{"8. Levantamientos de Proyecto", "Registro técnico de visitas, croquis y relación con proyectos"},
            "LogsPruebasView", new String[]{"Logs y Pruebas de Fallos", "Auditoría de errores en soptec_errores.log y simulación controlada de excepciones"}
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        grupoMenu.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null && anterior != null) {
                grupoMenu.selectToggle(anterior);
            }
        });

        actualizarBadgeConexion();
        cargarVista("ClientesView");
    }

    @FXML
    private void navegar(ActionEvent event) {
        ToggleButton btn = (ToggleButton) event.getSource();
        String vista = (String) btn.getUserData();
        cargarVista(vista);
        actualizarBadgeConexion();
    }

    @FXML
    private void verificarConexion(ActionEvent event) {
        boolean ok = actualizarBadgeConexion();
        if (ok) {
            AlertaUtil.info("Conexión activa", "Conexión verificada correctamente con la base de datos soptec_it.");
        } else {
            AlertaUtil.advertencia("Sin conexión",
                    "No se pudo conectar con MySQL (soptec_it). Revisa que el servicio esté iniciado o desactiva el fallo simulado en 'Logs y Pruebas'.");
        }
    }

    public boolean actualizarBadgeConexion() {
        boolean ok = Conexion.verificarConexion();
        lblEstadoConexion.getStyleClass().removeAll("badge-ok", "badge-error");
        if (ok) {
            lblEstadoConexion.setText("MySQL: Conectado");
            lblEstadoConexion.getStyleClass().add("badge-ok");
        } else {
            lblEstadoConexion.setText("MySQL: Sin conexión");
            lblEstadoConexion.getStyleClass().add("badge-error");
        }
        return ok;
    }

    private void cargarVista(String nombreFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + nombreFxml + ".fxml"));
            Parent vista = loader.load();
            contenedorModulo.setContent(vista);
            contenedorModulo.setVvalue(0);
            String[] info = TITULOS.getOrDefault(nombreFxml, new String[]{nombreFxml, ""});
            lblTituloModulo.setText(info[0]);
            lblSubtituloModulo.setText(info[1]);
        } catch (IOException ex) {
            AppLogger.registrarExcepcion("Navegacion", "MainCrudController", "CARGAR_VISTA_" + nombreFxml, ex);
            AlertaUtil.error("No se pudo abrir el módulo",
                    "Ocurrió un problema al cargar la vista " + nombreFxml + ". El detalle fue guardado en el log.");
        }
    }
}
