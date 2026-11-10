package LogicaNegocio;

import java.util.Comparator;

public class OcupacionActividad {

    private final String actividad;
    private final int reservas;

    public OcupacionActividad(String actividad, int reservas) {
        this.actividad = actividad;
        this.reservas = reservas;
    }

    public String getActividad() {
        return actividad;
    }

    public int getReservas() {
        return reservas;
    }

    public static final Comparator<OcupacionActividad> POR_OCUPACION = new Comparator<OcupacionActividad>() {
        @Override
        public int compare(OcupacionActividad a, OcupacionActividad b) {
            int resultado = Integer.compare(b.reservas, a.reservas);
            if (resultado == 0) {
                resultado = a.actividad.compareToIgnoreCase(b.actividad);
            }
            return resultado;
        }
    };
}
