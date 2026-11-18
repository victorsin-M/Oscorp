package com.mycompany.soptec.it.controlador;

import com.mycompany.soptec.it.dao.Conexion;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controlador del visor de logs y simulador controlado de errores y recuperacion.
 */
public class LogsPruebasController implements Initializable {

    private static final String MODULO = "LogsPruebas";

    @FXML private CheckBox chkSimularDesconexion;
    @FXML private Label lblRutaLog;
    @FXML private TextArea txtLog;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblRutaLog.setText("Archivo: " + AppLogger.getRutaLog());
        chkSimularDesconexion.setSelected(Conexion.isSimularFalloConexion());
        cargarLog(null);
    }

    @FXML
    private void cargarLog(ActionEvent event) {
        String contenido = AppLogger.leerContenidoCompleto();
        txtLog.setText(contenido.isBlank() ? "(El archivo de log aún está vacío)" : contenido);
        txtLog.positionCaret(txtLog.getText().length());
    }

    @FXML
    private void limpiarLog(ActionEvent event) {
        if (!AlertaUtil.confirmar("Limpiar archivo de log",
                "¿Deseas vaciar el contenido actual del archivo de log?")) {
            return;
        }
        try {
            AppLogger.limpiarLog();
            cargarLog(null);
        } catch (IOException ex) {
            AlertaUtil.error("Error", "No se pudo limpiar el archivo de log: " + ex.getMessage());
        }
    }

    @FXML
    private void simularErrorValidacion(ActionEvent event) {
        try {
            Cliente invalido = new Cliente(0, "RFC_MAL", "", "correo-invalido", null, null, null, null, null, true);
            new ClienteService().crear(invalido);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "PRUEBA_VALIDACION", ex);
        } finally {
            cargarLog(null);
        }
    }

    @FXML
    private void simularRollback(ActionEvent event) {
        try {
            String sufijo = String.valueOf(System.currentTimeMillis() % 100000);
            Cliente prueba = new Cliente(0, "PRUE260101AB1", "Cliente Rollback " + sufijo,
                    "rollback" + sufijo + "@soptecit.com", null, null, null, null, null, true);
            new ClienteService().crearConTelefonoInicial(prueba, "3312345678", true);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "PRUEBA_ROLLBACK", ex);
        } finally {
            cargarLog(null);
        }
    }

    @FXML
    private void simularIntegridad(ActionEvent event) {
        IntegridadReferencialException ex = new IntegridadReferencialException(
                MODULO,
                "PRUEBA_INTEGRIDAD",
                "No es posible eliminar el cliente porque tiene proyectos, citas o teléfonos asociados.");
        AppLogger.registrarError(MODULO, "LogsPruebasController", "PRUEBA_INTEGRIDAD",
                "INTEGRIDAD_REFERENCIAL", "Prueba controlada de restriccion de llave foranea", ex);
        AlertaUtil.errorControlado(MODULO, "PRUEBA_INTEGRIDAD", ex);
        cargarLog(null);
    }

    @FXML
    private void toggleSimularDesconexion(ActionEvent event) {
        boolean activar = chkSimularDesconexion.isSelected();
        Conexion.setSimularFalloConexion(activar);
        if (activar) {
            try {
                Conexion.conectarStrict(MODULO, "PRUEBA_DESCONEXION");
            } catch (Exception ex) {
                AlertaUtil.errorControlado(MODULO, "PRUEBA_DESCONEXION", ex);
            }
        } else {
            AppLogger.registrarInfo(MODULO, "LogsPruebasController", "RESTAURAR_CONEXION",
                    "Se desactivo la simulacion de fallo de conexion y la aplicacion recupero la conectividad.");
            AlertaUtil.info("Conexión restaurada",
                    "Se desactivó la simulación de caída. La aplicación continúa operando normalmente.");
        }
        cargarLog(null);
    }
}
