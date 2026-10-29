package LogicaNegocio;

// La fecha de devolucion es anterior a la fecha del alquiler
public class FechaInvalidaException extends Exception {

    private static final long serialVersionUID = 1L;

    public FechaInvalidaException() {
        super();
    }
}
