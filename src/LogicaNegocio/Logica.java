package LogicaNegocio;

import Datos.AccesoDatos;
import Datos.PersistenciaException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Logica {

    private static final double VALOR_BASE_CUOTA = 10000;

    private final Map<String, Socio> indicePorDni = new HashMap<String, Socio>();
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
