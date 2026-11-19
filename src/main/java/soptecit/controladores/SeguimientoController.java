package soptecit.controladores;

import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.ClienteTelefono;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.ClienteTelefonoService;
import com.mycompany.soptec.it.servicio.LevantamientoService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import soptecit.util.Mensajes;
import soptecit.util.Tablas;

/**
 * Consulta de avance de proyectos conectada a la base de datos MySQL (soptec_it).
 */
public class SeguimientoController implements Initializable {

    private static final String MODULO = "Seguimiento";
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final ProyectoService proyectoService = new ProyectoService();
    private final ClienteService clienteService = new ClienteService();
    private final ClienteTelefonoService telefonoService = new ClienteTelefonoService();
    private final LevantamientoService levantamientoService = new LevantamientoService();

    @FXML private ComboBox<Proyecto> cmbProyectos;
    @FXML private TextField txtFolioBuscar;
    @FXML private Label lblFolio;
    @FXML private Label lblCliente;
    @FXML private Label lblDescripcion;
    @FXML private Label lblEstadoProyecto;
    @FXML private ProgressBar pbAvance;
    @FXML private Label lblAvance;
    @FXML private Label lblFechaInicio;
    @FXML private Label lblFechaEntrega;
    @FXML private Label lblAreaActual;

    @FXML private Circle cirPaso1;
    @FXML private Circle cirPaso2;
    @FXML private Circle cirPaso3;
    @FXML private Circle cirPaso4;
    @FXML private Label lblFechaPaso1;
    @FXML private Label lblDescPaso1;
    @FXML private Label lblFechaPaso2;
    @FXML private Label lblDescPaso2;
    @FXML private Label lblEstadoPaso3;
    @FXML private Label lblDescPaso3;
    @FXML private Label lblEstadoPaso4;

    @FXML private Label lblTotalProyecto;
    @FXML private Label lblPagado;
    @FXML private Label lblSaldo;
    @FXML private ProgressBar pbPagos;
    @FXML private Label lblPorcentajePagado;

    @FXML private Label lblContacto;
    @FXML private Label lblTelefonoContacto;
    @FXML private Label lblCorreoContacto;
    @FXML private Label lblDireccionContacto;
    @FXML private TextArea txtNotasCliente;

