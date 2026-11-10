package LogicaNegocio;

import Datos.AccesoDatos;
import Datos.PersistenciaException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class Logica {

    private static final double VALOR_BASE_CUOTA = 10000;

    // Socios ingresado
    private final Map<String, Socio> indicePorDni = new HashMap<String, Socio>();

    // Equipamiento ingresado
    private final Map<String, Equipamiento> inventario = new HashMap<String, Equipamiento>();
    private final List<Alquiler> alquileres = new ArrayList<Alquiler>();
    private int proximoNumeroAlquiler = 1;

    private final Map<String, Set<Reserva>> reservasPorActividad = new HashMap<String, Set<Reserva>>();

    private final Map<String, List<Factura>> facturasPorSocio = new HashMap<String, List<Factura>>();
    private int proximoNumeroFactura = 1;

    private final AccesoDatos persistencia;

    public Logica(AccesoDatos persistencia) throws PersistenciaException {
        this.persistencia = Objects.requireNonNull(persistencia);
        traerSociosDelArchivo();
        traerEquipamientoDelArchivo();
        traerReservasDelArchivo();
        traerFacturasDelArchivo();
    }

    public double valorBaseCuota() {
        return VALOR_BASE_CUOTA;
    }

    // Socios
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

    // Equipamineto
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

        if (!socio.isHabilitadoAcceso()) {
            throw new AccesoDenegadoException(socio.getMotivoAcceso(), socio.getMotivoSancion());
        }

        if (tieneAlquilerVencido(socio.getDni(), fecha)) {
            throw new SocioConAlquilerVencidoException(socio.getDni());
        }

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
            // no se pudo guardar
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

        double recargo = alquiler.calcularRecargo(fechaDevolucion);
        long diasAtraso = alquiler.diasDeAtraso(fechaDevolucion);
        Socio socio = indicePorDni.get(alquiler.getDniSocio());
        boolean sancionar = recargo > 0 && socio != null;

        alquiler.registrarDevolucion(fechaDevolucion, recargo);
        equipo.marcarDisponible();
        if (sancionar) {
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
            // se deshace si no se pudo guardar
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

    public void reservar(String dni, String actividad, String horario)
            throws SocioNoEncontradoException, DniInvalidoException, ReservaDuplicadaException,
            PersistenciaException {
        Socio socio = consultarPorDni(dni);
        Reserva nueva = new Reserva(socio.getDni(), actividad.trim(), horario.trim());

        Set<Reserva> reservas = reservasPorActividad.get(nueva.getActividad());
        boolean actividadNueva = (reservas == null);
        if (actividadNueva) {
            reservas = new HashSet<Reserva>();
            reservasPorActividad.put(nueva.getActividad(), reservas);
        }

        if (!reservas.add(nueva)) {
            throw new ReservaDuplicadaException(nueva);
        }

        try {
            volcarReservasAlArchivo();
        } catch (PersistenciaException e) {
            reservas.remove(nueva);
            if (actividadNueva) {
                reservasPorActividad.remove(nueva.getActividad());
            }
            throw e;
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

    public Factura emitirFacturaCuota(String dni, LocalDate fecha)
            throws SocioNoEncontradoException, DniInvalidoException, PersistenciaException {
        Objects.requireNonNull(fecha);
        Socio socio = consultarPorDni(dni);

        double recargo = socio.cobrarRecargoPendiente();
        Factura nueva = agregarFactura(socio.getDni(), TipoFactura.CUOTA,
                socio.calcularCuotaMensual() + recargo, fecha);

        try {
            volcarFacturasAlArchivo();
            if (recargo > 0) {
                volcarSociosAlArchivo();
            }
        } catch (PersistenciaException e) {
            quitarFactura(nueva);
            socio.sumarRecargo(recargo);
            throw e;
        }
        return nueva;
    }

    public Factura emitirFactura(String dni, TipoFactura tipo, double monto, LocalDate fecha)
            throws SocioNoEncontradoException, DniInvalidoException, PersistenciaException {
        Socio socio = consultarPorDni(dni);
        Factura nueva = agregarFactura(socio.getDni(), tipo, monto, fecha);

        try {
            volcarFacturasAlArchivo();
        } catch (PersistenciaException e) {
            quitarFactura(nueva);
            throw e;
        }
        return nueva;
    }

    public List<Factura> facturasDeSocio(String dni) throws DniInvalidoException {
        List<Factura> facturas = facturasPorSocio.get(limpiarDni(dni));
        if (facturas == null) {
            return new ArrayList<Factura>();
        }
        return new ArrayList<Factura>(facturas);
    }

    public List<Factura> facturasDeSocioEnPeriodo(String dni, int anio, int mes) throws DniInvalidoException {
        List<Factura> delPeriodo = new ArrayList<Factura>();
        for (Factura f : facturasDeSocio(dni)) {
            if (f.getAnio() == anio && f.getMes() == mes) {
                delPeriodo.add(f);
            }
        }
        return delPeriodo;
    }

    public int totalFacturas() {
        int total = 0;
        for (List<Factura> facturas : facturasPorSocio.values()) {
            total += facturas.size();
        }
        return total;
    }

    private Factura agregarFactura(String dni, TipoFactura tipo, double monto, LocalDate fecha) {
        Factura nueva = new Factura(proximoNumeroFactura, dni, tipo, monto, fecha);
        List<Factura> facturas = facturasPorSocio.get(dni);
        if (facturas == null) {
            facturas = new ArrayList<Factura>();
            facturasPorSocio.put(dni, facturas);
        }
        facturas.add(nueva);
        proximoNumeroFactura++;
        return nueva;
    }

    private void quitarFactura(Factura factura) {
        facturasPorSocio.get(factura.getDniSocio()).remove(factura);
        proximoNumeroFactura--;
    }

    public List<Socio> reporteSociosPorApellidoNombre() {
        List<Socio> lista = new ArrayList<Socio>(indicePorDni.values());
        Collections.sort(lista, ComparadoresSocio.POR_APELLIDO_NOMBRE);
        return lista;
    }

    public List<Socio> reporteSociosPorCuota() {
        List<Socio> lista = new ArrayList<Socio>(indicePorDni.values());
        Collections.sort(lista, ComparadoresSocio.POR_CUOTA);
        return lista;
    }

    public List<Socio> reporteMorososYSancionados() {
        List<Socio> lista = new ArrayList<Socio>();
        for (Socio s : indicePorDni.values()) {
            if (!s.isCuotasAlDia() || s.isSancionado()) {
                lista.add(s);
            }
        }
        Collections.sort(lista, ComparadoresSocio.POR_APELLIDO_NOMBRE);
        return lista;
    }

    public List<Socio> buscarPorApellido(String apellido) {
        Objects.requireNonNull(apellido);
        List<Socio> ordenada = reporteSociosPorApellidoNombre();
        Socio clave = new SocioEstandar("", "", apellido.trim(), "", "", 0);
        int posicion = Collections.binarySearch(ordenada, clave, ComparadoresSocio.BUSCAR_POR_APELLIDO);
        return coincidencias(ordenada, posicion, clave, ComparadoresSocio.BUSCAR_POR_APELLIDO);
    }

    public List<Socio> buscarPorCuota(double monto) {
        List<Socio> ordenada = reporteSociosPorCuota();
        Socio clave = new SocioEstandar("", "", "", "", "", monto);
        int posicion = Collections.binarySearch(ordenada, clave, ComparadoresSocio.BUSCAR_POR_CUOTA);
        return coincidencias(ordenada, posicion, clave, ComparadoresSocio.BUSCAR_POR_CUOTA);
    }

    private List<Socio> coincidencias(List<Socio> ordenada, int posicion, Socio clave,
            Comparator<Socio> criterio) {
        List<Socio> encontrados = new ArrayList<Socio>();
        if (posicion < 0) { // negativo = no existe
            return encontrados;
        }
        int inicio = posicion;
        while (inicio > 0 && criterio.compare(ordenada.get(inicio - 1), clave) == 0) {
            inicio--;
        }
        int fin = posicion;
        while (fin < ordenada.size() - 1 && criterio.compare(ordenada.get(fin + 1), clave) == 0) {
            fin++;
        }
        encontrados.addAll(ordenada.subList(inicio, fin + 1));
        return encontrados;
    }

    public List<OcupacionActividad> reporteOcupacionActividades() {
        List<OcupacionActividad> lista = new ArrayList<OcupacionActividad>();
        for (Map.Entry<String, Set<Reserva>> entrada : reservasPorActividad.entrySet()) {
            lista.add(new OcupacionActividad(entrada.getKey(), entrada.getValue().size()));
        }
        Collections.sort(lista, OcupacionActividad.POR_OCUPACION);
        return lista;
    }

    public double recaudacionDelPeriodo(int anio, int mes) {
        double total = 0;
        for (List<Factura> facturas : facturasPorSocio.values()) {
            for (Factura f : facturas) {
                if (f.getAnio() == anio && f.getMes() == mes) {
                    total += f.getMonto();
                }
            }
        }
        return total;
    }

    public Map<String, Double> recaudacionPorPeriodo() {
        Map<String, Double> porPeriodo = new TreeMap<String, Double>();
        for (List<Factura> facturas : facturasPorSocio.values()) {
            for (Factura f : facturas) {
                String periodo = String.format("%04d/%02d", f.getAnio(), f.getMes());
                Double acumulado = porPeriodo.get(periodo);
                porPeriodo.put(periodo, (acumulado == null ? 0 : acumulado) + f.getMonto());
            }
        }
        return porPeriodo;
    }

    private void traerFacturasDelArchivo() throws PersistenciaException {
        facturasPorSocio.putAll(persistencia.leerFacturas());

        int mayor = 0;
        for (List<Factura> facturas : facturasPorSocio.values()) {
            for (Factura f : facturas) {
                if (f.getNumero() > mayor) {
                    mayor = f.getNumero();
                }
            }
        }
        proximoNumeroFactura = mayor + 1;
    }

    private void volcarFacturasAlArchivo() throws PersistenciaException {
        persistencia.grabarFacturas(facturasPorSocio);
    }

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

    private void traerReservasDelArchivo() throws PersistenciaException {
        reservasPorActividad.putAll(persistencia.leerReservas());
    }

    private void volcarReservasAlArchivo() throws PersistenciaException {
        persistencia.grabarReservas(reservasPorActividad);
    }

    private String limpiarDni(String dni) throws DniInvalidoException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new DniInvalidoException();
        }
        return dni.trim();
    }

    public void registrarAccesoMolinete(String dni)
            throws SocioNoEncontradoException, DniInvalidoException, AccesoDenegadoException {
        Socio socio = consultarPorDni(dni);
        if (!socio.isHabilitadoAcceso()) {
            throw new AccesoDenegadoException(socio.getMotivoAcceso(), socio.getMotivoSancion());
        }

    }
}