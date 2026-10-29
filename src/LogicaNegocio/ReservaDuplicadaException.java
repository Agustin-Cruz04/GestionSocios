package LogicaNegocio;

public class ReservaDuplicadaException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Reserva reserva;

    public ReservaDuplicadaException(Reserva reserva) {
        this.reserva = reserva;
    }

    public Reserva getReserva() {
        return reserva;
    }
}