    private Proyecto proyectoActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbProyectos.valueProperty().addListener((obs, a, nuevo) -> {
            if (nuevo != null) {
                mostrarProyecto(nuevo);
            }
        });
        cargarListaProyectos();
    }

    private void cargarListaProyectos() {
        try {
            List<Proyecto> lista = proyectoService.listar(false);
            cmbProyectos.setItems(FXCollections.observableArrayList(lista));
            if (!lista.isEmpty()) {
                cmbProyectos.getSelectionModel().selectFirst();
            }
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MODULO, "SeguimientoController", "CARGAR_PROYECTOS", ex);
        }
    }

    public void buscarDesdeExterno(String criterio) {
        txtFolioBuscar.setText(criterio);
        buscarProyecto(null);
    }

    @FXML
    private void buscarProyecto(ActionEvent event) {
        String texto = txtFolioBuscar.getText() == null ? "" : txtFolioBuscar.getText().trim();
        if (texto.isEmpty()) {
            Mensajes.info("Buscar proyecto", "Escribe el ID del proyecto, su nombre o el nombre del cliente.");
            return;
        }
        try {
            Proyecto encontrado = null;
            if (texto.matches("\\d+")) {
                encontrado = proyectoService.consultarPorId(Integer.parseInt(texto));
            }
            if (encontrado == null) {
                List<Proyecto> resultados = proyectoService.buscar(texto, true);
                if (!resultados.isEmpty()) {
                    encontrado = resultados.get(0);
                }
            }
            if (encontrado == null) {
                Mensajes.advertencia("Proyecto no encontrado",
                        "No se encontró ningún proyecto que coincida con '" + texto + "'.");
                return;
            }
            mostrarProyecto(encontrado);
        } catch (Exception ex) {
            AlertaUtil.errorControlado(MODULO, "BUSCAR_PROYECTO", ex);
        }
    }

    private void mostrarProyecto(Proyecto p) {
        this.proyectoActual = p;
        lblFolio.setText("PRY-" + String.format("%04d", p.getIdProyecto()));
        lblCliente.setText(p.getClienteNombre() != null ? p.getClienteNombre() : "Sin cliente");
        lblDescripcion.setText(p.getNombreProyecto() + (p.getClienteRfc() != null ? " · RFC " + p.getClienteRfc() : ""));

        String estado = p.getEstado() != null ? p.getEstado() : "Planeado";
        lblEstadoProyecto.setText(estado);
        lblEstadoProyecto.getStyleClass().setAll("label", "badge", Tablas.claseEstado(estado));

        int avance = InicioController.porcentajePorEstado(estado);
        pbAvance.setProgress(avance / 100.0);
        lblAvance.setText(avance + " %");

        lblFechaInicio.setText(p.getFechaInicio() != null ? p.getFechaInicio().toString() : "—");
        lblFechaEntrega.setText(p.getFechaEstimadaFin() != null ? p.getFechaEstimadaFin().toString() : "Por definir");
        lblAreaActual.setText(areaPorEstado(estado));

        lblFechaPaso1.setText(p.getFechaInicio() != null ? p.getFechaInicio().toString() : "—");
        lblDescPaso1.setText("Proyecto '" + p.getNombreProyecto() + "' registrado para " + p.getClienteDescriptivo() + ".");

        BigDecimal costo = p.getCostoTotal() != null ? p.getCostoTotal() : BigDecimal.ZERO;
        BigDecimal iva = costo.multiply(BigDecimal.valueOf(0.16));
        BigDecimal totalConIva = costo.add(iva);
        lblTotalProyecto.setText(MONEDA.format(costo));
        lblPagado.setText(MONEDA.format(iva));
        lblSaldo.setText(MONEDA.format(totalConIva));
        pbPagos.setProgress(avance / 100.0);
        lblPorcentajePagado.setText("Estado del proyecto: " + estado + " (" + avance + " % avance)");

        actualizarPasos(p, avance);
        cargarDatosClienteYLevantamiento(p);
    }

    private void actualizarPasos(Proyecto p, int avance) {
        cirPaso1.getStyleClass().setAll("paso-hecho");
        cirPaso2.getStyleClass().setAll(avance >= 30 ? "paso-hecho" : "paso-actual");
        cirPaso3.getStyleClass().setAll(avance >= 85 ? "paso-hecho" : (avance >= 50 ? "paso-actual" : "paso-pendiente"));
        cirPaso4.getStyleClass().setAll(avance >= 100 ? "paso-hecho" : (avance >= 85 ? "paso-actual" : "paso-pendiente"));
        lblEstadoPaso3.setText(avance + " %");
        lblEstadoPaso4.setText(avance >= 100 ? "Cerrado" : (avance >= 85 ? "En revisión" : "Pendiente"));
    }

    private void cargarDatosClienteYLevantamiento(Proyecto p) {
        try {
            Cliente cli = clienteService.consultarPorId(p.getIdCliente());
            if (cli != null) {
                lblContacto.setText(cli.getNombreCliente() + " (" + cli.getRfc() + ")");
                lblCorreoContacto.setText("Correo: " + (cli.getCorreo() != null ? cli.getCorreo() : "—"));
                String dir = cli.getDireccionResumen();
                lblDireccionContacto.setText("Dirección: " + (dir.isBlank() ? "Sin dirección registrada" : dir));

                List<ClienteTelefono> tels = telefonoService.listarPorCliente(cli.getIdCliente(), false);
                if (!tels.isEmpty()) {
                    lblTelefonoContacto.setText("Tel: " + tels.get(0).getTelefono()
                            + (tels.size() > 1 ? " (+" + (tels.size() - 1) + " más)" : ""));
                } else {
                    lblTelefonoContacto.setText("Tel: Sin teléfonos registrados");
                }
            }
            List<Levantamiento> levs = levantamientoService.buscar(p.getNombreProyecto(), false);
            Levantamiento match = levs.stream()
                    .filter(l -> l.getIdProyecto() == p.getIdProyecto())
                    .findFirst()
                    .orElse(null);
            if (match != null) {
                lblFechaPaso2.setText(match.getFechaVisita() != null ? match.getFechaVisita().toString() : "—");
                String obs = match.getObservacionesTecnicas();
                lblDescPaso2.setText("Levantamiento #" + match.getIdLevantamiento()
                        + (obs != null && !obs.isBlank() ? " · " + obs : " registrado con croquis."));
            } else {
                lblFechaPaso2.setText("Pendiente");
                lblDescPaso2.setText("Aún no se registra un levantamiento técnico para este proyecto.");
            }
        } catch (Exception ex) {
            AppLogger.registrarExcepcion(MODULO, "SeguimientoController", "CARGAR_DETALLE_CLIENTE", ex);
        }
    }

    private String areaPorEstado(String estado) {
        String e = estado.toLowerCase();
        if (e.contains("planead")) {
            return "Ventas / Levantamientos";
        }
        if (e.contains("proceso")) {
            return "Instalaciones";
        }
        if (e.contains("revisi") || e.contains("finaliz") || e.contains("cerrad")) {
            return "Administración";
        }
        return "General";
    }

    @FXML
    private void enviarActualizacion(ActionEvent event) {
        if (txtNotasCliente.getText().trim().isEmpty()) {
            Mensajes.error("Mensaje vacío", "Escribe la actualización que recibirá el cliente.");
            return;
        }
        AppLogger.registrarInfo(MODULO, "SeguimientoController", "ENVIAR_ACTUALIZACION",
                "Actualización registrada para " + (proyectoActual != null ? proyectoActual.getNombreProyecto() : "cliente")
                + ": " + txtNotasCliente.getText().trim());
        Mensajes.info("Actualización enviada", "Se registró y envió la actualización al contacto del cliente.");
        txtNotasCliente.clear();
    }

    @FXML
    private void exportarReporte(ActionEvent event) {
        if (proyectoActual == null) {
            Mensajes.advertencia("Sin proyecto", "Selecciona primero un proyecto para ver su resumen.");
            return;
        }
        Mensajes.info("Resumen del proyecto",
                "Folio: PRY-" + String.format("%04d", proyectoActual.getIdProyecto())
                + "\nProyecto: " + proyectoActual.getNombreProyecto()
                + "\nCliente: " + proyectoActual.getClienteDescriptivo()
                + "\nInicio: " + proyectoActual.getFechaInicio()
                + "\nEstado: " + proyectoActual.getEstado()
                + "\nCosto: " + MONEDA.format(proyectoActual.getCostoTotal()));
    }
}
