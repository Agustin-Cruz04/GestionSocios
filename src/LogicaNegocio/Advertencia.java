package LogicaNegocio;

import java.io.Serializable;
import java.time.LocalDate;

public class Advertencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final String codigoEquipamiento;
    private final String nombreEquipamiento;
    private final long diasDeAtraso;
    private final double recargo;

    public Advertencia(LocalDate fecha, String codigoEquipamiento, String nombreEquipamiento, long diasDeAtraso,
            double recargo) {
        this.fecha = fecha;
        this.codigoEquipamiento = codigoEquipamiento;
        this.nombreEquipamiento = nombreEquipamiento;
        this.diasDeAtraso = diasDeAtraso;
        this.recargo = recargo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getCodigoEquipamiento() {
        return codigoEquipamiento;
    }

    public String getNombreEquipamiento() {
        return nombreEquipamiento;
    }

    public long getDiasDeAtraso() {
        return diasDeAtraso;
    }

    public double getRecargo() {
        return recargo;
    }
}
