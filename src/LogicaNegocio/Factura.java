package LogicaNegocio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int numero;
    private final String dniSocio;
    private final TipoFactura tipo;
    private final double monto;
    private final LocalDate fecha;

    public Factura(int numero, String dniSocio, TipoFactura tipo, double monto, LocalDate fecha) {
        Objects.requireNonNull(dniSocio);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(fecha);
        if (monto < 0) {
            throw new IllegalArgumentException();
        }
        this.numero = numero;
        this.dniSocio = dniSocio;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
    }

    public int getNumero() {
        return numero;
    }

    public String getDniSocio() {
        return dniSocio;
    }

    public TipoFactura getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getAnio() {
        return fecha.getYear();
    }

    public int getMes() {
        return fecha.getMonthValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof Factura))
            return false;
        Factura other = (Factura) obj;
        return this.numero == other.numero;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.numero);
    }
}
