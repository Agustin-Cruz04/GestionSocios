package LogicaNegocio;

public class SocioEstandar extends Socio {

    private static final long serialVersionUID = 1L;

    public SocioEstandar(String dni, String nombre, String apellido, String email, String telefono, double cuotaBase) {
        super(dni, nombre, apellido, email, telefono, cuotaBase);
    }

    @Override
    public double calcularCuotaMensual() {
        return cuotaBase;
    }

    @Override
    public TipoMembresia getTipoMembresia() {
        return TipoMembresia.ESTANDAR;
    }
}
