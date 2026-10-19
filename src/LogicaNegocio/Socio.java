package LogicaNegocio;

import java.io.Serializable;
import java.util.Objects;

public abstract class Socio implements Serializable, Comparable<Socio> {

    private static final long serialVersionUID = 1L;

    protected String dni;
    protected String nombre;
    protected String apellido;
    protected String email;
    protected String telefono;
    protected double cuotaBase;
    protected boolean activo;
    protected boolean cuotasAlDia;
    protected boolean sancionado;
    protected String motivoSancion;

    public Socio(String dni, String nombre, String apellido, String email, String telefono, double cuotaBase) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.cuotaBase = cuotaBase;
        this.activo = true;
        this.cuotasAlDia = true;
        this.sancionado = false;
        this.motivoSancion = "";
    }

    public abstract double calcularCuotaMensual();

    public abstract String getTipoMembresia();

    public boolean isHabilitadoAcceso() {
        return activo && cuotasAlDia && !sancionado;
    }

    public MotivoAcceso getMotivoRechazoAcceso() {
        if (!activo)
            return MotivoAcceso.DADO_DE_BAJA;
        if (!cuotasAlDia)
            return MotivoAcceso.CUOTAS_IMPAGAS;
        if (sancionado)
            return MotivoAcceso.SANCIONADO;
        return MotivoAcceso.PERMITIDO;
    }

    public void aplicarSancion(String motivo) {
        this.sancionado = true;
        this.motivoSancion = motivo;
    }

    public void levantarSancion() {
        this.sancionado = false;
        this.motivoSancion = "";
    }

    public void marcarMora() {
        this.cuotasAlDia = false;
    }

    public void registrarPagoCuota() {
        this.cuotasAlDia = true;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public double getCuotaBase() {
        return cuotaBase;
    }

    public void setCuotaBase(double cuotaBase) {
        this.cuotaBase = cuotaBase;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isCuotasAlDia() {
        return cuotasAlDia;
    }

    public void setCuotasAlDia(boolean cuotasAlDia) {
        this.cuotasAlDia = cuotasAlDia;
    }

    public boolean isSancionado() {
        return sancionado;
    }

    public String getMotivoSancion() {
        return motivoSancion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof Socio))
            return false;
        Socio other = (Socio) obj;
        return Objects.equals(this.dni, other.dni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dni);
    }

    @Override
    public int compareTo(Socio otro) {
        if (otro == null)
            return 1;
        return this.dni.compareTo(otro.dni);
    }
}
