package com.mycompany.soptec.it;

import com.mycompany.soptec.it.dao.Conexion;
import com.mycompany.soptec.it.excepciones.ArchivoException;
import com.mycompany.soptec.it.excepciones.ConexionBDException;
import com.mycompany.soptec.it.excepciones.IntegridadReferencialException;
import com.mycompany.soptec.it.excepciones.OperacionBDException;
import com.mycompany.soptec.it.excepciones.SoptecException;
import com.mycompany.soptec.it.excepciones.ValidacionException;
import com.mycompany.soptec.it.modelo.Cita;
import com.mycompany.soptec.it.modelo.Cliente;
import com.mycompany.soptec.it.modelo.Empleado;
import com.mycompany.soptec.it.modelo.Levantamiento;
import com.mycompany.soptec.it.modelo.Material;
import com.mycompany.soptec.it.modelo.OrdenCompra;
import com.mycompany.soptec.it.modelo.OrdenCompraMaterial;
import com.mycompany.soptec.it.modelo.Pago;
import com.mycompany.soptec.it.modelo.Proveedor;
import com.mycompany.soptec.it.modelo.Proyecto;
import com.mycompany.soptec.it.modelo.ProyectoMaterial;
import com.mycompany.soptec.it.servicio.CitaService;
import com.mycompany.soptec.it.servicio.ClienteService;
import com.mycompany.soptec.it.servicio.EmpleadoService;
import com.mycompany.soptec.it.servicio.MaterialProveedorService;
import com.mycompany.soptec.it.servicio.MaterialService;
import com.mycompany.soptec.it.servicio.OrdenCompraService;
import com.mycompany.soptec.it.servicio.PagoService;
import com.mycompany.soptec.it.servicio.ProveedorService;
import com.mycompany.soptec.it.servicio.ProyectoEmpleadoService;
import com.mycompany.soptec.it.servicio.ProyectoMaterialService;
import com.mycompany.soptec.it.servicio.ProyectoService;
import com.mycompany.soptec.it.util.AlertaUtil;
import com.mycompany.soptec.it.util.AppLogger;
import com.mycompany.soptec.it.util.GestorImagenes;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas integrales de la Actividad 4.8:
 * CRUD de tablas relacionadas y relaciones M:N, transacciones/rollback multi-tabla,
 * diferenciacion de las 5 categorias de excepcion, gestion de imagenes y logs seguros.
 */
public class CrudRelacionesIntegrationTest {

    private final ClienteService clienteService = new ClienteService();
    private final EmpleadoService empleadoService = new EmpleadoService();
    private final MaterialService materialService = new MaterialService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final ProyectoService proyectoService = new ProyectoService();
    private final CitaService citaService = new CitaService();
    private final PagoService pagoService = new PagoService();
    private final ProyectoEmpleadoService peService = new ProyectoEmpleadoService();
    private final ProyectoMaterialService pmService = new ProyectoMaterialService();
    private final MaterialProveedorService mpService = new MaterialProveedorService();
    private final OrdenCompraService ocService = new OrdenCompraService();

    @AfterEach
    void restaurarEstado() {
        Conexion.restaurarConfiguracionPorDefecto();
        GestorImagenes.restaurarDirectorioPorDefecto();
    }

