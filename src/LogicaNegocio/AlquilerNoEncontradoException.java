package LogicaNegocio;

public class AlquilerNoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String codigo;

    public AlquilerNoEncontradoException(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
