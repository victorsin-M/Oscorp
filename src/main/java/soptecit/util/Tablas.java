package soptecit.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Utilidades para tablas estilizadas de SOPTEC-IT.
 * Soporta tanto tablas genéricas con modelos (Cliente, Proyecto, Cita, etc.)
 * como tablas basadas en String[].
 *
 * Columnas con styleClass "col-estado" se pintan como insignia de color.
 * Columnas con styleClass "col-avance" se pintan como barra de progreso (0-100).
 */
public final class Tablas {

    private Tablas() {
    }

    @SuppressWarnings("unchecked")
    public static void cargar(TableView<String[]> tabla, String[][] filas) {
        for (int i = 0; i < tabla.getColumns().size(); i++) {
            final int indice = i;
            TableColumn<String[], String> columna = (TableColumn<String[], String>) tabla.getColumns().get(i);
            columna.setCellValueFactory(c -> new SimpleStringProperty(
                    indice < c.getValue().length ? c.getValue()[indice] : ""));
            if (columna.getStyleClass().contains("col-estado")) {
                columna.setCellFactory(c -> crearCeldaEstado());
            } else if (columna.getStyleClass().contains("col-avance")) {
                columna.setCellFactory(c -> crearCeldaAvance());
            }
        }
        tabla.setItems(FXCollections.observableArrayList(filas));
        tabla.setPlaceholder(new Label("No hay registros para mostrar"));
    }

    /** Devuelve la clase CSS de la insignia según el texto del estado. */
    public static String claseEstado(String estado) {
        if (estado == null) {
            return "badge-neutro";
        }
        String e = estado.toLowerCase();
        if (contiene(e, "inactiv", "vencid", "retras", "cancel", "rechaz", "agotad", "bajo stock", "detenid", "sin conexión")) {
            return "badge-peligro";
        }
        if (contiene(e, "pendiente", "planead", "programad", "revisión", "revision", "por surtir", "parcial", "borrador", "reprogram")) {
            return "badge-alerta";
        }
        if (contiene(e, "activ", "pagad", "complet", "finaliz", "termin", "aprobad", "en tiempo", "confirm",
                "disponible", "atendid", "recibid", "entrada", "cerrad", "autorizad", "conectado")) {
            return "badge-exito";
        }
        if (contiene(e, "salida")) {
            return "badge-naranja";
        }
        if (contiene(e, "proceso", "curso", "enviad", "en sitio", "instalaci", "solicitad", "camino")) {
            return "badge-info";
        }
        return "badge-neutro";
    }

    private static boolean contiene(String texto, String... palabras) {
        for (String p : palabras) {
            if (texto.contains(p)) {
                return true;
            }
        }
        return false;
    }

    /** Celda con insignia (badge) reutilizable en cualquier TableColumn tipada. */
    public static <T> TableCell<T, String> crearCeldaEstado() {
        return new TableCell<T, String>() {
            private final Label insignia = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null || item.isEmpty()) {
                    setGraphic(null);
                    return;
                }
                insignia.setText(item);
                insignia.getStyleClass().setAll("label", "badge", claseEstado(item));
                setGraphic(insignia);
            }
        };
    }

    /** Celda con barra de progreso (0-100) reutilizable en cualquier TableColumn tipada. */
    public static <T> TableCell<T, String> crearCeldaAvance() {
        return new TableCell<T, String>() {
            private final ProgressBar barra = new ProgressBar();
            private final Label texto = new Label();
            private final HBox caja = new HBox(8, barra, texto);

            {
                caja.setAlignment(Pos.CENTER_LEFT);
                barra.setMaxWidth(Double.MAX_VALUE);
                barra.setPrefHeight(10);
                HBox.setHgrow(barra, Priority.ALWAYS);
                texto.setMinWidth(38);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null || item.isEmpty()) {
                    setGraphic(null);
                    return;
                }
                double valor;
                try {
                    valor = Double.parseDouble(item.replace("%", "").trim());
                } catch (NumberFormatException ex) {
                    valor = 0;
                }
                barra.setProgress(Math.max(0, Math.min(1, valor / 100.0)));
                barra.getStyleClass().removeAll("progreso-exito", "progreso-alerta");
                if (valor >= 100) {
                    barra.getStyleClass().add("progreso-exito");
                } else if (valor < 35) {
                    barra.getStyleClass().add("progreso-alerta");
                }
                texto.setText((int) valor + " %");
                setGraphic(caja);
            }
        };
    }
}
