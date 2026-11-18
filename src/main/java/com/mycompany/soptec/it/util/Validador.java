package com.mycompany.soptec.it.util;

import com.mycompany.soptec.it.excepciones.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Validaciones de campos obligatorios, formatos y reglas de negocio.
 */
public final class Validador {

    private static final Pattern PATRON_RFC = Pattern.compile("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private Validador() {
    }

    public static String requerido(String valor, String nombreCampo, String modulo, String operacion)
            throws ValidacionException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException(modulo, operacion,
                    "El campo '" + nombreCampo + "' es obligatorio. Por favor complétalo.");
        }
        return valor.trim();
    }

    public static String opcional(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    public static void longitudMaxima(String valor, int max, String nombreCampo, String modulo, String operacion)
            throws ValidacionException {
        if (valor != null && valor.trim().length() > max) {
            throw new ValidacionException(modulo, operacion,
                    "El campo '" + nombreCampo + "' no debe exceder " + max + " caracteres.");
        }
    }

    public static String validarRfc(String rfc, String modulo, String operacion) throws ValidacionException {
        String limpio = requerido(rfc, "RFC", modulo, operacion).toUpperCase();
        longitudMaxima(limpio, 15, "RFC", modulo, operacion);
        if (!PATRON_RFC.matcher(limpio).matches()) {
            throw new ValidacionException(modulo, operacion,
                    "El RFC ingresado no tiene un formato válido (ejemplo: XAXX010101000 o ABC150301AB2).");
        }
        return limpio;
    }

    public static String validarCorreo(String correo, String modulo, String operacion) throws ValidacionException {
        String limpio = requerido(correo, "Correo electrónico", modulo, operacion).toLowerCase();
        longitudMaxima(limpio, 100, "Correo electrónico", modulo, operacion);
        if (!PATRON_CORREO.matcher(limpio).matches()) {
            throw new ValidacionException(modulo, operacion,
                    "El correo electrónico '" + limpio + "' no tiene un formato válido.");
        }
        return limpio;
    }

    public static String validarTelefono(String telefono, String modulo, String operacion) throws ValidacionException {
        String limpio = requerido(telefono, "Teléfono", modulo, operacion);
        longitudMaxima(limpio, 20, "Teléfono", modulo, operacion);
        String soloDigitos = limpio.replaceAll("\\D", "");
        if (!PATRON_TELEFONO.matcher(limpio).matches() || soloDigitos.length() < 10) {
            throw new ValidacionException(modulo, operacion,
                    "El teléfono debe contener al menos 10 dígitos y solo caracteres válidos.");
        }
        return limpio;
    }

    public static BigDecimal parsearDecimalNoNegativo(String texto, String nombreCampo, String modulo, String operacion)
            throws ValidacionException {
        String limpio = requerido(texto, nombreCampo, modulo, operacion).replace(",", "").replace("$", "").trim();
        try {
            BigDecimal valor = new BigDecimal(limpio);
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidacionException(modulo, operacion,
                        "El campo '" + nombreCampo + "' no puede ser negativo.");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new ValidacionException(modulo, operacion,
                    "El campo '" + nombreCampo + "' debe ser un número válido.", ex);
        }
    }

    public static int parsearEnteroNoNegativo(String texto, String nombreCampo, String modulo, String operacion)
            throws ValidacionException {
        String limpio = requerido(texto, nombreCampo, modulo, operacion);
        try {
            int valor = Integer.parseInt(limpio);
            if (valor < 0) {
                throw new ValidacionException(modulo, operacion,
                        "El campo '" + nombreCampo + "' no puede ser negativo.");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new ValidacionException(modulo, operacion,
                    "El campo '" + nombreCampo + "' debe ser un número entero válido.", ex);
        }
    }

    public static int parsearIdPositivo(String texto, String nombreCampo, String modulo, String operacion)
            throws ValidacionException {
        String limpio = requerido(texto, nombreCampo, modulo, operacion);
        try {
            int id = Integer.parseInt(limpio);
            if (id <= 0) {
                throw new ValidacionException(modulo, operacion,
                        "El '" + nombreCampo + "' debe ser un número entero mayor a cero.");
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new ValidacionException(modulo, operacion,
                    "El '" + nombreCampo + "' debe ser un número entero válido.", ex);
        }
    }

    public static void validarRangoFechas(LocalDate fechaInicio, LocalDate fechaFin, String modulo, String operacion)
            throws ValidacionException {
        if (fechaInicio == null) {
            throw new ValidacionException(modulo, operacion, "La fecha de inicio es obligatoria.");
        }
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ValidacionException(modulo, operacion,
                    "La fecha estimada de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    public static LocalTime parsearHora(String texto, String modulo, String operacion) throws ValidacionException {
        String limpio = requerido(texto, "Hora de cita", modulo, operacion);
        try {
            return LocalTime.parse(limpio.length() == 5 ? limpio + ":00" : limpio);
        } catch (Exception ex) {
            throw new ValidacionException(modulo, operacion,
                    "La hora debe tener formato HH:mm (ejemplo: 09:30 o 14:00).", ex);
        }
    }
}
