package LogicaNegocio;

public class EquipamientoNoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String codigo;

    public EquipamientoNoEncontradoException(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
