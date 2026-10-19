package LogicaNegocio;

public class SocioNoEncontradoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String dni;

    public SocioNoEncontradoException(String dni) {
        this.dni = dni;
    }

    public String getDni() {
        return dni;
    }
}
