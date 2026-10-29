package LogicaNegocio;

public enum EstadoAlquiler {
    ACTIVO, // alquilado y dentro del plazo
    VENCIDO, // alquilado, paso la fecha limite y no se devolvio
    DEVUELTO
}
