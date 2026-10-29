package LogicaNegocio;

import Datos.AccesoDatos;
import Datos.PersistenciaException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Logica {

    private static final double VALOR_BASE_CUOTA = 10000;

    // Socios ingresado
    private final Map<String, Socio> indicePorDni = new HashMap<String, Socio>();

    // Equipamiento ingresado
    private final Map<String, Equipamiento> inventario = new HashMap<String, Equipamiento>();
    private final List<Alquiler> alquileres = new ArrayList<Alquiler>();
    private int proximoNumeroAlquiler = 1;

    private final Map<String, Set<Reserva>> reservasPorActividad = new HashMap<String, Set<Reserva>>();

    private final AccesoDatos persistencia;

    public Logica(AccesoDatos persistencia) throws PersistenciaException {
        this.persistencia = Objects.requireNonNull(persistencia);
        traerSociosDelArchivo();
        traerEquipamientoDelArchivo();
    }

    public double valorBaseCuota() {
        return VALOR_BASE_CUOTA;
    }
    // SOCIOS

    public void darDeAlta(Socio nuevo)
            throws SocioDuplicadoException, DniInvalidoException, PersistenciaException {
        Objects.requireNonNull(nuevo);
        String dni = limpiarDni(nuevo.getDni());

        if (indicePorDni.containsKey(dni)) {
            throw new SocioDuplicadoException(dni);
        }

        nuevo.setDni(dni);
        indicePorDni.put(dni, nuevo);

        try {
            volcarSociosAlArchivo();
        } catch (PersistenciaException e) {
            indicePorDni.remove(dni);
            throw e;
        }
    }

    public Socio consultarPorDni(String dni) throws SocioNoEncontradoException, DniInvalidoException {
        String clave = limpiarDni(dni);
        Socio buscado = indicePorDni.get(clave);
        if (buscado == null) {
            throw new SocioNoEncontradoException(clave);
        }
        return buscado;
    }

    public boolean yaEstaRegistrado(String dni) {
        if (dni == null) {
            return false;
        }
        return indicePorDni.containsKey(dni.trim());
    }

    public int totalSocios() {
        return indicePorDni.size();
    }

    public List<Socio> listadoOrdenadoPorDni() {
        List<Socio> lista = new ArrayList<Socio>(indicePorDni.values());
        Collections.sort(lista);
        return lista;
    }

    // EQUIPAMIENTO
    public void registrarEquipamiento(Equipamiento nuevo)
            throws EquipamientoDuplicadoException, PersistenciaException {
        Objects.requireNonNull(nuevo);
        String codigo = nuevo.getCodigo();

        if (inventario.containsKey(codigo)) {
            throw new EquipamientoDuplicadoException(codigo);
        }

        inventario.put(codigo, nuevo);

        try {
            volcarEquipamientoAlArchivo();
        } catch (PersistenciaException e) {
            inventario.remove(codigo); // se deshace el alta si no se pudo guardar
            throw e;
        }
    }

    public Equipamiento consultarEquipamiento(String codigo) throws EquipamientoNoEncontradoException {
        String clave = codigo == null ? "" : codigo.trim();
        Equipamiento buscado = inventario.get(clave);
        if (buscado == null) {
            throw new EquipamientoNoEncontradoException(clave);
        }
        return buscado;
    }

    public List<Equipamiento> listadoEquipamientoOrdenado() {
        List<Equipamiento> lista = new ArrayList<Equipamiento>(inventario.values());
        Collections.sort(lista);
        return lista;
    }

    public int totalEquipamiento() {
        return inventario.size();
    }

    // ALQUILERES

    // Alquila con fecha de hoy
    public Alquiler alquilar(String dni, String codigoEquipamiento)
            throws SocioNoEncontradoException, DniInvalidoException, EquipamientoNoEncontradoException,
            AccesoDenegadoException, SocioConAlquilerVencidoException, EquipamientoNoDisponibleException,
            PersistenciaException {
        return alquilar(dni, codigoEquipamiento, LocalDate.now());
    }

    // Devoluciones para saber si hay recargo
    public Alquiler alquilar(String dni, String codigoEquipamiento, LocalDate fecha)
            throws SocioNoEncontradoException, DniInvalidoException, EquipamientoNoEncontradoException,
            AccesoDenegadoException, SocioConAlquilerVencidoException, EquipamientoNoDisponibleException,
            PersistenciaException {
        Objects.requireNonNull(fecha);
        Socio socio = consultarPorDni(dni);
        Equipamiento equipo = consultarEquipamiento(codigoEquipamiento);

        // 1
        if (!socio.isHabilitadoAcceso()) {
            throw new AccesoDenegadoException(socio.getMotivoAcceso(), socio.getMotivoSancion());
        }
        // 2
        if (tieneAlquilerVencido(socio.getDni(), fecha)) {
            throw new SocioConAlquilerVencidoException(socio.getDni());
        }
        // 3
        if (!equipo.isDisponible()) {
            throw new EquipamientoNoDisponibleException(equipo.getCodigo(), equipo.getNombre());
        }

        Alquiler nuevo = new Alquiler(proximoNumeroAlquiler, socio.getDni(), equipo, fecha);
        equipo.marcarAlquilado();
        alquileres.add(nuevo);
        proximoNumeroAlquiler++;

        try {
            volcarEquipamientoAlArchivo();
        } catch (PersistenciaException e) {
            // se deshace todo si no se pudo guardar
            alquileres.remove(nuevo);
            equipo.marcarDisponible();
            proximoNumeroAlquiler--;
            throw e;
        }
        return nuevo;
    }

    // Devuelve con fecha de hoy
    public Alquiler devolver(String codigoEquipamiento)
            throws EquipamientoNoEncontradoException, AlquilerNoEncontradoException, FechaInvalidaException,
            PersistenciaException {
        return devolver(codigoEquipamiento, LocalDate.now());
    }

    public Alquiler devolver(String codigoEquipamiento, LocalDate fechaDevolucion)
            throws EquipamientoNoEncontradoException, AlquilerNoEncontradoException, FechaInvalidaException,
            PersistenciaException {
        Objects.requireNonNull(fechaDevolucion);
        Equipamiento equipo = consultarEquipamiento(codigoEquipamiento);

        Alquiler alquiler = buscarAlquilerActivo(equipo.getCodigo());
        if (alquiler == null) {
            throw new AlquilerNoEncontradoException(equipo.getCodigo());
        }
        if (fechaDevolucion.isBefore(alquiler.getFechaAlquiler())) {
            throw new FechaInvalidaException();
        }

        // recargo por cada dia de atraso
        double recargo = alquiler.calcularRecargo(fechaDevolucion);
        long diasAtraso = alquiler.diasDeAtraso(fechaDevolucion);
        Socio socio = indicePorDni.get(alquiler.getDniSocio());
        boolean sancionar = recargo > 0 && socio != null;

        alquiler.registrarDevolucion(fechaDevolucion, recargo);
        equipo.marcarDisponible();
        if (sancionar) {
            // el recargo queda pendiente para la siguiente factura y se deja una
            // advertencia
            socio.sumarRecargo(recargo);
            socio.agregarAdvertencia(new Advertencia(fechaDevolucion, equipo.getCodigo(), equipo.getNombre(),
                    diasAtraso, recargo));
        }

        try {
            volcarEquipamientoAlArchivo();
            if (sancionar) {
                volcarSociosAlArchivo();
            }
        } catch (PersistenciaException e) {
            // se deshace todo si no se pudo guardar
            alquiler.anularDevolucion();
            equipo.marcarAlquilado();
            if (sancionar) {
                socio.restarRecargo(recargo);
                socio.quitarUltimaAdvertencia();
            }
            throw e;
        }
        return alquiler;
    }

    public boolean tieneAlquilerVencido(String dni, LocalDate hoy) {
        for (Alquiler a : alquileres) {
            if (a.getDniSocio().equals(dni) && a.isVencido(hoy)) {
                return true;
            }
        }
        return false;
    }

    public List<Alquiler> listadoAlquileres() {
        return new ArrayList<Alquiler>(alquileres);
    }

    public List<Alquiler> alquileresActivos() {
        List<Alquiler> activos = new ArrayList<Alquiler>();
        for (Alquiler a : alquileres) {
            if (!a.isDevuelto()) {
                activos.add(a);
            }
        }
        return activos;
    }

    public List<Alquiler> alquileresDeSocio(String dni) {
        List<Alquiler> delSocio = new ArrayList<Alquiler>();
        for (Alquiler a : alquileres) {
            if (a.getDniSocio().equals(dni)) {
                delSocio.add(a);
            }
        }
        return delSocio;
    }

    public int totalAlquileres() {
        return alquileres.size();
    }

    private Alquiler buscarAlquilerActivo(String codigo) {
        for (Alquiler a : alquileres) {
            if (!a.isDevuelto() && a.getEquipamiento().getCodigo().equals(codigo)) {
                return a;
            }
        }
        return null;
    }

    // RESERVAS (TS2-02)

    public void reservar(String dni, String actividad, String horario)
            throws SocioNoEncontradoException, DniInvalidoException, ReservaDuplicadaException {
        Socio socio = consultarPorDni(dni);
        Reserva nueva = new Reserva(socio.getDni(), actividad.trim(), horario.trim());

        Set<Reserva> reservas = reservasPorActividad.get(nueva.getActividad());
        if (reservas == null) {
            reservas = new HashSet<Reserva>();
            reservasPorActividad.put(nueva.getActividad(), reservas);
        }

        // evitar set duplicado
        if (!reservas.add(nueva)) {
            throw new ReservaDuplicadaException(nueva);
        }
    }

    public Set<Reserva> reservasDeActividad(String actividad) {
        Set<Reserva> reservas = reservasPorActividad.get(actividad.trim());
        if (reservas == null) {
            return new HashSet<Reserva>();
        }
        return new HashSet<Reserva>(reservas);
    }

    public int totalReservas() {
        int total = 0;
        for (Set<Reserva> reservas : reservasPorActividad.values()) {
            total += reservas.size();
        }
        return total;
    }

    // Persistencia de datos

    private void traerSociosDelArchivo() throws PersistenciaException {
        indicePorDni.putAll(persistencia.leerSocios());
    }

    private void volcarSociosAlArchivo() throws PersistenciaException {
        persistencia.grabarSocios(indicePorDni);
    }

    private void traerEquipamientoDelArchivo() throws PersistenciaException {
        RegistroEquipamiento registro = persistencia.leerEquipamiento();
        inventario.putAll(registro.getInventario());
        alquileres.addAll(registro.getAlquileres());

        int mayor = 0;
        for (Alquiler a : alquileres) {
            if (a.getNumero() > mayor) {
                mayor = a.getNumero();
            }
        }
        proximoNumeroAlquiler = mayor + 1;
    }

    private void volcarEquipamientoAlArchivo() throws PersistenciaException {

        persistencia.grabarEquipamiento(new RegistroEquipamiento(
                new HashMap<String, Equipamiento>(inventario), new ArrayList<Alquiler>(alquileres)));
    }

    private String limpiarDni(String dni) throws DniInvalidoException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new DniInvalidoException();
        }
        return dni.trim();
    }
}
