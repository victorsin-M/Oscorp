package com.mycompany.soptec.it;

import com.mycompany.soptec.it.dao.Conexion;
import com.mycompany.soptec.it.excepciones.ConexionBDException;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.ClienteTelefono;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.servicio.CitaService;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.ClienteTelefonoService;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.LevantamientoService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.ProveedorService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AppLogger;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion CRUD, validaciones, transacciones/rollback, integridad referencial y logs.
 */
public class CrudBaseIntegrationTest {

    private final ClienteService clienteService = new ClienteService();
    private final ClienteTelefonoService telefonoService = new ClienteTelefonoService();
    private final EmpleadoService empleadoService = new EmpleadoService();
    private final MaterialService materialService = new MaterialService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final ProyectoService proyectoService = new ProyectoService();
    private final CitaService citaService = new CitaService();
    private final LevantamientoService levantamientoService = new LevantamientoService();

    @AfterEach
    void restaurarConexion() {
        Conexion.restaurarConfiguracionPorDefecto();
    }

    @Test
    void flujoCompletoRelacionalYReglasDeIntegridad() throws SoptecException {
        String sufijo = String.valueOf(System.nanoTime() % 100000);
        String rfc = "TEST" + String.format("%06d", Integer.parseInt(sufijo) % 999999) + "AB1";
        Cliente cliente = null;
        Proyecto proyecto = null;
        Cita cita = null;
        Levantamiento lev = null;
        String telPrincipal = "3311223344";
        String telActualizado = "3399887766";

        try {
            // 1. Crear cliente con telefono inicial mediante transaccion
            Cliente nuevo = new Cliente(0, rfc, "Cliente Prueba " + sufijo,
                    "prueba" + sufijo + "@soptecit.com", "Av. Vallarta", "100", "Centro", "Jalisco",
                    "uploads/clientes/demo.png", true);
            cliente = clienteService.crearConTelefonoInicial(nuevo, telPrincipal, false);
            assertTrue(cliente.getIdCliente() > 0);

            // Consultar por ID y verificar imagen/campos
            Cliente consultado = clienteService.consultarPorId(cliente.getIdCliente());
            assertNotNull(consultado);
            assertEquals(rfc, consultado.getRfc());
            assertEquals("uploads/clientes/demo.png", consultado.getImagen());

            // 2. Validar duplicado de cliente
            Cliente duplicado = new Cliente(0, rfc, "Otro", "otro" + sufijo + "@soptecit.com",
                    null, null, null, null, null, true);
            assertThrows(ValidacionException.class, () -> clienteService.crear(duplicado));

            // 3. Consultar y actualizar telefono-cliente
            ClienteTelefono ct = telefonoService.consultarPorClave(cliente.getIdCliente(), telPrincipal);
            assertNotNull(ct);
            assertEquals(consultado.getNombreCliente(), ct.getNombreCliente());

            ClienteTelefono ctAct = new ClienteTelefono(cliente.getIdCliente(), telActualizado, true);
            telefonoService.actualizar(cliente.getIdCliente(), telPrincipal, ctAct);
            assertNotNull(telefonoService.consultarPorClave(cliente.getIdCliente(), telActualizado));

            // 4. Crear proyecto asociado al cliente
            Proyecto p = new Proyecto(0, "Proyecto Red " + sufijo, LocalDate.now(),
                    LocalDate.now().plusDays(15), "Planeado", new BigDecimal("25000.00"),
                    cliente.getIdCliente(), true, null, null);
            proyecto = proyectoService.crear(p);
            assertTrue(proyecto.getIdProyecto() > 0);
            assertEquals(cliente.getNombreCliente(), proyecto.getClienteNombre());

            // 5. Crear cita asociada al cliente
            Cita ci = new Cita(0, LocalDate.now().plusDays(1), LocalTime.of(10, 30), "Programada",
                    "Visita tecnica inicial", cliente.getIdCliente(), true, null, null);
            cita = citaService.crear(ci);
            assertTrue(cita.getIdCita() > 0);

            // 6. Crear levantamiento asociado al proyecto y cita opcional
            Levantamiento l = new Levantamiento(0, LocalDate.now(), "uploads/croquis/demo.png",
                    "24 nodos Cat6", proyecto.getIdProyecto(), cita.getIdCita(), true, null, null, null);
            lev = levantamientoService.crear(l);
            assertTrue(lev.getIdLevantamiento() > 0);
            assertEquals(proyecto.getNombreProyecto(), lev.getNombreProyecto());

            // 7. Reglas de integridad: no se puede eliminar cliente con relaciones ni proyecto con levantamiento
            final int idCliente = cliente.getIdCliente();
            final int idProyecto = proyecto.getIdProyecto();
            assertThrows(IntegridadReferencialException.class, () -> clienteService.desactivar(idCliente));
            assertThrows(IntegridadReferencialException.class, () -> clienteService.eliminarFisico(idCliente));
            assertThrows(IntegridadReferencialException.class, () -> proyectoService.desactivar(idProyecto));
            assertThrows(IntegridadReferencialException.class, () -> proyectoService.eliminarFisico(idProyecto));

            // 8. Baja logica y reactivacion en levantamiento
            levantamientoService.desactivar(lev.getIdLevantamiento());
            assertFalse(levantamientoService.consultarPorId(lev.getIdLevantamiento()).isActivo());
            levantamientoService.reactivar(lev.getIdLevantamiento());
            assertTrue(levantamientoService.consultarPorId(lev.getIdLevantamiento()).isActivo());

        } finally {
            if (lev != null && lev.getIdLevantamiento() > 0) {
                try { levantamientoService.eliminarFisico(lev.getIdLevantamiento()); } catch (Exception ignored) {}
            }
            if (cita != null && cita.getIdCita() > 0) {
                try { citaService.eliminarFisico(cita.getIdCita()); } catch (Exception ignored) {}
            }
            if (proyecto != null && proyecto.getIdProyecto() > 0) {
                try { proyectoService.eliminarFisico(proyecto.getIdProyecto()); } catch (Exception ignored) {}
            }
            if (cliente != null && cliente.getIdCliente() > 0) {
                try {
                    if (telefonoService.consultarPorClave(cliente.getIdCliente(), telActualizado) != null) {
                        telefonoService.eliminarFisico(cliente.getIdCliente(), telActualizado);
                    }
                    if (telefonoService.consultarPorClave(cliente.getIdCliente(), telPrincipal) != null) {
                        telefonoService.eliminarFisico(cliente.getIdCliente(), telPrincipal);
                    }
                } catch (Exception ignored) {}
                try { clienteService.eliminarFisico(cliente.getIdCliente()); } catch (Exception ignored) {}
            }
        }
    }

