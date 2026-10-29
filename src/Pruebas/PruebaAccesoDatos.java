package Pruebas;

import Datos.AccesoDatos;
import Datos.MotivoFallo;
import Datos.PersistenciaException;
import LogicaNegocio.Alquiler;
import LogicaNegocio.Equipamiento;
import LogicaNegocio.RegistroEquipamiento;
import LogicaNegocio.Socio;
import LogicaNegocio.TipoEquipamiento;
import LogicaNegocio.TipoMembresia;
import LogicaNegocio.SocioBecado;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioVIP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PruebaAccesoDatos {

    private static int fallos = 0;

    public static void main(String[] args) throws IOException {
        System.out.println("=== PRUEBA ACCESODATOS Y PERSISTENCIAEXCEPTION ===");

        File archivo = File.createTempFile("socios_prueba", ".dat");
        archivo.delete(); // se empieza SIN archivo
        File archivoEquipo = File.createTempFile("equipamiento_prueba", ".dat");
        archivoEquipo.delete();
        AccesoDatos datos = new AccesoDatos(archivo.getPath(), archivoEquipo.getPath());

        try {
            System.out.println("\n--- 1. Archivo inexistente ---");
            try {
                Map<String, Socio> vacio = datos.leerSocios();
                comprobar("Sin archivo devuelve un mapa vacio y no lanza error", vacio != null && vacio.isEmpty());
            } catch (PersistenciaException e) {
                comprobar("Sin archivo NO debe lanzar excepcion", false);
            }

            System.out.println("\n--- 2. Grabar y recuperar ---");
            Socio carlos = new SocioEstandar("30111222", "Carlos", "Gomez", "c@mail.com", "11-1111", 10000);
            Socio valeria = new SocioVIP("32333444", "Valeria", "Lopez", "v@mail.com", "11-2222", 10000, 4000.0);
            Socio lucas = new SocioBecado("35555666", "Lucas", "Fernandez", "l@mail.com", "11-3333", 10000, 50.0);
            carlos.aplicarSancion("Devolucion tardia");
            Map<String, Socio> original = new HashMap<String, Socio>();
            original.put(carlos.getDni(), carlos);
            original.put(valeria.getDni(), valeria);
            original.put(lucas.getDni(), lucas);
            try {
                datos.grabarSocios(original);
                comprobar("El archivo se creo en disco", archivo.exists());

                Map<String, Socio> leido = datos.leerSocios();
                comprobar("Se recuperaron los 3 socios", leido.size() == 3);
                comprobar("Son objetos leidos del archivo, no los mismos de memoria", leido.get("30111222") != carlos);
                comprobar("Carlos sigue siendo Estandar",
                        leido.get("30111222").getTipoMembresia() == TipoMembresia.ESTANDAR);
                comprobar("Valeria sigue siendo VIP y su cuota polimorfica da 14000",
                        leido.get("32333444").getTipoMembresia() == TipoMembresia.VIP
                                && leido.get("32333444").calcularCuotaMensual() == 14000.0);
                comprobar("Lucas sigue siendo Becado y su cuota polimorfica da 5000",
                        leido.get("35555666").getTipoMembresia() == TipoMembresia.BECADO
                                && leido.get("35555666").calcularCuotaMensual() == 5000.0);
                comprobar("Se conserva el estado (sancion de Carlos)",
                        leido.get("30111222").isSancionado()
                                && leido.get("30111222").getMotivoSancion().equals("Devolucion tardia"));
            } catch (PersistenciaException e) {
                comprobar("Grabar y leer no debe fallar (archivo " + e.getArchivo() + ")", false);
            }

            System.out.println("\n--- 3. Volver a grabar reemplaza lo anterior ---");
            try {
                Map<String, Socio> soloUno = new HashMap<String, Socio>();
                soloUno.put(lucas.getDni(), lucas);
                datos.grabarSocios(soloUno);
                comprobar("Despues de grabar 1 socio, se leen 1", datos.leerSocios().size() == 1);
            } catch (PersistenciaException e) {
                comprobar("Volver a grabar no debe fallar", false);
            }

            System.out.println("\n--- 4. Archivo danado (texto cualquiera) ---");
            try (FileOutputStream basura = new FileOutputStream(archivo)) {
                basura.write("esto no es un objeto serializado".getBytes());
            }
            probarLecturaFallida(datos, MotivoFallo.FORMATO_INVALIDO, "Archivo danado");

            System.out.println("\n--- 5. Archivo vacio ---");
            new FileOutputStream(archivo).close();
            probarLecturaFallida(datos, MotivoFallo.FORMATO_INVALIDO, "Archivo vacio");

            System.out.println("\n--- 6. Archivo valido pero con otro contenido (un String, no un Map) ---");
            try (ObjectOutputStream otro = new ObjectOutputStream(new FileOutputStream(archivo))) {
                otro.writeObject("hola");
            }
            probarLecturaFallida(datos, MotivoFallo.FORMATO_INVALIDO, "Contenido de otro tipo");

            System.out.println("\n--- 7. La ruta es una carpeta (no se puede leer como archivo) ---");
            AccesoDatos conCarpeta = new AccesoDatos(System.getProperty("java.io.tmpdir"), archivoEquipo.getPath());
            probarLecturaFallida(conCarpeta, MotivoFallo.LECTURA, "Ruta que es una carpeta");

            System.out.println("\n--- 8. Error al escribir (carpeta inexistente) ---");
            AccesoDatos imposible = new AccesoDatos("carpeta_que_no_existe/Socios.txt",
                    "carpeta_que_no_existe/Equipamiento.txt");
            try {
                imposible.grabarSocios(new HashMap<String, Socio>());
                comprobar("Debio fallar al escribir en una carpeta inexistente", false);
            } catch (PersistenciaException e) {
                comprobar("Lanza PersistenciaException de escritura", e.getMotivo() == MotivoFallo.ESCRITURA);
                comprobar("Informa el nombre del archivo", e.getArchivo().equals("carpeta_que_no_existe/Socios.txt"));
                comprobar("Conserva la causa tecnica original", e.getCause() != null);
            }

            System.out.println("\n--- 9. Mapa null ---");
            try {
                datos.grabarSocios(null);
                comprobar("Debio rechazar un mapa null", false);
            } catch (NullPointerException e) {
                comprobar("Mapa null se rechaza (NullPointerException de requireNonNull)", true);
            } catch (PersistenciaException e) {
                comprobar("Mapa null no debe llegar a PersistenciaException", false);
            }

            System.out.println("\n--- 10. Equipamiento.txt: archivo inexistente ---");
            try {
                RegistroEquipamiento vacioEq = datos.leerEquipamiento();
                comprobar("Sin archivo devuelve inventario y alquileres vacios",
                        vacioEq.getInventario().isEmpty() && vacioEq.getAlquileres().isEmpty());
            } catch (PersistenciaException e) {
                comprobar("Sin archivo de equipamiento NO debe lanzar excepcion", false);
            }

            System.out.println("\n--- 11. Equipamiento.txt: grabar y recuperar ---");
            try {
                Equipamiento raqueta = new Equipamiento("RQ-01", "Raqueta Pro", TipoEquipamiento.RAQUETA, 2, 500);
                Map<String, Equipamiento> inventario = new HashMap<String, Equipamiento>();
                inventario.put(raqueta.getCodigo(), raqueta);
                raqueta.marcarAlquilado();
                java.util.List<Alquiler> alquileres = new ArrayList<Alquiler>();
                alquileres.add(new Alquiler(1, "30111222", raqueta, LocalDate.of(2026, 10, 28)));
                datos.grabarEquipamiento(new RegistroEquipamiento(inventario, alquileres));
                comprobar("El archivo Equipamiento se creo en disco", archivoEquipo.exists());

                RegistroEquipamiento leido = datos.leerEquipamiento();
                comprobar("Se recupero 1 equipamiento y 1 alquiler",
                        leido.getInventario().size() == 1 && leido.getAlquileres().size() == 1);
                Alquiler a = leido.getAlquileres().get(0);
                comprobar("El alquiler conserva DNI y fecha limite (28/10 + 2 dias = 30/10)",
                        a.getDniSocio().equals("30111222") && a.getFechaLimite().equals(LocalDate.of(2026, 10, 30)));
                comprobar("El alquiler apunta al MISMO equipamiento del inventario leido",
                        a.getEquipamiento() == leido.getInventario().get("RQ-01"));
                comprobar("El equipamiento sigue marcado como no disponible",
                        !leido.getInventario().get("RQ-01").isDisponible());
            } catch (PersistenciaException e) {
                comprobar("Grabar y leer equipamiento no debe fallar (archivo " + e.getArchivo() + ")", false);
            }

            System.out.println("\n--- 12. Equipamiento.txt danado ---");
            try (FileOutputStream basura = new FileOutputStream(archivoEquipo)) {
                basura.write("basura".getBytes());
            }
            try {
                datos.leerEquipamiento();
                comprobar("Debio lanzar PersistenciaException", false);
            } catch (PersistenciaException e) {
                comprobar("Equipamiento danado -> PersistenciaException de formato",
                        e.getMotivo() == MotivoFallo.FORMATO_INVALIDO);
            }

        } finally {
            archivo.delete(); // limpia el archivo temporal
            archivoEquipo.delete();
        }

        System.out.println("\n=== " + (fallos == 0 ? "TODAS LAS PRUEBAS OK" : fallos + " PRUEBA(S) FALLARON") + " ===");
    }

    private static void probarLecturaFallida(AccesoDatos datos, MotivoFallo tipoEsperado, String caso) {
        try {
            datos.leerSocios();
            comprobar(caso + ": debio lanzar PersistenciaException", false);
        } catch (PersistenciaException e) {
            boolean coincide = e.getMotivo() == tipoEsperado;
            comprobar(caso + " -> PersistenciaException (" + tipoEsperado + ")", coincide);
        }
    }

    private static void comprobar(String descripcion, boolean condicion) {
        System.out.println((condicion ? "[OK]    " : "[FALLO] ") + descripcion);
        if (!condicion) {
            fallos++;
        }
    }
}