    @Test
    void crudCompletoTablasRelacionadasYMuchosAMuchos() throws SoptecException {
        String sufijo = String.valueOf(System.nanoTime() % 100000);
        String rfc = "REL" + String.format("%06d", Integer.parseInt(sufijo) % 999999) + "AB1";
        Cliente cli = null;
        Empleado emp = null;
        Material mat = null;
        Proveedor prov = null;
        Proyecto proy = null;
        Pago pago = null;
        OrdenCompra orden = null;

        try {
            cli = clienteService.crear(new Cliente(0, rfc, "Cliente Rel " + sufijo,
                    "rel" + sufijo + "@soptecit.com", "Av. Patria", "200", "Zapopan", "Jalisco", null, true));
            emp = empleadoService.crear(new Empleado(0, "Tecnico Rel " + sufijo, "3312345678",
                    "Técnico Instalador", "tecrel" + sufijo + "@soptecit.com", null, true));
            mat = materialService.crear(new Material(0, "Material Rel " + sufijo,
                    new BigDecimal("500.00"), 20, true));
            prov = proveedorService.crear(new Proveedor(0, "Proveedor Rel " + sufijo,
                    "3398765432", "provrel" + sufijo + "@soptecit.com", null, true));
            proy = proyectoService.crear(new Proyecto(0, "Proyecto Rel " + sufijo, LocalDate.now(),
                    LocalDate.now().plusDays(20), "En proceso", new BigDecimal("10000.00"),
                    cli.getIdCliente(), true, null, null));

            // 1. M:N material_proveedor
            mpService.asociar(mat.getIdMaterial(), prov.getIdProveedor());
            assertEquals(1, mpService.listarPorMaterial(mat.getIdMaterial()).size());
            final int idMat = mat.getIdMaterial();
            final int idProv = prov.getIdProveedor();
            assertThrows(ValidacionException.class, () -> mpService.asociar(idMat, idProv));

            // 2. M:N proyecto_empleado
            peService.asignar(proy.getIdProyecto(), emp.getIdEmpleado());
            assertEquals(1, peService.listarPorProyecto(proy.getIdProyecto()).size());
            final int idProy = proy.getIdProyecto();
            final int idEmp = emp.getIdEmpleado();
            assertThrows(ValidacionException.class, () -> peService.asignar(idProy, idEmp));

            // 3. M:N proyecto_material con descuento atomico de stock
            pmService.asignarConDescuentoStock(proy.getIdProyecto(), mat.getIdMaterial(), new BigDecimal("5"), false);
            assertEquals(15, materialService.consultarPorId(mat.getIdMaterial()).getExistencias());
            pmService.actualizarCantidad(proy.getIdProyecto(), mat.getIdMaterial(), new BigDecimal("6"));
            assertEquals(0, new BigDecimal("6").compareTo(
                    pmService.consultar(proy.getIdProyecto(), mat.getIdMaterial()).getCantidad()));

            // 4. 1:N pago
            pago = pagoService.crear(new Pago(0, proy.getIdProyecto(), "Anticipo 40%",
                    new BigDecimal("4000.00"), LocalDate.now(), "Parcial", "Transferencia", true, null, null, null));
            assertTrue(pago.getIdPago() > 0);
            assertEquals(0, new BigDecimal("4000.00").compareTo(
                    pagoService.sumarPagosActivosPorProyecto(proy.getIdProyecto())));
            pago.setEstadoCuenta("Pagado");
            pagoService.actualizar(pago);
            assertEquals("Pagado", pagoService.consultarPorId(pago.getIdPago()).getEstadoCuenta());

            // 5. orden_compra + M:N orden_compra_material + recepcion con aumento de stock
            OrdenCompra ocNueva = new OrdenCompra();
            ocNueva.setIdProyecto(proy.getIdProyecto());
            ocNueva.setIdProveedor(prov.getIdProveedor());
            ocNueva.setFechaOrden(LocalDate.now());
            ocNueva.setTiempoEntrega("3 días");
            ocNueva.setEstadoOrden("Solicitada");
            OrdenCompraMaterial partida = new OrdenCompraMaterial(0, mat.getIdMaterial(), new BigDecimal("4"));
            orden = ocService.crearConPartidas(ocNueva, Collections.singletonList(partida), false);
            assertTrue(orden.getIdOrdenCompra() > 0);
            assertEquals(0, new BigDecimal("2000.00").compareTo(orden.getTotalCompra()));
            assertEquals(1, orden.getPartidas().size());

            // Recibir orden: stock debe subir de 15 a 19
            OrdenCompra recibida = ocService.recibirOrden(orden.getIdOrdenCompra(), false);
            assertEquals("Recibida", recibida.getEstadoOrden());
            assertEquals(19, materialService.consultarPorId(mat.getIdMaterial()).getExistencias());

            // Regla de integridad: proyecto con relaciones no puede eliminarse fisicamente
            assertThrows(IntegridadReferencialException.class, () -> proyectoService.eliminarFisico(idProy));

            // Limpiar relaciones antes de eliminar entidades padre
            pmService.eliminar(proy.getIdProyecto(), mat.getIdMaterial(), false);
            peService.eliminar(proy.getIdProyecto(), emp.getIdEmpleado());
            mpService.eliminar(mat.getIdMaterial(), prov.getIdProveedor());
            ocService.eliminarFisico(orden.getIdOrdenCompra());
            orden = null;
            pagoService.eliminarFisico(pago.getIdPago());
            pago = null;
        } finally {
            if (orden != null && orden.getIdOrdenCompra() > 0) {
                try { ocService.eliminarFisico(orden.getIdOrdenCompra()); } catch (Exception ignored) {}
            }
            if (pago != null && pago.getIdPago() > 0) {
                try { pagoService.eliminarFisico(pago.getIdPago()); } catch (Exception ignored) {}
            }
            if (proy != null && mat != null) {
                try { pmService.eliminar(proy.getIdProyecto(), mat.getIdMaterial(), false); } catch (Exception ignored) {}
            }
            if (proy != null && emp != null) {
                try { peService.eliminar(proy.getIdProyecto(), emp.getIdEmpleado()); } catch (Exception ignored) {}
            }
            if (mat != null && prov != null) {
                try { mpService.eliminar(mat.getIdMaterial(), prov.getIdProveedor()); } catch (Exception ignored) {}
            }
            if (proy != null && proy.getIdProyecto() > 0) {
                try { proyectoService.eliminarFisico(proy.getIdProyecto()); } catch (Exception ignored) {}
            }
            if (cli != null && cli.getIdCliente() > 0) {
                try { clienteService.eliminarFisico(cli.getIdCliente()); } catch (Exception ignored) {}
            }
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
    void rollbacksMultiTablaCitaProyectoYOrdenCompra() throws SoptecException {
        String sufijo = String.valueOf(System.nanoTime() % 100000);
        String rfc = "RBK" + String.format("%06d", Integer.parseInt(sufijo) % 999999) + "XY1";
        Cliente cli = null;
        Proyecto proyBase = null;
        Material mat = null;

        try {
            cli = clienteService.crear(new Cliente(0, rfc, "Cliente Rollback " + sufijo,
                    "rbk" + sufijo + "@soptecit.com", null, null, null, null, null, true));
            proyBase = proyectoService.crear(new Proyecto(0, "Proy Base Rollback " + sufijo, LocalDate.now(),
                    LocalDate.now().plusDays(10), "Planeado", new BigDecimal("5000.00"),
                    cli.getIdCliente(), true, null, null));
            mat = materialService.crear(new Material(0, "Mat Rollback " + sufijo,
                    new BigDecimal("100.00"), 10, true));

            // 1. Rollback en Cita + Levantamiento: si falla el levantamiento, la cita no debe quedar registrada
            String marcaCita = "CITA_ROLLBACK_" + sufijo;
            Cita cita = new Cita(0, LocalDate.now().plusDays(2), LocalTime.of(11, 0),
                    "Programada", marcaCita, cli.getIdCliente(), true, null, null);
            Levantamiento lev = new Levantamiento(0, LocalDate.now().plusDays(2), null,
                    "Obs rollback", proyBase.getIdProyecto(), null, true, null, null, null);
            assertThrows(SoptecException.class, () -> citaService.crearConLevantamientoInicial(cita, lev, true));
            assertTrue(citaService.buscar(marcaCita, true).isEmpty(),
                    "La cita no debe quedar registrada tras rollback del levantamiento");

            // 2. Rollback en Proyecto/Venta + Material + Pago: nada debe quedar parcial ni bajar el stock
            String marcaProy = "PROY_INTEGRAL_ROLLBACK_" + sufijo;
            Proyecto pInt = new Proyecto(0, marcaProy, LocalDate.now(), LocalDate.now().plusDays(5),
                    "Planeado", new BigDecimal("8000.00"), cli.getIdCliente(), true, null, null);
            ProyectoMaterial pm = new ProyectoMaterial(0, mat.getIdMaterial(), new BigDecimal("3"));
            Pago pagoIni = new Pago(0, 0, "Anticipo Rollback " + sufijo, new BigDecimal("2000.00"),
                    LocalDate.now(), "Parcial", "Transferencia", true, null, null, null);
            assertThrows(SoptecException.class, () -> proyectoService.crearProyectoIntegral(
                    pInt, Collections.singletonList(pm), Collections.emptyList(), pagoIni, true));
            assertTrue(proyectoService.buscar(marcaProy, true).isEmpty(),
                    "El proyecto no debe persistir tras rollback");
            assertEquals(10, materialService.consultarPorId(mat.getIdMaterial()).getExistencias(),
                    "El stock no debe disminuir tras rollback");
            assertTrue(pagoService.buscar("Anticipo Rollback " + sufijo, true).isEmpty(),
                    "El pago inicial no debe quedar registrado tras rollback");

            // 3. Rollback en Orden de Compra + Partidas
            String marcaOrden = "ENTREGA_RBK_" + sufijo;
            OrdenCompra oc = new OrdenCompra();
            oc.setIdProyecto(proyBase.getIdProyecto());
            oc.setFechaOrden(LocalDate.now());
            oc.setTiempoEntrega(marcaOrden);
            oc.setEstadoOrden("Solicitada");
            OrdenCompraMaterial ocm = new OrdenCompraMaterial(0, mat.getIdMaterial(), new BigDecimal("2"));
            assertThrows(SoptecException.class,
                    () -> ocService.crearConPartidas(oc, Collections.singletonList(ocm), true));
            assertTrue(ocService.buscar(marcaOrden, true).isEmpty(),
                    "La orden de compra no debe persistir tras rollback de partidas");
        } finally {
            if (proyBase != null && proyBase.getIdProyecto() > 0) {
                try { proyectoService.eliminarFisico(proyBase.getIdProyecto()); } catch (Exception ignored) {}
            }
            if (cli != null && cli.getIdCliente() > 0) {
                try { clienteService.eliminarFisico(cli.getIdCliente()); } catch (Exception ignored) {}
            }
            if (mat != null && mat.getIdMaterial() > 0) {
                try { materialService.eliminarFisico(mat.getIdMaterial()); } catch (Exception ignored) {}
            }
        }
    }

    @Test
    void diferenciarCincoCategoriasExcepcionYValidacionDeImagenes() throws Exception {
        // 1. Verificar titulos diferenciados y ausencia de SQL en mensajes al usuario
        assertEquals("Datos inválidos o incompletos",
                AlertaUtil.tituloPara(new ValidacionException("Test", "OP", "Campo requerido")));
        assertEquals("Sin conexión con la base de datos",
                AlertaUtil.tituloPara(new ConexionBDException("Test", "OP", "Sin enlace")));
        assertEquals("Operación bloqueada por registros relacionados",
                AlertaUtil.tituloPara(new IntegridadReferencialException("Test", "OP", "Tiene hijos")));
        assertEquals("Problema con el archivo de imagen",
                AlertaUtil.tituloPara(new ArchivoException("Test", "OP", "Sin permisos")));
        assertEquals("Error al procesar la operación",
                AlertaUtil.tituloPara(new OperacionBDException("Test", "OP", "Error SQL controlado")));

        String msgInesperado = AlertaUtil.mensajeUsuario(new RuntimeException("SELECT * FROM cliente WHERE password=123"));
        assertFalse(msgInesperado.contains("SELECT"), "Nunca debe exponer consultas SQL al usuario");

        // 2. Validar rechazo de archivo de texto renombrado a .png y guardado de PNG real
        Path tempDir = Files.createTempDirectory("soptec_img_test");
        GestorImagenes.configurarDirectorioBase(tempDir);
        File falsoPng = tempDir.resolve("falso.png").toFile();
        Files.writeString(falsoPng.toPath(), "esto no es una imagen");
        assertThrows(ValidacionException.class, () -> GestorImagenes.guardarImagen(falsoPng, "clientes", "Test"));

        byte[] cabeceraPng = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        File validoPng = tempDir.resolve("valido.png").toFile();
        Files.write(validoPng.toPath(), cabeceraPng);
        String rutaRel = GestorImagenes.guardarImagen(validoPng, "evidencias", "Test");
        assertNotNull(rutaRel);
        assertTrue(GestorImagenes.existeImagen(rutaRel));

        // 3. Sanitizacion en log
        assertEquals("usuario=root, password=***", AppLogger.sanitizar("usuario=root, password=miClaveSecreta123"));
    }
}
