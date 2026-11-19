package com.mycompany.soptec.it;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica que todas las vistas FXML, CSS y controladores carguen sin errores de inyeccion o enlace.
 */
public class FxmlSmokeTest {

    private static final String[] VISTAS = {
        // Vistas estilizadas integradas (Interfaces-SOPTEC_IT + CRUD MySQL)
        "/soptecit/vistas/Login.fxml",
        "/soptecit/vistas/Principal.fxml",
        "/soptecit/vistas/Inicio.fxml",
        "/soptecit/vistas/Seguimiento.fxml",
        "/soptecit/vistas/Ventas.fxml",
        "/soptecit/vistas/Citas.fxml",
        "/soptecit/vistas/Levantamientos.fxml",
        "/soptecit/vistas/Administracion.fxml",
        "/soptecit/vistas/ComprasAlmacen.fxml",
        "/soptecit/vistas/Instalaciones.fxml",
        "/soptecit/vistas/DetalleFactura.fxml",
        "/soptecit/vistas/LogsPruebas.fxml",
        // Vistas CRUD base originales (compatibilidad)
        "/fxml/MainCrud.fxml",
        "/fxml/ClientesView.fxml",
        "/fxml/TelefonosClienteView.fxml",
        "/fxml/EmpleadosView.fxml",
        "/fxml/MaterialesView.fxml",
        "/fxml/ProveedoresView.fxml",
        "/fxml/ProyectosView.fxml",
        "/fxml/CitasView.fxml",
        "/fxml/LevantamientosView.fxml",
        "/fxml/LogsPruebasView.fxml"
    };

    @Test
    void cargarTodasLasVistasFxmlSinErrores() throws Exception {
        CountDownLatch inicioLatch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        try {
            Platform.startup(inicioLatch::countDown);
        } catch (IllegalStateException yaIniciado) {
            inicioLatch.countDown();
        }
        assertTrue(inicioLatch.await(10, TimeUnit.SECONDS), "JavaFX Toolkit no inicio a tiempo");

        CountDownLatch fxmlLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                for (String ruta : VISTAS) {
                    FXMLLoader loader = new FXMLLoader(FxmlSmokeTest.class.getResource(ruta));
                    Parent root = loader.load();
                    assertNotNull(root, "La vista no debe ser nula: " + ruta);
                    assertNotNull(loader.getController(), "El controlador no debe ser nulo: " + ruta);
                }
            } catch (Throwable ex) {
                errorRef.set(ex);
            } finally {
                fxmlLatch.countDown();
            }
        });

        assertTrue(fxmlLatch.await(15, TimeUnit.SECONDS), "Tiempo agotado al cargar las vistas FXML");
        if (errorRef.get() != null) {
            fail("Error al cargar vista FXML: " + errorRef.get().getMessage(), errorRef.get());
        }
    }
}
