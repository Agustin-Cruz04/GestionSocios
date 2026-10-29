package LogicaNegocio;

public class SocioConAlquilerVencidoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String dni;

    public SocioConAlquilerVencidoException(String dni) {
        this.dni = dni;
    }

    public String getDni() {
        return dni;
    }
}
