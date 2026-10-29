package LogicaNegocio;

public class AccesoDenegadoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final MotivoAcceso motivo;
    private final String motivoSancion; // solo tiene dato si el motivo es SANCION_VIGENTE

    public AccesoDenegadoException(MotivoAcceso motivo, String motivoSancion) {
        this.motivo = motivo;
        this.motivoSancion = motivoSancion;
    }

    public MotivoAcceso getMotivo() {
        return motivo;
    }

    public String getMotivoSancion() {
        return motivoSancion;
    }
}
