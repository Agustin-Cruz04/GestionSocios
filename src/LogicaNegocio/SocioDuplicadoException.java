package LogicaNegocio;

public class SocioDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String dni;

    public SocioDuplicadoException(String dni) {
        this.dni = dni;
    }

    public String getDni() {
        return dni;
    }
}
