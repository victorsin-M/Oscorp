package soptecit.controladores;

import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.CitaService;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AppLogger;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import soptecit.util.Tablas;

public class InicioController implements Initializable {

    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private final ProyectoService proyectoService = new ProyectoService();
    private final CitaService citaService = new CitaService();
    private final MaterialService materialService = new MaterialService();
    private final ClienteService clienteService = new ClienteService();

    @FXML private Label lblSaludo;
    @FXML private Label lblProyectosActivos;
    @FXML private Label lblProyectosSub;
    @FXML private Label lblCitasHoy;
    @FXML private Label lblCitasSub;
    @FXML private Label lblPorCobrar;
    @FXML private Label lblClientesSub;
    @FXML private Label lblBajoStock;
    @FXML private TableView<String[]> tblProyectos;
    @FXML private VBox listaCitas;
    @FXML private VBox listaAlertas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int hora = LocalTime.now().getHour();
        String saludo = hora < 12 ? "¡Buen día" : (hora < 19 ? "¡Buenas tardes" : "¡Buenas noches");
        lblSaludo.setText(saludo + ", " + PrincipalController.getUsuarioActual() + "!");

        tblProyectos.setRowFactory(t -> {
            TableRow<String[]> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    PrincipalController.irASeguimientoConFiltro(fila.getItem()[0]);
                }
            });
            return fila;
        });

        cargarResumen();
    }

    @FXML
    private void refrescarPanel(ActionEvent event) {
        PrincipalController.refrescarConexionGlobal();
        cargarResumen();
    }

    private void cargarResumen() {
        try {
            List<Proyecto> proyectos = proyectoService.listar(false);
            List<Cita> citas = citaService.listar(false);
            List<Material> materiales = materialService.listar(false);
            List<Cliente> clientes = clienteService.listar(false);

            lblProyectosActivos.setText(String.valueOf(proyectos.size()));
            long enProceso = proyectos.stream()
                    .filter(p -> p.getEstado() != null && p.getEstado().toLowerCase().contains("proceso"))
                    .count();
            lblProyectosSub.setText(enProceso + " en proceso");

            long citasHoy = citas.stream()
                    .filter(c -> LocalDate.now().equals(c.getFechaCita()))
                    .count();
            lblCitasHoy.setText(String.valueOf(citas.size()));
            lblCitasSub.setText(citasHoy + " programadas para hoy");

            BigDecimal montoTotal = proyectos.stream()
                    .map(p -> p.getCostoTotal() != null ? p.getCostoTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblPorCobrar.setText(MONEDA.format(montoTotal));
            lblClientesSub.setText(clientes.size() + " clientes activos");

            long bajoStock = materiales.stream().filter(m -> m.getExistencias() <= 10).count();
            lblBajoStock.setText(String.valueOf(bajoStock));

            // Tabla de proyectos
            String[][] filas = new String[proyectos.size()][6];
            for (int i = 0; i < proyectos.size(); i++) {
                Proyecto p = proyectos.get(i);
                filas[i] = new String[]{
                    String.valueOf(p.getIdProyecto()),
                    p.getClienteNombre() != null ? p.getClienteNombre() : "Sin cliente",
                    p.getNombreProyecto(),
                    MONEDA.format(p.getCostoTotal() != null ? p.getCostoTotal() : BigDecimal.ZERO),
                    String.valueOf(porcentajePorEstado(p.getEstado())),
                    p.getEstado() != null ? p.getEstado() : "Planeado"
                };
            }
            Tablas.cargar(tblProyectos, filas);

            poblarProximasCitas(citas);
            poblarAlertasMaterial(materiales);
        } catch (Exception ex) {
            AppLogger.registrarExcepcion("Inicio", "InicioController", "CARGAR_RESUMEN", ex);
            Tablas.cargar(tblProyectos, new String[0][0]);
            mostrarEstadoVacio(listaCitas, "Sin conexión con MySQL para cargar citas.");
            mostrarEstadoVacio(listaAlertas, "Sin conexión con MySQL para cargar inventario.");
        }
    }

    private void poblarProximasCitas(List<Cita> citas) {
        listaCitas.getChildren().clear();
        if (citas.isEmpty()) {
            mostrarEstadoVacio(listaCitas, "No hay citas activas registradas.");
            return;
        }
        String[] barras = {"barra-color-azul", "barra-color-naranja", "barra-color-verde"};
        int limite = Math.min(4, citas.size());
        for (int i = 0; i < limite; i++) {
            Cita c = citas.get(i);
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.getStyleClass().add("item-lista");

            Rectangle barra = new Rectangle(4, 34);
            barra.getStyleClass().add(barras[i % barras.length]);

            VBox textos = new VBox(2);
            HBox.setHgrow(textos, Priority.ALWAYS);
            Label titulo = new Label(c.getNombreCliente() != null ? c.getNombreCliente() : "Cliente #" + c.getIdCliente());
            titulo.getStyleClass().add("item-titulo");
            String fechaStr = c.getFechaCita() != null ? c.getFechaCita().toString() : "";
            Label sub = new Label(fechaStr + " · " + (c.getEstadoCita() != null ? c.getEstadoCita() : ""));
            sub.getStyleClass().add("texto-tenue");
            textos.getChildren().addAll(titulo, sub);

            String horaStr = c.getHoraCita() != null ? c.getHoraCita().toString() : "--:--";
            if (horaStr.length() > 5) {
                horaStr = horaStr.substring(0, 5);
            }
            Label lblHora = new Label(horaStr);
            lblHora.getStyleClass().add("item-hora");

            item.getChildren().addAll(barra, textos, lblHora);
            listaCitas.getChildren().add(item);
        }
    }

    private void poblarAlertasMaterial(List<Material> materiales) {
        listaAlertas.getChildren().clear();
        int mostrados = 0;
        for (Material m : materiales) {
            if (m.getExistencias() <= 10 && mostrados < 4) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.getStyleClass().add("item-lista");

                VBox textos = new VBox(2);
                HBox.setHgrow(textos, Priority.ALWAYS);
                Label titulo = new Label(m.getDescripcionMaterial());
                titulo.getStyleClass().add("item-titulo");
                Label sub = new Label("Existencia actual: " + m.getExistencias() + " · Costo: " + MONEDA.format(m.getPrecioCosto()));
                sub.getStyleClass().add("texto-tenue");
                textos.getChildren().addAll(titulo, sub);

                boolean agotado = m.getExistencias() == 0;
                Label badge = new Label(agotado ? "Agotado" : "Bajo stock");
                badge.getStyleClass().addAll("badge", agotado ? "badge-peligro" : "badge-alerta");

                item.getChildren().addAll(textos, badge);
                listaAlertas.getChildren().add(item);
                mostrados++;
            }
        }
        if (mostrados == 0) {
            mostrarEstadoVacio(listaAlertas, "Todos los materiales tienen stock saludable (> 10).");
        }
    }

    private void mostrarEstadoVacio(VBox contenedor, String mensaje) {
        contenedor.getChildren().clear();
        Label lbl = new Label(mensaje);
        lbl.getStyleClass().add("texto-tenue");
        lbl.setWrapText(true);
        contenedor.getChildren().add(lbl);
    }

    public static int porcentajePorEstado(String estado) {
        if (estado == null) {
            return 10;
        }
        String e = estado.toLowerCase();
        if (e.contains("finaliz") || e.contains("cerrad") || e.contains("termin")) {
            return 100;
        }
        if (e.contains("revisi")) {
            return 85;
        }
        if (e.contains("proceso") || e.contains("curso")) {
            return 60;
        }
        if (e.contains("cancel")) {
            return 0;
        }
        return 20;
    }

    @FXML
    private void irACitas(ActionEvent event) {
        PrincipalController.irA("Citas");
    }

    @FXML
    private void irASeguimiento(ActionEvent event) {
        PrincipalController.irA("Seguimiento");
    }

    @FXML
    private void irACompras(ActionEvent event) {
        PrincipalController.irA("ComprasAlmacen");
    }
}
