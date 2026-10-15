package LogicaNegocio;

public class SocioBecado extends Socio {

    private static final long serialVersionUID = 1L;

    private double porcentajeDescuento;

    public SocioBecado(String dni, String nombre, String apellido, String email, String telefono, double cuotaBase, double porcentajeDescuento) {
        super(dni, nombre, apellido, email, telefono, cuotaBase);
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public double calcularCuotaMensual() {
        double descuento = cuotaBase * (porcentajeDescuento / 100.0);
        return cuotaBase - descuento;
    }

    @Override
    public String getTipoMembresia() {
        return "Becado";
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }
}
