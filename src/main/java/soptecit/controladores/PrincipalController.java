package soptecit.controladores;

import com.mycompany.soptec.it.dao.Conexion;
import com.mycompany.soptec.it.util.AppLogger;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import soptecit.util.Mensajes;

/**
 * Ventana principal estilizada de SOPTEC-IT.
 * Carga cada módulo en el panel central y controla la visibilidad por área y estado de MySQL.
 */
public class PrincipalController implements Initializable {

    @FXML private ToggleGroup grupoMenu;
    @FXML private ToggleButton btnInicio;
    @FXML private ToggleButton btnSeguimiento;
    @FXML private ToggleButton btnVentas;
    @FXML private ToggleButton btnCitas;
    @FXML private ToggleButton btnLevantamientos;
    @FXML private ToggleButton btnAdministracion;
    @FXML private ToggleButton btnCompras;
    @FXML private ToggleButton btnInstalaciones;
    @FXML private ToggleButton btnLogs;

    @FXML private Label lblIniciales;
    @FXML private Label lblUsuario;
    @FXML private Label lblArea;
    @FXML private Label lblTitulo;
    @FXML private Label lblRuta;
    @FXML private Label lblFecha;
    @FXML private Button btnEstadoConexion;
    @FXML private TextField txtBuscarGlobal;
    @FXML private ScrollPane contenedor;

    private static PrincipalController instancia;
    private String usuarioActual = "Administrador";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instancia = this;

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM, yyyy", new Locale("es", "MX"));
        String fecha = LocalDate.now().format(formato);
        lblFecha.setText(fecha.substring(0, 1).toUpperCase() + fecha.substring(1));

