package Presentacion;

import Datos.PersistenciaException;
import LogicaNegocio.Advertencia;
import LogicaNegocio.Alquiler;
import LogicaNegocio.Equipamiento;
import LogicaNegocio.EstadoAlquiler;
import LogicaNegocio.MotivoAcceso;
import LogicaNegocio.Reserva;
import LogicaNegocio.Socio;
import LogicaNegocio.TipoEquipamiento;
import LogicaNegocio.TipoMembresia;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class Mensajes {

    private Mensajes() {

    }

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String fecha(LocalDate fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    private static String dinero(double monto) {
        return "$" + String.format("%.2f", monto);
    }
    // GENERALES

    public static final String DESPEDIDA = "Hasta luego.";
    public static final String PEDIR_OPCION = "Opcion: ";
    public static final String OPCION_INVALIDA = "Opcion invalida. Elija una de las opciones del menu.";
    public static final String DATO_VACIO = "Este dato no puede quedar vacio.";
    public static final String NUMERO_INVALIDO = "Ingrese un numero valido (por ejemplo 1500 o 1500.50).";
    public static final String ENTERO_POSITIVO = "Ingrese un numero entero mayor a 0.";
    public static final String PORCENTAJE_INVALIDO = "El porcentaje tiene que estar entre 0 y 100.";
    public static final String FECHA_INVALIDA = "Fecha invalida. Use el formato dd/MM/yyyy (por ejemplo 30/10/2026).";

    public static String menuPrincipal() {
        String n = System.lineSeparator();
        return n + "=== CENTRO DEPORTIVO - GESTION DE SOCIOS ===" + n
                + "--- Socios ---" + n
                + "1. Registrar socio" + n
                + "2. Consultar socio por DNI" + n
                + "3. Listar socios" + n
                + "--- Equipamiento ---" + n
                + "4. Registrar equipamiento" + n
                + "5. Listar equipamiento" + n
                + "6. Alquilar equipamiento" + n
                + "7. Devolver equipamiento" + n
                + "8. Ver alquileres" + n
                + "--- Actividades ---" + n
                + "9. Reservar actividad" + n
                + "10. Ver reservas de una actividad" + n
                + "0. Salir";
    }

    // SOCIOS

    public static final String PEDIR_DNI = "DNI: ";
    public static final String PEDIR_NOMBRE = "Nombre: ";
    public static final String PEDIR_APELLIDO = "Apellido: ";
    public static final String PEDIR_EMAIL = "Email: ";
    public static final String PEDIR_TELEFONO = "Telefono: ";
    public static final String PEDIR_TIPO = "Tipo de socio (1=Estandar, 2=VIP, 3=Becado): ";
    public static final String PEDIR_ADICIONAL_VIP = "Adicional por zonas exclusivas: ";
    public static final String PEDIR_DESCUENTO = "Porcentaje de descuento (0 a 100): ";

    public static final String TIPO_INVALIDO = "Tipo de socio invalido. Se cancelo el registro.";
    public static final String SIN_SOCIOS = "Todavia no hay socios registrados.";
    public static final String ENCABEZADO_LISTADO = "--- Socios registrados (ordenados por DNI) ---";
    public static final String ERROR_DNI_INVALIDO = "El DNI no puede estar vacio.";

    public static String socioRegistrado(Socio s) {
        return "Socio registrado correctamente: " + s.getApellido() + ", " + s.getNombre()
                + " (DNI " + s.getDni() + ", " + tipoMembresia(s.getTipoMembresia()) + ").";
    }

    public static String fichaSocio(Socio s) {
        String n = System.lineSeparator();
        String sancion = s.isSancionado() ? "Si (" + s.getMotivoSancion() + ")" : "No";
        String texto = "--- Ficha del socio ---" + n
                + "Tipo de membresia: " + tipoMembresia(s.getTipoMembresia()) + n
                + "DNI: " + s.getDni() + n
                + "Nombre: " + s.getApellido() + ", " + s.getNombre() + n
                + "Email: " + s.getEmail() + n
                + "Telefono: " + s.getTelefono() + n
                + "Cuota mensual: " + dinero(s.calcularCuotaMensual()) + n
                + "Activo: " + (s.isActivo() ? "Si" : "No") + n
                + "Cuotas al dia: " + (s.isCuotasAlDia() ? "Si" : "No") + n
                + "Sancionado: " + sancion + n
                + "Recargo pendiente (proxima factura): " + dinero(s.getRecargoPendiente());
        List<Advertencia> advertencias = s.getAdvertencias();
        if (advertencias.isEmpty()) {
            return texto + n + "Advertencias: ninguna";
        }
        texto += n + "Advertencias:";
        for (Advertencia a : advertencias) {
            texto += n + "  - " + advertencia(a);
        }
        return texto;
    }

    // Tipo de socio
    public static String tipoMembresia(TipoMembresia tipo) {
        switch (tipo) {
            case ESTANDAR:
                return "Estandar";
            case VIP:
                return "VIP";
            case BECADO:
                return "Becado";
            default:
                return "Desconocido";
        }
    }

    public static String advertencia(Advertencia a) {
        return fecha(a.getFecha()) + " - Devolucion tardia de " + a.getNombreEquipamiento() + " ("
                + a.getCodigoEquipamiento() + "): " + a.getDiasDeAtraso() + " dia(s) de atraso, recargo "
                + dinero(a.getRecargo());
    }

    public static String lineaListado(Socio s) {
        return s.getDni() + " - " + s.getApellido() + ", " + s.getNombre()
                + " [" + tipoMembresia(s.getTipoMembresia()) + "] cuota " + dinero(s.calcularCuotaMensual());
    }

    public static String errorDniDuplicado(String dni) {
        return "No se pudo registrar: ya existe un socio con el DNI " + dni + ".";
    }

    public static String errorSocioNoEncontrado(String dni) {
        return "No existe ningun socio con el DNI " + dni + ".";
    }

    // EQUIPAMIENTO Y ALQUILERES

    public static final String PEDIR_CODIGO_EQUIPO = "Codigo del equipamiento (ej. RQ-01): ";
    public static final String PEDIR_NOMBRE_EQUIPO = "Nombre (ej. Raqueta de tenis): ";
    public static final String PEDIR_TIPO_EQUIPO = "Tipo de equipamiento (1=Raqueta, 2=Locker, 3=Pelota, 4=Otro): ";
    public static final String TIPO_EQUIPO_INVALIDO = "Tipo invalido. Elija una opcion del 1 al 4.";
    public static final String PEDIR_DIAS_PRESTAMO = "Dias de prestamo: ";
    public static final String PEDIR_RECARGO_DIA = "Recargo por cada dia de atraso: ";
    public static final String PEDIR_DNI_SOCIO = "DNI del socio: ";
    public static final String PEDIR_CODIGO_ALQUILER = "Codigo del equipamiento: ";
    public static final String PEDIR_CODIGO_DEVOLUCION = "Codigo del equipamiento a devolver: ";
    public static final String PEDIR_FECHA_DEVOLUCION = "Fecha de devolucion (dd/MM/yyyy, Enter = hoy): ";

    public static final String SIN_EQUIPAMIENTO = "Todavia no hay equipamiento registrado.";
    public static final String ENCABEZADO_EQUIPAMIENTO = "--- Equipamiento (ordenado por codigo) ---";
    public static final String SIN_ALQUILERES = "Todavia no hay alquileres registrados.";
    public static final String ENCABEZADO_ALQUILERES = "--- Alquileres registrados ---";
    public static final String DEVUELTO_EN_TERMINO = "Devuelto en termino. Sin recargo.";
    public static final String ERROR_FECHA_DEVOLUCION = "La fecha de devolucion no puede ser anterior a la del alquiler.";

    public static String equipamientoRegistrado(Equipamiento e) {
        return "Equipamiento registrado correctamente: " + e.getNombre() + " (" + e.getCodigo() + ").";
    }

    public static String lineaEquipamiento(Equipamiento e) {
        return "[" + tipoEquipamiento(e.getTipo()) + "] " + e.getCodigo() + " - " + e.getNombre()
                + " | Prestamo: " + e.getDiasPrestamo() + " dia(s)"
                + " | Recargo por dia: " + dinero(e.getRecargoPorDia())
                + " | " + (e.isDisponible() ? "Disponible" : "Alquilado");
    }

    public static String alquilerRegistrado(Alquiler a) {
        String n = System.lineSeparator();
        return "Alquiler registrado: #" + a.getNumero() + " - " + a.getEquipamiento().getNombre()
                + " (" + a.getEquipamiento().getCodigo() + ") para el DNI " + a.getDniSocio() + "." + n
                + "Fecha de alquiler: " + fecha(a.getFechaAlquiler())
                + " | Fecha limite de devolucion: " + fecha(a.getFechaLimite());
    }

    public static String devolucionRegistrada(Alquiler a) {
        return "Devolucion registrada: " + a.getEquipamiento().getNombre() + " ("
                + a.getEquipamiento().getCodigo() + ") del DNI " + a.getDniSocio() + ".";
    }

    public static String devolucionTardia(Alquiler a) {
        String n = System.lineSeparator();
        return "DEVOLUCION TARDIA: " + a.diasDeAtraso(a.getFechaDevolucion()) + " dia(s) de atraso." + n
                + "Recargo de " + dinero(a.getRecargo())
                + " se aplicara en la siguiente factura. Se registro una advertencia al socio.";
    }

    // Estado del alquiler
    public static String estadoAlquiler(EstadoAlquiler estado) {
        switch (estado) {
            case DEVUELTO:
                return "DEVUELTO";
            case VENCIDO:
                return "VENCIDO";
            default:
                return "ACTIVO";
        }
    }

    // Tipo de equipamiento
    public static String tipoEquipamiento(TipoEquipamiento tipo) {
        switch (tipo) {
            case RAQUETA:
                return "Raqueta";
            case LOCKER:
                return "Locker";
            case PELOTA:
                return "Pelota";
            default:
                return "Otro";
        }
    }

    public static String lineaAlquiler(Alquiler a, LocalDate hoy) {
        String linea = "#" + a.getNumero() + " | DNI " + a.getDniSocio() + " | "
                + a.getEquipamiento().getNombre() + " (" + a.getEquipamiento().getCodigo() + ")"
                + " | Desde " + fecha(a.getFechaAlquiler())
                + " | Limite " + fecha(a.getFechaLimite())
                + " | " + estadoAlquiler(a.getEstado(hoy));
        if (a.isDevuelto()) {
            linea += " el " + fecha(a.getFechaDevolucion()) + " | Recargo " + dinero(a.getRecargo());
        }
        return linea;
    }

    public static String errorEquipamientoDuplicado(String codigo) {
        return "No se pudo registrar: ya existe un equipamiento con el codigo " + codigo + ".";
    }

    public static String errorEquipamientoNoEncontrado(String codigo) {
        return "No existe ningun equipamiento con el codigo " + codigo + ".";
    }

    public static String errorEquipamientoNoDisponible(String codigo, String nombre) {
        return "El equipamiento " + nombre + " (" + codigo + ") ya esta alquilado y no esta disponible.";
    }

    public static String errorAccesoDenegado(MotivoAcceso motivo, String motivoSancion) {
        return "Alquiler rechazado: " + motivoRechazoAcceso(motivo, motivoSancion);
    }

    // Traduce Socio.getMotivoAcceso() a texto (la sancion lleva su propio motivo)
    public static String motivoRechazoAcceso(MotivoAcceso motivo, String motivoSancion) {
        switch (motivo) {
            case SOCIO_DE_BAJA:
                return "El socio esta dado de baja.";
            case CUOTAS_IMPAGAS:
                return "El socio tiene cuotas impagas.";
            case SANCION_VIGENTE:
                return "Sancion vigente: " + motivoSancion;
            default:
                return "Acceso permitido.";
        }
    }

    public static String errorAlquilerVencido(String dni) {
        return "Alquiler rechazado: el socio con DNI " + dni + " tiene equipamiento vencido sin devolver.";
    }

    public static String errorSinAlquilerActivo(String codigo) {
        return "El equipamiento " + codigo + " no tiene ningun alquiler activo para devolver.";
    }

    // RESERVAS

    public static final String PEDIR_ACTIVIDAD = "Actividad (ej. Natacion): ";
    public static final String PEDIR_HORARIO = "Horario (ej. Lunes 18:00): ";
    public static final String ENCABEZADO_RESERVAS = "--- Reservas de la actividad ---";

    public static String reservaRegistrada(Reserva r) {
        return "Reserva registrada: " + r.getActividad() + " - " + r.getHorario() + " (DNI " + r.getDniSocio() + ").";
    }

    public static String errorReservaDuplicada(Reserva r) {
        return "No se pudo reservar: el socio con DNI " + r.getDniSocio() + " ya tiene una reserva en "
                + r.getActividad() + " para " + r.getHorario() + ".";
    }

    public static String sinReservas(String actividad) {
        return "La actividad " + actividad + " todavia no tiene reservas.";
    }

    public static String lineaReserva(Reserva r) {
        return r.getActividad() + " - " + r.getHorario() + " (DNI " + r.getDniSocio() + ")";
    }

    // ERRORES DE ARCHIVOS
    public static String errorArchivoNoEncontrado(String archivo) {
        return "No se pudo leer el archivo " + archivo + ". Verifique que exista y que no este en uso.";
    }

    public static String errorPersistencia(PersistenciaException e) {
        switch (e.getMotivo()) {
            case ESCRITURA:
                return "No se pudieron guardar los datos en el archivo " + e.getArchivo()
                        + ". Verifique que haya espacio y permisos de escritura.";
            case FORMATO_INVALIDO:
                return "El archivo " + e.getArchivo()
                        + " esta danado o no tiene el formato esperado. Revise o elimine ese archivo.";
            default:
                return "No se pudo leer el archivo " + e.getArchivo()
                        + ". Verifique que exista y que no este en uso.";
        }
    }
}
