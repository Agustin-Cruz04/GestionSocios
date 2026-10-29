package Datos;

public class PersistenciaException extends Exception {

    private static final long serialVersionUID = 1L;

    private final MotivoFallo motivo;
    private final String archivo;

    public PersistenciaException(MotivoFallo motivo, String archivo, Throwable causa) {
        super(causa); // se guarda el error original
        this.motivo = motivo;
        this.archivo = archivo;
    }

    public MotivoFallo getMotivo() {
        return motivo;
    }

    public String getArchivo() {
        return archivo;
    }
}
