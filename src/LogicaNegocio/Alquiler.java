package LogicaNegocio;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Alquiler implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int numero;
    private final String dniSocio;
    private final Equipamiento equipamiento;
    private final LocalDate fechaAlquiler;
    private final LocalDate fechaLimite;
    private LocalDate fechaDevolucion; // null mientras no se devuelva
    private double recargo;

    public Alquiler(int numero, String dniSocio, Equipamiento equipamiento, LocalDate fechaAlquiler) {
        this.numero = numero;
        this.dniSocio = dniSocio;
        this.equipamiento = equipamiento;
        this.fechaAlquiler = fechaAlquiler;
        this.fechaLimite = fechaAlquiler.plusDays(equipamiento.getDiasPrestamo());
        this.fechaDevolucion = null;
        this.recargo = 0;
    }

    public boolean isDevuelto() {
        return fechaDevolucion != null;
    }

    // Vencido
    public boolean isVencido(LocalDate hoy) {
        return !isDevuelto() && hoy.isAfter(fechaLimite);
    }

    public long diasDeAtraso(LocalDate fechaEntrega) {
        long dias = ChronoUnit.DAYS.between(fechaLimite, fechaEntrega);
        return dias > 0 ? dias : 0;
    }

    public double calcularRecargo(LocalDate fechaEntrega) {
        return diasDeAtraso(fechaEntrega) * equipamiento.getRecargoPorDia();
    }

    public void registrarDevolucion(LocalDate fechaEntrega, double recargoAplicado) {
        this.fechaDevolucion = fechaEntrega;
        this.recargo = recargoAplicado;
    }

    public void anularDevolucion() {
        this.fechaDevolucion = null;
        this.recargo = 0;
    }

    public EstadoAlquiler getEstado(LocalDate hoy) {
        if (isDevuelto())
            return EstadoAlquiler.DEVUELTO;
        if (isVencido(hoy))
            return EstadoAlquiler.VENCIDO;
        return EstadoAlquiler.ACTIVO;
    }

    public int getNumero() {
        return numero;
    }

    public String getDniSocio() {
        return dniSocio;
    }

    public Equipamiento getEquipamiento() {
        return equipamiento;
    }

    public LocalDate getFechaAlquiler() {
        return fechaAlquiler;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public double getRecargo() {
        return recargo;
    }
}
