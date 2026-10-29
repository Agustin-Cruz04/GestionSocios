package LogicaNegocio;

public class EquipamientoDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String codigo;

    public EquipamientoDuplicadoException(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
