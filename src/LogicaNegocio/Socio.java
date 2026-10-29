package LogicaNegocio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    protected double recargoPendiente;
    protected List<Advertencia> advertencias = new ArrayList<Advertencia>();

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
        this.recargoPendiente = 0;
    }

    public abstract double calcularCuotaMensual();

    public abstract TipoMembresia getTipoMembresia();

    public boolean isHabilitadoAcceso() {
        return activo && cuotasAlDia && !sancionado;
    }

    public MotivoAcceso getMotivoAcceso() {
        if (!activo)
            return MotivoAcceso.SOCIO_DE_BAJA;
        if (!cuotasAlDia)
            return MotivoAcceso.CUOTAS_IMPAGAS;
        if (sancionado)
            return MotivoAcceso.SANCION_VIGENTE;
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

    // Recargos y advertencias

    public double getRecargoPendiente() {
        return recargoPendiente;
    }

    public void sumarRecargo(double monto) {
        this.recargoPendiente += monto;
    }

    public void restarRecargo(double monto) {
        this.recargoPendiente -= monto;
        if (this.recargoPendiente < 0) {
            this.recargoPendiente = 0;
        }
    }

    // facturacion devuelve el recargo y lo deja en cero
    public double cobrarRecargoPendiente() {
        double monto = recargoPendiente;
        recargoPendiente = 0;
        return monto;
    }

    public void agregarAdvertencia(Advertencia advertencia) {
        listaAdvertencias().add(advertencia);
    }

    public void quitarUltimaAdvertencia() {
        List<Advertencia> lista = listaAdvertencias();
        if (!lista.isEmpty()) {
            lista.remove(lista.size() - 1);
        }
    }

    public List<Advertencia> getAdvertencias() {
        return Collections.unmodifiableList(listaAdvertencias());
    }

    // Si el socio viene de un Socios.txt viejo llega null la lista
    private List<Advertencia> listaAdvertencias() {
        if (advertencias == null) {
            advertencias = new ArrayList<Advertencia>();
        }
        return advertencias;
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
