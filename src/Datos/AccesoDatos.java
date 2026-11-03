package Datos;

import LogicaNegocio.Alquiler;
import LogicaNegocio.Equipamiento;
import LogicaNegocio.Factura;
import LogicaNegocio.RegistroEquipamiento;
import LogicaNegocio.Socio;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccesoDatos {

    private static final String ARCHIVO_SOCIOS = "Socios.txt";
    private static final String ARCHIVO_EQUIPAMIENTO = "Equipamiento.txt";
    private static final String ARCHIVO_FACTURAS = "Facturas.txt";

    private final String rutaSocios;
    private final String rutaEquipamiento;
    private final String rutaFacturas;

    public AccesoDatos() {
        this(ARCHIVO_SOCIOS, ARCHIVO_EQUIPAMIENTO, ARCHIVO_FACTURAS);
    }

    public AccesoDatos(String rutaSocios, String rutaEquipamiento, String rutaFacturas) {
        this.rutaSocios = Objects.requireNonNull(rutaSocios);
        this.rutaEquipamiento = Objects.requireNonNull(rutaEquipamiento);
        this.rutaFacturas = Objects.requireNonNull(rutaFacturas);
    }

    // Socios.txt

    public void grabarSocios(Map<String, Socio> socios) throws PersistenciaException {
        Objects.requireNonNull(socios);
        // Se graba una copia
        escribirEnArchivo(rutaSocios, new HashMap<String, Socio>(socios));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Socio> leerSocios() throws PersistenciaException {
        Object contenido = leerDeArchivo(rutaSocios);

        if (contenido == null) {
            return new HashMap<String, Socio>(); // no hay archivo todavia
        }
        try {
            return (Map<String, Socio>) contenido; // importante
        } catch (ClassCastException e) {
            throw new PersistenciaException(MotivoFallo.FORMATO_INVALIDO, rutaSocios, e);
        }
    }

    // Equipamiento.txt

    public void grabarEquipamiento(RegistroEquipamiento registro) throws PersistenciaException {
        Objects.requireNonNull(registro);
        escribirEnArchivo(rutaEquipamiento, registro);
    }

    public RegistroEquipamiento leerEquipamiento() throws PersistenciaException {
        Object contenido = leerDeArchivo(rutaEquipamiento);

        if (contenido == null) {
            // no hay archivo todavia
            return new RegistroEquipamiento(new HashMap<String, Equipamiento>(),
                    new ArrayList<Alquiler>());
        }
        try {
            return (RegistroEquipamiento) contenido;
        } catch (ClassCastException e) {
            throw new PersistenciaException(MotivoFallo.FORMATO_INVALIDO, rutaEquipamiento, e);
        }
    }

    // Facturas.txt

    public void grabarFacturas(Map<String, List<Factura>> facturas) throws PersistenciaException {
        Objects.requireNonNull(facturas);
        Map<String, List<Factura>> copia = new HashMap<String, List<Factura>>();
        for (Map.Entry<String, List<Factura>> entrada : facturas.entrySet()) {
            copia.put(entrada.getKey(), new ArrayList<Factura>(entrada.getValue()));
        }
        escribirEnArchivo(rutaFacturas, copia);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Factura>> leerFacturas() throws PersistenciaException {
        Object contenido = leerDeArchivo(rutaFacturas);

        if (contenido == null) {
            return new HashMap<String, List<Factura>>();
        }
        try {
            return (Map<String, List<Factura>>) contenido;
        } catch (ClassCastException e) {
            throw new PersistenciaException(MotivoFallo.FORMATO_INVALIDO, rutaFacturas, e);
        }
    }

    // Metodos
    private void escribirEnArchivo(String ruta, Object objeto) throws PersistenciaException {
        try (ObjectOutputStream flujo = new ObjectOutputStream(new FileOutputStream(ruta))) {
            flujo.writeObject(objeto);
        } catch (IOException e) {
            throw new PersistenciaException(MotivoFallo.ESCRITURA, ruta, e);
        }
    }

    private Object leerDeArchivo(String ruta) throws PersistenciaException {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return null;
        }
        try (ObjectInputStream flujo = new ObjectInputStream(new FileInputStream(archivo))) {
            return flujo.readObject();
        } catch (FileNotFoundException e) {
            throw new PersistenciaException(MotivoFallo.LECTURA, ruta, e);
        } catch (ObjectStreamException | EOFException | ClassNotFoundException | ClassCastException e) {
            // vacio o version vieja de una clase o no existe
            throw new PersistenciaException(MotivoFallo.FORMATO_INVALIDO, ruta, e);
        } catch (IOException e) {
            throw new PersistenciaException(MotivoFallo.LECTURA, ruta, e);
        }
    }
}
