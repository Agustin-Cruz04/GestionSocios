package Pruebas;

import Datos.AccesoDatos;
import Datos.MotivoFallo;
import Datos.PersistenciaException;
import LogicaNegocio.DniInvalidoException;
import LogicaNegocio.Logica;
import LogicaNegocio.Reserva;
import LogicaNegocio.ReservaDuplicadaException;
import LogicaNegocio.Socio;
import LogicaNegocio.SocioDuplicadoException;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioNoEncontradoException;

import java.io.File;
import java.io.IOException;

public class PruebaReservas {

    private static int fallos = 0;

    public static void main(String[] args) throws IOException, PersistenciaException {
        System.out.println("=== PRUEBA RESERVAS (MAP + SET) ===");

        File archivo = File.createTempFile("socios_reservas", ".dat");
        archivo.delete();
        File archivoEquipo = File.createTempFile("equipo_reservas", ".dat");
        archivoEquipo.delete();
        File archivoReservas = File.createTempFile("reservas_reservas", ".dat");
        archivoReservas.delete();
        File archivoFacturas = File.createTempFile("facturas_prueba", ".dat");
        archivoFacturas.delete();

        try {
            Logica logica = new Logica(new AccesoDatos(archivo.getPath(), archivoEquipo.getPath(),
                    archivoReservas.getPath(), archivoFacturas.getPath()));
            Socio carlos = new SocioEstandar("30111222", "Carlos", "Gomez", "c@mail.com", "11-1111",
                    logica.valorBaseCuota());
            Socio valeria = new SocioEstandar("32333444", "Valeria", "Lopez", "v@mail.com", "11-2222",
                    logica.valorBaseCuota());
            try {
                logica.darDeAlta(carlos);
                logica.darDeAlta(valeria);
            } catch (SocioDuplicadoException | DniInvalidoException e) {
                comprobar("Las altas validas no deben fallar", false);
            }

            System.out.println("\n--- Equals y hashCode de Reserva ---");
            Reserva a = new Reserva("30111222", "Natacion", "Lunes 18:00");
            Reserva b = new Reserva("30111222", "Natacion", "Lunes 18:00");
            Reserva c = new Reserva("30111222", "Natacion", "Martes 18:00");
            comprobar("Dos reservas con los mismos datos son iguales", a.equals(b));
            comprobar("Dos reservas iguales tienen el mismo hashCode", a.hashCode() == b.hashCode());
            comprobar("Distinto horario -> reservas distintas", !a.equals(c));

            System.out.println("\n--- Reservas validas ---");
            try {
                logica.reservar("30111222", "Natacion", "Lunes 18:00");
                logica.reservar("32333444", "Natacion", "Lunes 18:00");
                logica.reservar("30111222", "Natacion", "Martes 18:00");
                logica.reservar("30111222", "Spinning", "Lunes 18:00");
            } catch (SocioNoEncontradoException | DniInvalidoException | ReservaDuplicadaException e) {
                comprobar("Las reservas validas no deben fallar", false);
            }
            comprobar("Hay 4 reservas en total", logica.totalReservas() == 4);
            comprobar("Natacion tiene 3 reservas", logica.reservasDeActividad("Natacion").size() == 3);
            comprobar("Spinning tiene 1 reserva", logica.reservasDeActividad("Spinning").size() == 1);
            comprobar("Una actividad sin reservas devuelve un conjunto vacio",
                    logica.reservasDeActividad("Tenis").isEmpty());

            System.out.println("\n--- Reserva duplicada ---");
            try {
                logica.reservar("30111222", "Natacion", "Lunes 18:00");
                comprobar("Debio rechazar la reserva duplicada", false);
            } catch (ReservaDuplicadaException e) {
                comprobar("Misma clase, mismo horario -> ReservaDuplicadaException",
                        e.getReserva().getDniSocio().equals("30111222"));
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("El socio existe y el DNI es valido", false);
            }
            try {
                logica.reservar("  30111222 ", " Natacion ", " Lunes 18:00 ");
                comprobar("Debio rechazar el duplicado con espacios", false);
            } catch (ReservaDuplicadaException e) {
                comprobar("Los espacios sobrantes no evitan el duplicado", true);
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("El socio existe y el DNI es valido", false);
            }
            comprobar("La cantidad de reservas sigue en 4", logica.totalReservas() == 4);

            System.out.println("\n--- Socio inexistente o DNI vacio ---");
            try {
                logica.reservar("99999999", "Natacion", "Lunes 18:00");
                comprobar("Debio rechazar al socio inexistente", false);
            } catch (SocioNoEncontradoException e) {
                comprobar("DNI inexistente -> SocioNoEncontradoException", e.getDni().equals("99999999"));
            } catch (DniInvalidoException | ReservaDuplicadaException e) {
                comprobar("Error equivocado para un socio inexistente", false);
            }
            try {
                logica.reservar("  ", "Natacion", "Lunes 18:00");
                comprobar("Debio rechazar el DNI vacio", false);
            } catch (DniInvalidoException e) {
                comprobar("DNI vacio -> DniInvalidoException", true);
            } catch (SocioNoEncontradoException | ReservaDuplicadaException e) {
                comprobar("Error equivocado para un DNI vacio", false);
            }
            comprobar("Ninguna reserva invalida se guardo", logica.totalReservas() == 4);

            System.out.println("\n--- Persistencia: las reservas sobreviven a un reinicio ---");
            comprobar("Se creo Reservas.txt", archivoReservas.exists());
            Logica reiniciada = new Logica(
                    new AccesoDatos(archivo.getPath(), archivoEquipo.getPath(), archivoReservas.getPath(),
                            archivoFacturas.getPath()));
            comprobar("Tras reiniciar hay 4 reservas", reiniciada.totalReservas() == 4);
            comprobar("Natacion sigue con 3 reservas", reiniciada.reservasDeActividad("Natacion").size() == 3);
            comprobar("La reserva de Carlos en Spinning sigue ahi",
                    reiniciada.reservasDeActividad("Spinning").contains(
                            new Reserva("30111222", "Spinning", "Lunes 18:00")));
            try {
                reiniciada.reservar("30111222", "Natacion", "Lunes 18:00");
                comprobar("Debio rechazar el duplicado de una reserva ya persistida", false);
            } catch (ReservaDuplicadaException e) {
                comprobar("El duplicado se detecta tambien despues de reiniciar", true);
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("El socio existe y el DNI es valido", false);
            }
            try {
                reiniciada.reservar("32333444", "Spinning", "Lunes 18:00");
            } catch (SocioNoEncontradoException | DniInvalidoException | ReservaDuplicadaException e) {
                comprobar("Una reserva nueva despues de reiniciar no debe fallar", false);
            }
            Logica otraVez = new Logica(
                    new AccesoDatos(archivo.getPath(), archivoEquipo.getPath(), archivoReservas.getPath(),
                            archivoFacturas.getPath()));
            comprobar("La reserva nueva tambien se guardo (5 en total)", otraVez.totalReservas() == 5);

            System.out.println("\n--- Persistencia: si falla el archivo, la reserva se deshace ---");
            Logica sinDisco = new Logica(new AccesoDatos(archivo.getPath(), archivoEquipo.getPath(),
                    "carpeta_que_no_existe/Reservas.txt", "carpeta_que_no_existe/Facturas.txt"));
            int antes = sinDisco.totalReservas();
            try {
                sinDisco.reservar("30111222", "Tenis", "Viernes 09:00");
                comprobar("Debio lanzar PersistenciaException", false);
            } catch (PersistenciaException e) {
                comprobar("Falla de archivo -> PersistenciaException de escritura",
                        e.getMotivo() == MotivoFallo.ESCRITURA);
            } catch (SocioNoEncontradoException | DniInvalidoException | ReservaDuplicadaException e) {
                comprobar("El error debe ser de persistencia", false);
            }
            comprobar("La reserva no quedo en memoria", sinDisco.totalReservas() == antes);
            comprobar("Una actividad nueva que no se pudo guardar no queda registrada",
                    sinDisco.reservasDeActividad("Tenis").isEmpty());

        } finally {
            archivo.delete();
            archivoEquipo.delete();
            archivoReservas.delete();
            archivoFacturas.delete();
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