    @Test
    void crudEmpleadoMaterialYProveedor() throws SoptecException {
        String sufijo = String.valueOf(System.nanoTime() % 100000);
        Empleado emp = null;
        Material mat = null;
        Proveedor prov = null;

        try {
            // Empleado
            emp = empleadoService.crear(new Empleado(0, "Tecnico " + sufijo, "3312345678",
                    "Instalador", "emp" + sufijo + "@soptecit.com", "uploads/empleados/foto.png", true));
            assertTrue(emp.getIdEmpleado() > 0);
            emp.setPuesto("Supervisor");
            empleadoService.actualizar(emp);
            assertEquals("Supervisor", empleadoService.consultarPorId(emp.getIdEmpleado()).getPuesto());
            empleadoService.desactivar(emp.getIdEmpleado());
            assertFalse(empleadoService.consultarPorId(emp.getIdEmpleado()).isActivo());
            empleadoService.reactivar(emp.getIdEmpleado());

            // Material
            mat = materialService.crear(new Material(0, "Cable UTP Prueba " + sufijo,
                    new BigDecimal("1950.50"), 12, true));
            assertTrue(mat.getIdMaterial() > 0);
            mat.setExistencias(20);
            materialService.actualizar(mat);
            assertEquals(20, materialService.consultarPorId(mat.getIdMaterial()).getExistencias());
            assertThrows(ValidacionException.class, () -> materialService.crear(
                    new Material(0, "Cable UTP Prueba " + sufijo, new BigDecimal("10.00"), 1, true)));

            // Proveedor
            prov = proveedorService.crear(new Proveedor(0, "Proveedor Prueba " + sufijo,
                    "3344556677", "prov" + sufijo + "@soptecit.com", "uploads/proveedores/logo.png", true));
            assertTrue(prov.getIdProveedor() > 0);
            List<Proveedor> encontrados = proveedorService.buscar("Proveedor Prueba " + sufijo, false);
            assertEquals(1, encontrados.size());
        } finally {
            if (emp != null && emp.getIdEmpleado() > 0) {
                try { empleadoService.eliminarFisico(emp.getIdEmpleado()); } catch (Exception ignored) {}
            }
            if (mat != null && mat.getIdMaterial() > 0) {
                try { materialService.eliminarFisico(mat.getIdMaterial()); } catch (Exception ignored) {}
            }
            if (prov != null && prov.getIdProveedor() > 0) {
                try { proveedorService.eliminarFisico(prov.getIdProveedor()); } catch (Exception ignored) {}
            }
        }
    }

    @Test
    void rollbackTransaccionalYLogSinCredenciales() throws SoptecException {
        String sufijo = String.valueOf(System.nanoTime() % 100000);
        String correoUnico = "rollback" + sufijo + "@soptecit.com";
        String rfcUnico = "ROLL" + String.format("%06d", Integer.parseInt(sufijo) % 999999) + "XY1";

        Cliente c = new Cliente(0, rfcUnico, "Cliente Rollback " + sufijo,
                correoUnico, null, null, null, null, null, true);

        assertThrows(SoptecException.class,
                () -> clienteService.crearConTelefonoInicial(c, "3311112222", true));

        // Verificar que no quedo guardado ningun cliente por el rollback
        List<Cliente> hallados = clienteService.buscar(correoUnico, true);
        assertTrue(hallados.isEmpty(), "El cliente no debio persistirse tras el rollback");

        // Simular fallo de conexion y verificar recuperacion y log seguro
        Conexion.setSimularFalloConexion(true);
        assertThrows(ConexionBDException.class, () -> clienteService.listar(false));
        Conexion.setSimularFalloConexion(false);
        assertDoesNotThrow(() -> clienteService.listar(false));

        String log = AppLogger.leerContenidoCompleto();
        assertTrue(log.contains("TipoExcepcion=CONEXION_BD"));
        assertFalse(log.contains("password=secret"), "No debe contener credenciales reales");
    }
}
