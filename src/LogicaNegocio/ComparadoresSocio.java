package LogicaNegocio;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public final class ComparadoresSocio {

    private ComparadoresSocio() {

    }

    private static final Collator COLLATOR = Collator.getInstance(Locale.forLanguageTag("es-AR"));

    static {
        COLLATOR.setStrength(Collator.PRIMARY);
    }

    private static int compararTexto(String a, String b) {
        return COLLATOR.compare(a, b);
    }

    public static final Comparator<Socio> POR_APELLIDO_NOMBRE = new Comparator<Socio>() {
        @Override
        public int compare(Socio a, Socio b) {
            int resultado = compararTexto(a.getApellido(), b.getApellido());
            if (resultado == 0) {
                resultado = compararTexto(a.getNombre(), b.getNombre());
            }
            if (resultado == 0) {
                resultado = a.getDni().compareTo(b.getDni());
            }
            return resultado;
        }
    };

    public static final Comparator<Socio> POR_CUOTA = new Comparator<Socio>() {
        @Override
        public int compare(Socio a, Socio b) {
            int resultado = Double.compare(a.calcularCuotaMensual(), b.calcularCuotaMensual());
            if (resultado == 0) {
                resultado = POR_APELLIDO_NOMBRE.compare(a, b);
            }
            return resultado;
        }
    };

    public static final Comparator<Socio> BUSCAR_POR_APELLIDO = new Comparator<Socio>() {
        @Override
        public int compare(Socio a, Socio b) {
            return compararTexto(a.getApellido(), b.getApellido());
        }
    };

    public static final Comparator<Socio> BUSCAR_POR_CUOTA = new Comparator<Socio>() {
        @Override
        public int compare(Socio a, Socio b) {
            return Double.compare(a.calcularCuotaMensual(), b.calcularCuotaMensual());
        }
    };
}
