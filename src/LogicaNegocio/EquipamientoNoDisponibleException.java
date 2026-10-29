package LogicaNegocio;

public class EquipamientoNoDisponibleException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String nombre;

    public EquipamientoNoDisponibleException(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
}
