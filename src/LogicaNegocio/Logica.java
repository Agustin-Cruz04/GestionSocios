package LogicaNegocio;

import Datos.AccesoDatos;
import Datos.PersistenciaException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

public class Logica {

    private static final double VALOR_BASE_CUOTA = 10000;

    private final Map<String, Socio> indicePorDni = new HashMap<String, Socio>();
    private final Map<String, Set<Reserva>> reservasPorActividad = new HashMap<String, Set<Reserva>>();
    private final AccesoDatos persistencia;

    public Logica(AccesoDatos persistencia) throws PersistenciaException {
        this.persistencia = Objects.requireNonNull(persistencia);
        traerSociosDelArchivo();
    }

    public double valorBaseCuota() {
        return VALOR_BASE_CUOTA;
    }

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

    public void reservar(String dni, String actividad, String horario)
            throws SocioNoEncontradoException, DniInvalidoException, ReservaDuplicadaException {
        Socio socio = consultarPorDni(dni);
        Reserva nueva = new Reserva(socio.getDni(), actividad.trim(), horario.trim());

        Set<Reserva> reservas = reservasPorActividad.get(nueva.getActividad());
        if (reservas == null) {
            reservas = new HashSet<Reserva>();
            reservasPorActividad.put(nueva.getActividad(), reservas);
        }

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

    private void traerSociosDelArchivo() throws PersistenciaException {
        indicePorDni.putAll(persistencia.leerSocios());
    }

    private void volcarSociosAlArchivo() throws PersistenciaException {
        persistencia.grabarSocios(indicePorDni);
    }

    private String limpiarDni(String dni) throws DniInvalidoException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new DniInvalidoException();
        }
        return dni.trim();
    }
}
