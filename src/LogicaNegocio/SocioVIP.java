package LogicaNegocio;

public class SocioVIP extends Socio {

    private static final long serialVersionUID = 1L;

    private double adicionalZonasExclusivas;

    public SocioVIP(String dni, String nombre, String apellido, String email, String telefono, double cuotaBase,
            double adicionalZonasExclusivas) {
        super(dni, nombre, apellido, email, telefono, cuotaBase);
        this.adicionalZonasExclusivas = adicionalZonasExclusivas;
    }

    @Override
    public double calcularCuotaMensual() {
        return cuotaBase + adicionalZonasExclusivas;
    }

    @Override
    public TipoMembresia getTipoMembresia() {
        return TipoMembresia.VIP;
    }

    public double getAdicionalZonasExclusivas() {
        return adicionalZonasExclusivas;
    }

    public void setAdicionalZonasExclusivas(double adicionalZonasExclusivas) {
        this.adicionalZonasExclusivas = adicionalZonasExclusivas;
    }
}
