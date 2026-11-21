package soptecit.controladores;

import com.mycompany.soptec.it.dao.Conexion;
import com.mycompany.soptec.it.excepciones.ArchivoException;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.OrdenCompra;
import com.mycompany.soptec.it.modelo.OrdenCompraMaterial;
import com.mycompany.soptec.it.modelo.Pago;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.modelo.ProyectoMaterial;
import com.mycompany.soptec.it.servicio.CitaService;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.OrdenCompraService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controlador estilizado del visor de logs y simulador controlado de fallos y rollbacks multi-tabla.
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
            ArchivoException ae = new ArchivoException(MODULO, "LIMPIAR_LOG",
                    "No se pudo vaciar el archivo de log. Verifica los permisos de escritura de la carpeta logs.", ex);
            AppLogger.registrarExcepcion(MODULO, "LogsPruebasController", "LIMPIAR_LOG", ae);
            AlertaUtil.errorControlado(MODULO, "LIMPIAR_LOG", ae);
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
    private void simularErrorArchivo(ActionEvent event) {
        ArchivoException ex = new ArchivoException(
                MODULO,
                "PRUEBA_ALMACENAMIENTO_ARCHIVO",
                "No se pudo almacenar la imagen seleccionada porque el directorio de destino no tiene permisos de escritura.");
        AppLogger.registrarError(MODULO, "LogsPruebasController", "PRUEBA_ALMACENAMIENTO_ARCHIVO",
                "ARCHIVO", "Prueba controlada de error de almacenamiento de archivos e imagenes", ex);
        AlertaUtil.errorControlado(MODULO, "PRUEBA_ALMACENAMIENTO_ARCHIVO", ex);
        cargarLog(null);
    }

    @FXML
    private void simularRollback(ActionEvent event) {
        try {
            String sufijo = String.valueOf(System.currentTimeMillis() % 100000);
            Cliente prueba = new Cliente(0, "PRUE260101AB1", "Cliente Rollback " + sufijo,
                    "rollback" + sufijo + "@soptecit.com", null, null, null, null, null, true);
            new ClienteService().crearConTelefonoInicial(prueba, "3312345678", true);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "PRUEBA_ROLLBACK_CLIENTE", ex);
        } finally {
            cargarLog(null);
        }
    }

    @FXML
    private void simularRollbackCita(ActionEvent event) {
        try {
            List<Cliente> clientes = new ClienteService().listar(false);
            List<Proyecto> proyectos = new ProyectoService().listar(false);
            if (clientes.isEmpty() || proyectos.isEmpty()) {
                AlertaUtil.advertencia("Datos previos requeridos",
                        "Registra al menos un cliente y un proyecto activo para ejecutar la prueba de rollback de cita.");
                return;
            }
            Cita cita = new Cita(0, LocalDate.now().plusDays(1), LocalTime.of(10, 0),
                    "Programada", "Prueba rollback cita + levantamiento", clientes.get(0).getIdCliente(), true, null, null);
            Levantamiento lev = new Levantamiento(0, LocalDate.now().plusDays(1), null,
                    "Servicio relacionado de prueba", proyectos.get(0).getIdProyecto(), null, true, null, null, null);
            new CitaService().crearConLevantamientoInicial(cita, lev, true);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "PRUEBA_ROLLBACK_CITA", ex);
        } finally {
            cargarLog(null);
        }
    }

    @FXML
    private void simularRollbackVentaProyecto(ActionEvent event) {
        try {
            List<Cliente> clientes = new ClienteService().listar(false);
            List<Material> materiales = new MaterialService().listar(false);
            if (clientes.isEmpty() || materiales.isEmpty()) {
                AlertaUtil.advertencia("Datos previos requeridos",
                        "Registra al menos un cliente y un material activo con existencias para probar el rollback integral.");
                return;
            }
            Material mat = materiales.stream().filter(m -> m.getExistencias() > 0).findFirst().orElse(materiales.get(0));
            Proyecto p = new Proyecto(0, "Proyecto Rollback Integral", LocalDate.now(),
                    LocalDate.now().plusDays(15), "Planeado", new BigDecimal("15000.00"),
                    clientes.get(0).getIdCliente(), true, null, null);
            List<ProyectoMaterial> listaMat = mat.getExistencias() > 0
                    ? Collections.singletonList(new ProyectoMaterial(0, mat.getIdMaterial(), BigDecimal.ONE))
                    : Collections.emptyList();
            Pago anticipo = new Pago(0, 0, "Anticipo prueba rollback", new BigDecimal("5000.00"),
                    LocalDate.now(), "Parcial", "Transferencia", true, null, null, null);
            new ProyectoService().crearProyectoIntegral(p, listaMat, Collections.emptyList(), anticipo, true);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "PRUEBA_ROLLBACK_PROYECTO", ex);
        } finally {
            cargarLog(null);
        }
    }

    @FXML
    private void simularRollbackOrden(ActionEvent event) {
        try {
            List<Proyecto> proyectos = new ProyectoService().listar(false);
            List<Material> materiales = new MaterialService().listar(false);
            if (proyectos.isEmpty() || materiales.isEmpty()) {
                AlertaUtil.advertencia("Datos previos requeridos",
                        "Registra al menos un proyecto y un material activo para probar el rollback de orden de compra.");
                return;
            }
            OrdenCompra oc = new OrdenCompra();
            oc.setIdProyecto(proyectos.get(0).getIdProyecto());
            oc.setFechaOrden(LocalDate.now());
            oc.setTiempoEntrega("3 días");
            oc.setEstadoOrden("Solicitada");
            OrdenCompraMaterial partida = new OrdenCompraMaterial(0, materiales.get(0).getIdMaterial(), new BigDecimal("2"));
            new OrdenCompraService().crearConPartidas(oc, Collections.singletonList(partida), true);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "PRUEBA_ROLLBACK_ORDEN", ex);
        } finally {
            cargarLog(null);
        }
    }

    @FXML
    private void simularIntegridad(ActionEvent event) {
        IntegridadReferencialException ex = new IntegridadReferencialException(
                MODULO,
                "PRUEBA_INTEGRIDAD",
                "No es posible eliminar el registro porque tiene proyectos, citas, órdenes o pagos relacionados.");
        AppLogger.registrarError(MODULO, "LogsPruebasController", "PRUEBA_INTEGRIDAD",
                "INTEGRIDAD_REFERENCIAL", "Prueba controlada de restriccion de llave foranea", ex);
        AlertaUtil.errorControlado(MODULO, "PRUEBA_INTEGRIDAD", ex);
        cargarLog(null);
    }

    @FXML
    private void toggleSimularDesconexion(ActionEvent event) {
        boolean activar = chkSimularDesconexion.isSelected();
        Conexion.setSimularFalloConexion(activar);
        PrincipalController.refrescarConexionGlobal();
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