        grupoMenu.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null && anterior != null) {
                grupoMenu.selectToggle(anterior);
            }
        });

        txtBuscarGlobal.setOnAction(e -> {
            String q = txtBuscarGlobal.getText();
            cargarModulo("Seguimiento", q);
        });

        actualizarBadgeConexion();
        cargarModulo("Inicio", null);
    }

    /** Configura usuario en sesión y aplica el filtro de módulos por área. */
    public void setUsuario(String usuario, String area) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario);
        lblIniciales.setText(usuario.length() >= 2 ? usuario.substring(0, 2).toUpperCase() : usuario.toUpperCase());
        boolean esAdmin = area == null || area.startsWith("Administrador");
        lblArea.setText(esAdmin ? "Acceso a todas las áreas" : "Área: " + area);

        mostrar(btnVentas, esAdmin || "Ventas".equals(area));
        mostrar(btnCitas, esAdmin || "Ventas".equals(area));
        mostrar(btnLevantamientos, esAdmin || "Levantamientos".equals(area));
        mostrar(btnAdministracion, esAdmin || "Administración".equals(area));
        mostrar(btnCompras, esAdmin || "Compras-Almacén".equals(area));
        mostrar(btnInstalaciones, esAdmin || "Instalaciones".equals(area));
        mostrar(btnLogs, true);

        cargarModulo("Inicio", null);
    }

    public static String getUsuarioActual() {
        return instancia != null ? instancia.usuarioActual : "Administrador";
    }

    public static void refrescarConexionGlobal() {
        if (instancia != null) {
            instancia.actualizarBadgeConexion();
        }
    }

    private void mostrar(ToggleButton boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }

    @FXML
    private void navegar(ActionEvent event) {
        ToggleButton boton = (ToggleButton) event.getSource();
        cargarModulo((String) boton.getUserData(), null);
        actualizarBadgeConexion();
    }

    @FXML
    private void verificarConexion(ActionEvent event) {
        boolean ok = actualizarBadgeConexion();
        if (ok) {
            Mensajes.info("Conexión activa", "Conexión verificada correctamente con la base de datos soptec_it.");
        } else {
            Mensajes.advertencia("Sin conexión con MySQL",
                    "No se pudo conectar con la base de datos soptec_it.\nRevisa que el servicio esté activo o desactiva la simulación en 'Logs y pruebas'.");
        }
    }

    public boolean actualizarBadgeConexion() {
        boolean ok = Conexion.verificarConexion();
        btnEstadoConexion.getStyleClass().removeAll("chip-conexion-ok", "chip-conexion-error");
        if (ok) {
            btnEstadoConexion.setText("MySQL: Conectado");
            btnEstadoConexion.getStyleClass().add("chip-conexion-ok");
        } else {
            btnEstadoConexion.setText("MySQL: Sin conexión");
            btnEstadoConexion.getStyleClass().add("chip-conexion-error");
        }
        return ok;
    }

    /** Cambia de módulo desde cualquier controlador: PrincipalController.irA("Citas"); */
    public static void irA(String modulo) {
        if (instancia != null) {
            instancia.cargarModulo(modulo, null);
        }
    }

    public static void irASeguimientoConFiltro(String filtro) {
        if (instancia != null) {
            instancia.cargarModulo("Seguimiento", filtro);
        }
    }

    private void cargarModulo(String modulo, String parametro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/soptecit/vistas/" + modulo + ".fxml"));
            Parent vista = loader.load();
            if ("Seguimiento".equals(modulo) && parametro != null && !parametro.isBlank()) {
                Object ctrl = loader.getController();
                if (ctrl instanceof SeguimientoController) {
                    ((SeguimientoController) ctrl).buscarDesdeExterno(parametro);
                }
            }
            contenedor.setContent(vista);
            contenedor.setVvalue(0);
            actualizarEncabezado(modulo);
        } catch (IOException ex) {
            AppLogger.registrarExcepcion("Navegacion", "PrincipalController", "CARGAR_MODULO_" + modulo, ex);
            Mensajes.error("No se pudo abrir el módulo",
                    "Ocurrió un problema al cargar " + modulo + ". El detalle fue guardado en soptec_errores.log.");
        }
    }

    private void actualizarEncabezado(String modulo) {
        switch (modulo) {
            case "Seguimiento":
                encabezado(btnSeguimiento, "Seguimiento de proyectos", "General  /  Consulta de avance en base de datos");
                break;
            case "Ventas":
                encabezado(btnVentas, "Ventas", "Áreas  /  Ventas  ·  Clientes, teléfonos, proyectos y facturación");
                break;
            case "Citas":
                encabezado(btnCitas, "Citas", "Áreas  /  Ventas  /  Agenda y CRUD de citas");
                break;
            case "Levantamientos":
                encabezado(btnLevantamientos, "Levantamientos", "Áreas  /  Levantamientos  ·  Visitas, croquis y relación con proyectos");
                break;
            case "Administracion":
                encabezado(btnAdministracion, "Administración", "Áreas  /  Administración  ·  Expedientes, cobranza, cierre y personal");
                break;
            case "ComprasAlmacen":
                encabezado(btnCompras, "Compras-Almacén", "Áreas  /  Compras-Almacén  ·  Materiales, movimientos, órdenes y proveedores");
                break;
            case "Instalaciones":
                encabezado(btnInstalaciones, "Instalaciones", "Áreas  /  Instalaciones  ·  Avance de obra y técnicos");
                break;
            case "LogsPruebas":
                encabezado(btnLogs, "Logs y pruebas de fallos", "Soporte  /  Auditoría de errores y pruebas de tolerancia a fallos");
                break;
            default:
                encabezado(btnInicio, "Inicio", "SOPTEC-IT  /  Panel general");
        }
    }

    private void encabezado(ToggleButton boton, String titulo, String ruta) {
        boton.setSelected(true);
        lblTitulo.setText(titulo);
        lblRuta.setText(ruta);
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        if (!Mensajes.confirmar("Cerrar sesión", "¿Deseas salir del sistema y volver al acceso?")) {
            return;
        }
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/soptecit/vistas/Login.fxml"));
            contenedor.getScene().setRoot(login);
            instancia = null;
        } catch (IOException ex) {
            AppLogger.registrarExcepcion("Autenticacion", "PrincipalController", "CERRAR_SESION", ex);
        }
    }

    @FXML
    private void salirAplicacion(ActionEvent event) {
        if (Mensajes.confirmar("Cerrar la aplicación", "¿Deseas cerrar SOPTEC-IT?")) {
            Platform.exit();
        }
    }
}
