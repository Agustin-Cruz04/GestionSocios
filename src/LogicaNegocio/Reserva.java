package LogicaNegocio;

import java.io.Serializable;
import java.util.Objects;

public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String dniSocio;
    private final String actividad;
    private final String horario;

    public Reserva(String dniSocio, String actividad, String horario) {
        this.dniSocio = dniSocio;
        this.actividad = actividad;
        this.horario = horario;
    }

    public String getDniSocio() {
        return dniSocio;
    }

    public String getActividad() {
        return actividad;
    }

    public String getHorario() {
        return horario;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof Reserva))
            return false;
        Reserva other = (Reserva) obj;
        return Objects.equals(this.dniSocio, other.dniSocio)
                && Objects.equals(this.actividad, other.actividad)
                && Objects.equals(this.horario, other.horario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dniSocio, this.actividad, this.horario);
    }
}
