package Presentacion;

import Datos.PersistenciaException;
import LogicaNegocio.MotivoAcceso;
import LogicaNegocio.Socio;

public final class Mensajes {

    private Mensajes() {

    }

    public static final String TITULO_MENU = "=== CENTRO DEPORTIVO - GESTION DE SOCIOS ===";
    public static final String OPCION_REGISTRAR = "1. Registrar socio";
    public static final String OPCION_CONSULTAR = "2. Consultar socio por DNI";
    public static final String OPCION_LISTAR = "3. Listar socios";
    public static final String OPCION_SALIR = "0. Salir";
    public static final String DESPEDIDA = "Hasta luego.";

    public static String menuPrincipal() {
        String n = System.lineSeparator();
        return n + TITULO_MENU + n + OPCION_REGISTRAR + n + OPCION_CONSULTAR + n + OPCION_LISTAR + n + OPCION_SALIR;
    }

    public static final String PEDIR_OPCION = "Opcion: ";
    public static final String PEDIR_DNI = "DNI: ";
    public static final String PEDIR_NOMBRE = "Nombre: ";
    public static final String PEDIR_APELLIDO = "Apellido: ";
    public static final String PEDIR_EMAIL = "Email: ";
    public static final String PEDIR_TELEFONO = "Telefono: ";
    public static final String PEDIR_TIPO = "Tipo de socio (1=Estandar, 2=VIP, 3=Becado): ";
    public static final String PEDIR_ADICIONAL_VIP = "Adicional por zonas exclusivas: ";
    public static final String PEDIR_DESCUENTO = "Porcentaje de descuento (0 a 100): ";

    public static final String OPCION_INVALIDA = "Opcion invalida. Elija una de las opciones del menu.";
    public static final String TIPO_INVALIDO = "Tipo de socio invalido. Se cancelo el registro.";
    public static final String DATO_VACIO = "Este dato no puede quedar vacio.";
    public static final String NUMERO_INVALIDO = "Ingrese un numero valido (por ejemplo 1500 o 1500.50).";
    public static final String PORCENTAJE_INVALIDO = "El porcentaje tiene que estar entre 0 y 100.";

    public static final String SIN_SOCIOS = "Todavia no hay socios registrados.";
    public static final String ENCABEZADO_LISTADO = "--- Socios registrados (ordenados por DNI) ---";

    public static String socioRegistrado(Socio s) {
        return "Socio registrado correctamente: " + s.getApellido() + ", " + s.getNombre()
                + " (DNI " + s.getDni() + ", " + s.getTipoMembresia() + ").";
    }

    public static String fichaSocio(Socio s) {
        String n = System.lineSeparator();
        String sancion = s.isSancionado() ? "Si (" + s.getMotivoSancion() + ")" : "No";
        return "--- Ficha del socio ---" + n
                + "Tipo de membresia: " + s.getTipoMembresia() + n
                + "DNI: " + s.getDni() + n
                + "Nombre: " + s.getApellido() + ", " + s.getNombre() + n
                + "Email: " + s.getEmail() + n
                + "Telefono: " + s.getTelefono() + n
                + "Cuota mensual: $" + String.format("%.2f", s.calcularCuotaMensual()) + n
                + "Activo: " + (s.isActivo() ? "Si" : "No") + n
                + "Cuotas al dia: " + (s.isCuotasAlDia() ? "Si" : "No") + n
                + "Sancionado: " + sancion;
    }

    public static String lineaListado(Socio s) {
        return s.getDni() + " - " + s.getApellido() + ", " + s.getNombre()
                + " [" + s.getTipoMembresia() + "] cuota $" + String.format("%.2f", s.calcularCuotaMensual());
    }

    public static final String ERROR_DNI_INVALIDO = "El DNI no puede estar vacio.";

    public static String errorDniDuplicado(String dni) {
        return "No se pudo registrar: ya existe un socio con el DNI " + dni + ".";
    }

    public static String errorSocioNoEncontrado(String dni) {
        return "No existe ningun socio con el DNI " + dni + ".";
    }

    public static String textoMotivoAcceso(MotivoAcceso motivo) {
        switch (motivo) {
            case DADO_DE_BAJA:
                return "El socio esta dado de baja.";
            case CUOTAS_IMPAGAS:
                return "El socio tiene cuotas impagas.";
            case SANCIONADO:
                return "El socio tiene una sancion vigente.";
            default:
                return "Acceso permitido.";
        }
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
