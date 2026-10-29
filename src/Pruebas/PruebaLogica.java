package Pruebas;

import Datos.AccesoDatos;
import Datos.MotivoFallo;
import Datos.PersistenciaException;
import LogicaNegocio.DniInvalidoException;
import LogicaNegocio.Logica;
import LogicaNegocio.Socio;
import LogicaNegocio.SocioBecado;
import LogicaNegocio.SocioDuplicadoException;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioNoEncontradoException;
import LogicaNegocio.SocioVIP;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PruebaLogica {

    private static int fallos = 0;

    public static void main(String[] args) throws IOException, PersistenciaException {
        System.out.println("=== PRUEBA LOGICA (INDICE DE SOCIOS POR DNI) ===");

        File archivo = File.createTempFile("socios_logica", ".dat");
        archivo.delete();

        try {
            Logica logica = new Logica(new AccesoDatos(archivo.getPath()));
            comprobar("Primer arranque sin archivo: empieza con 0 socios", logica.totalSocios() == 0);

            Socio carlos = new SocioEstandar("30111222", "Carlos", "Gomez", "c@mail.com", "11-1111",
                    logica.valorBaseCuota());
            Socio valeria = new SocioVIP("32333444", "Valeria", "Lopez", "v@mail.com", "11-2222",
                    logica.valorBaseCuota(), 4000);
            Socio lucas = new SocioBecado("35555666", "Lucas", "Fernandez", "l@mail.com", "11-3333",
                    logica.valorBaseCuota(), 50);

            try {
                logica.darDeAlta(valeria);
                logica.darDeAlta(carlos);
                logica.darDeAlta(lucas);
            } catch (SocioDuplicadoException | DniInvalidoException e) {
                comprobar("Los altas validas no deben fallar", false);
            }
            comprobar("Se dieron de alta 3 socios", logica.totalSocios() == 3);
            comprobar("Se creo el archivo de datos", archivo.exists());

            System.out.println("\n--- Criterio 1: consultar por DNI ---");
            try {
                comprobar("Encuentra a Valeria", logica.consultarPorDni("32333444") == valeria);
                comprobar("Encuentra a Carlos", logica.consultarPorDni("30111222") == carlos);
                comprobar("Ignora espacios alrededor del DNI", logica.consultarPorDni("  30111222 ") == carlos);
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("Consultar un DNI existente no debe fallar", false);
            }
            try {
                logica.consultarPorDni("99999999");
                comprobar("Debio lanzar SocioNoEncontradoException", false);
            } catch (SocioNoEncontradoException e) {
                comprobar("DNI inexistente -> SocioNoEncontradoException con el DNI buscado",
                        e.getDni().equals("99999999"));
            } catch (DniInvalidoException e) {
                comprobar("Un DNI con numeros no es invalido", false);
            }
            comprobar("yaEstaRegistrado: true para existente, false para inexistente",
                    logica.yaEstaRegistrado("30111222") && !logica.yaEstaRegistrado("99999999"));

            System.out.println("\n--- Criterio 2: DNI duplicado ---");
            Socio impostor = new SocioEstandar("30111222", "Otro", "Distinto", "o@mail.com", "11-0000", 1);
            try {
                logica.darDeAlta(impostor);
                comprobar("Debio rechazar el DNI duplicado", false);
            } catch (SocioDuplicadoException e) {
                comprobar("DNI repetido -> SocioDuplicadoException", e.getDni().equals("30111222"));
            } catch (DniInvalidoException e) {
                comprobar("El DNI no es invalido", false);
            }
            comprobar("La cantidad sigue en 3", logica.totalSocios() == 3);
            try {
                comprobar("El socio original no fue pisado", logica.consultarPorDni("30111222") == carlos);
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("El socio original debe seguir existiendo", false);
            }

            System.out.println("\n--- DNI vacio ---");
            try {
                logica.consultarPorDni("   ");
                comprobar("Debio rechazar el DNI vacio", false);
            } catch (DniInvalidoException e) {
                comprobar("Consultar con DNI vacio -> DniInvalidoException", true);
            } catch (SocioNoEncontradoException e) {
                comprobar("Un DNI vacio no debe llegar a 'no encontrado'", false);
            }
            try {
                logica.darDeAlta(new SocioEstandar("", "X", "Y", "e", "t", 1));
                comprobar("Debio rechazar el alta con DNI vacio", false);
            } catch (DniInvalidoException e) {
                comprobar("Alta con DNI vacio -> DniInvalidoException", true);
            } catch (SocioDuplicadoException e) {
                comprobar("Un DNI vacio no debe llegar a 'duplicado'", false);
            }

            System.out.println("\n--- Listado ordenado por DNI ---");
            List<Socio> lista = logica.listadoOrdenadoPorDni();
            comprobar("Listado ordenado por DNI (Carlos, Valeria, Lucas)",
                    lista.get(0) == carlos && lista.get(1) == valeria && lista.get(2) == lucas);

            System.out.println("\n--- Criterio 3: persistencia (se 'reinicia' el programa) ---");
            Logica reiniciada = new Logica(new AccesoDatos(archivo.getPath()));
            comprobar("Al iniciar Logica se cargan los 3 socios del archivo", reiniciada.totalSocios() == 3);
            try {
                Socio recuperada = reiniciada.consultarPorDni("32333444");
                comprobar("Valeria recuperada sigue siendo VIP con cuota 14000",
                        recuperada.getTipoMembresia().equals("VIP") && recuperada.calcularCuotaMensual() == 14000);
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("Valeria debe existir tras reiniciar", false);
            }
            try {
                reiniciada.darDeAlta(new SocioEstandar("30111222", "Otro", "Mas", "o@mail.com", "11-0000", 1));
                comprobar("Debio rechazar el duplicado tambien tras reiniciar", false);
            } catch (SocioDuplicadoException e) {
                comprobar("Tras reiniciar tambien se rechaza el DNI duplicado", true);
            } catch (DniInvalidoException e) {
                comprobar("El DNI no es invalido", false);
            }

            System.out.println("\n--- Si falla el archivo, el alta se deshace ---");
            Logica sinDisco = new Logica(new AccesoDatos("carpeta_que_no_existe/Socios.txt"));
            try {
                sinDisco.darDeAlta(new SocioEstandar("111", "A", "B", "e", "t", 1));
                comprobar("Debio lanzar PersistenciaException", false);
            } catch (PersistenciaException e) {
                comprobar("Falla de archivo -> PersistenciaException (ESCRITURA)",
                        e.getMotivo() == MotivoFallo.ESCRITURA);
            } catch (SocioDuplicadoException | DniInvalidoException e) {
                comprobar("No debia ser duplicado ni invalido", false);
            }
            comprobar("Rollback: el socio no quedo en memoria", sinDisco.totalSocios() == 0);

        } finally {
            archivo.delete();
        }

        System.out.println("\n=== " + (fallos == 0 ? "TODAS LAS PRUEBAS OK" : fallos + " PRUEBA(S) FALLARON") + " ===");
    }

    private static void comprobar(String descripcion, boolean condicion) {
        System.out.println((condicion ? "[OK]    " : "[FALLO] ") + descripcion);
        if (!condicion) {
            fallos++;
        }
    }
}
