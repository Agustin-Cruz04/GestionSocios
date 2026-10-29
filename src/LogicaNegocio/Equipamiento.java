package LogicaNegocio;

import java.io.Serializable;
import java.util.Objects;

public class Equipamiento implements Serializable, Comparable<Equipamiento> {

    // 2L: el campo tipo cambio de String a TipoEquipamiento (los archivos viejos ya no sirven)
    private static final long serialVersionUID = 2L;

    private final String codigo;
    private String nombre;
    private TipoEquipamiento tipo;
    private int diasPrestamo; // plazo para devolverlo
    private double recargoPorDia;
    private boolean disponible;

    public Equipamiento(String codigo, String nombre, TipoEquipamiento tipo, int diasPrestamo,
            double recargoPorDia) {
        Objects.requireNonNull(codigo);
        Objects.requireNonNull(tipo);
        if (codigo.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (diasPrestamo <= 0) {
            throw new IllegalArgumentException();
        }
        if (recargoPorDia < 0) {
            throw new IllegalArgumentException();
        }
        this.codigo = codigo.trim();
        this.nombre = nombre;
        this.tipo = tipo;
        this.diasPrestamo = diasPrestamo;
        this.recargoPorDia = recargoPorDia;
        this.disponible = true;
    }

    public void marcarAlquilado() {
        this.disponible = false;
    }

    public void marcarDisponible() {
        this.disponible = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoEquipamiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEquipamiento tipo) {
        this.tipo = tipo;
    }

    public int getDiasPrestamo() {
        return diasPrestamo;
    }

    public double getRecargoPorDia() {
        return recargoPorDia;
    }

    public boolean isDisponible() {
        return disponible;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Equipamiento other = (Equipamiento) obj;
        return Objects.equals(this.codigo, other.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.codigo);
    }

    @Override
    public int compareTo(Equipamiento otro) {
        if (otro == null)
            return 1;
        return this.codigo.compareTo(otro.codigo);
    }
}
