package Pruebas;

import Datos.AccesoDatos;
import Datos.MotivoFallo;
import Datos.PersistenciaException;
import LogicaNegocio.AccesoDenegadoException;
import LogicaNegocio.Advertencia;
import LogicaNegocio.Alquiler;
import LogicaNegocio.AlquilerNoEncontradoException;
import LogicaNegocio.Equipamiento;
import LogicaNegocio.EquipamientoDuplicadoException;
import LogicaNegocio.EquipamientoNoDisponibleException;
import LogicaNegocio.EquipamientoNoEncontradoException;
import LogicaNegocio.FechaInvalidaException;
import LogicaNegocio.Logica;
import LogicaNegocio.MotivoAcceso;
import LogicaNegocio.Socio;
import LogicaNegocio.SocioConAlquilerVencidoException;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioNoEncontradoException;
import LogicaNegocio.SocioVIP;
import LogicaNegocio.TipoEquipamiento;

import java.io.File;

import java.time.LocalDate;
import java.util.List;

public class PruebaEquipamiento {

    private static int fallos = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PRUEBA TS2-01: EQUIPAMIENTO, ALQUILERES Y DEVOLUCION ===");

        File archivoSocios = File.createTempFile("socios_equipo", ".dat");
        archivoSocios.delete();
        File archivoEquipo = File.createTempFile("equipo_equipo", ".dat");
        archivoEquipo.delete();

        LocalDate hoy = LocalDate.of(2026, 10, 28);

        try {
            AccesoDatos datos = new AccesoDatos(archivoSocios.getPath(), archivoEquipo.getPath());
            Logica logica = new Logica(datos);

            Socio carlos = new SocioEstandar("30111222", "Carlos", "Gomez", "c@mail.com", "11-1111", 10000);
            Socio valeria = new SocioVIP("32333444", "Valeria", "Lopez", "v@mail.com", "11-2222", 10000, 4000);
            logica.darDeAlta(carlos);
            logica.darDeAlta(valeria);

            System.out.println("\n--- 1. Alta de equipamiento ---");
            Equipamiento raqueta = new Equipamiento("RQ-01", "Raqueta Pro", TipoEquipamiento.RAQUETA, 2, 500);
            Equipamiento locker = new Equipamiento("LK-07", "Locker 7", TipoEquipamiento.LOCKER, 1, 300);
            Equipamiento pelota = new Equipamiento("PL-03", "Pelota de tenis", TipoEquipamiento.PELOTA, 3, 200);
            logica.registrarEquipamiento(raqueta);
            logica.registrarEquipamiento(locker);
            logica.registrarEquipamiento(pelota);
            comprobar("Se registraron 3 equipamientos", logica.totalEquipamiento() == 3);
            comprobar("Quedan disponibles al registrarse", raqueta.isDisponible() && locker.isDisponible());
            comprobar("Se creo Equipamiento.txt", archivoEquipo.exists());
            try {
                logica.registrarEquipamiento(new Equipamiento("RQ-01", "Otra", TipoEquipamiento.RAQUETA, 1, 1));
                comprobar("Debio rechazar el codigo duplicado", false);
            } catch (EquipamientoDuplicadoException e) {
                comprobar("Codigo repetido -> EquipamientoDuplicadoException", e.getCodigo().equals("RQ-01"));
            }
            comprobar("El listado esta ordenado por codigo (LK-07, PL-03, RQ-01)",
                    logica.listadoEquipamientoOrdenado().get(0) == locker
                            && logica.listadoEquipamientoOrdenado().get(2) == raqueta);

            System.out.println("\n--- 2. Alquiler correcto ---");
            Alquiler a1 = logica.alquilar("30111222", "RQ-01", hoy);
            comprobar("Se registro el alquiler #1", a1.getNumero() == 1 && logica.totalAlquileres() == 1);
            comprobar("El equipamiento paso a no disponible", !raqueta.isDisponible());
            comprobar("Fecha limite = fecha de alquiler + 2 dias de prestamo",
                    a1.getFechaLimite().equals(hoy.plusDays(2)));
            comprobar("Aparece como alquiler activo", logica.alquileresActivos().size() == 1);

            System.out.println("\n--- 3. Reglas que rechazan el alquiler ---");
            try {
                logica.alquilar("32333444", "RQ-01", hoy);
                comprobar("Debio rechazar un equipamiento ya alquilado", false);
            } catch (EquipamientoNoDisponibleException e) {
                comprobar("Equipamiento ocupado -> EquipamientoNoDisponibleException", e.getCodigo().equals("RQ-01"));
            }
            try {
                logica.alquilar("99999999", "LK-07", hoy);
                comprobar("Debio rechazar un socio inexistente", false);
            } catch (SocioNoEncontradoException e) {
                comprobar("Socio inexistente -> SocioNoEncontradoException", true);
            }
            try {
                logica.alquilar("32333444", "XX-99", hoy);
                comprobar("Debio rechazar un equipamiento inexistente", false);
            } catch (EquipamientoNoEncontradoException e) {
                comprobar("Equipamiento inexistente -> EquipamientoNoEncontradoException", true);
            }
            valeria.marcarMora();
            try {
                logica.alquilar("32333444", "LK-07", hoy);
                comprobar("Debio rechazar a un socio moroso", false);
            } catch (AccesoDenegadoException e) {
                comprobar("Socio con cuotas impagas -> AccesoDenegadoException",
                        e.getMotivo() == MotivoAcceso.CUOTAS_IMPAGAS);
            }
            valeria.registrarPagoCuota();
            valeria.aplicarSancion("Prueba");
            try {
                logica.alquilar("32333444", "LK-07", hoy);
                comprobar("Debio rechazar a un socio sancionado", false);
            } catch (AccesoDenegadoException e) {
                comprobar("Socio sancionado -> AccesoDenegadoException", e.getMotivo() == MotivoAcceso.SANCION_VIGENTE);
            }
            valeria.levantarSancion();
            comprobar("Ninguno de los rechazos creo alquileres", logica.totalAlquileres() == 1);

            System.out.println("\n--- 4. Devolucion en termino (sin recargo) ---");
            Alquiler d1 = logica.devolver("RQ-01", hoy.plusDays(2)); // justo el dia limite
            comprobar("Se marco como devuelto", d1.isDevuelto() && logica.alquileresActivos().isEmpty());
            comprobar("Sin recargo", d1.getRecargo() == 0);
            comprobar("El equipamiento volvio a estar disponible", raqueta.isDisponible());
            comprobar("El socio no tiene recargo ni advertencias",
                    carlos.getRecargoPendiente() == 0 && carlos.getAdvertencias().isEmpty());

            System.out.println("\n--- 5. Devolucion tardia (recargo + advertencia) ---");
            logica.alquilar("30111222", "RQ-01", hoy); // limite: hoy + 2
            Alquiler d2 = logica.devolver("RQ-01", hoy.plusDays(5)); // 3 dias tarde x $500
            comprobar("3 dias de atraso", d2.diasDeAtraso(hoy.plusDays(5)) == 3);
            comprobar("Recargo = 3 x 500 = 1500", d2.getRecargo() == 1500.0);
            comprobar("El recargo queda pendiente en el socio para la siguiente factura",
                    carlos.getRecargoPendiente() == 1500.0);
            comprobar("Se genero 1 advertencia", carlos.getAdvertencias().size() == 1);
            Advertencia adv = carlos.getAdvertencias().get(0);
            comprobar("La advertencia guarda equipamiento, dias de atraso y recargo",
                    adv.getCodigoEquipamiento().equals("RQ-01") && adv.getDiasDeAtraso() == 3
                            && adv.getRecargo() == 1500.0);
            comprobar("El equipamiento volvio a estar disponible", raqueta.isDisponible());

            System.out.println("\n--- 6. Devoluciones invalidas ---");
            try {
                logica.devolver("RQ-01", hoy.plusDays(6));
                comprobar("Debio fallar: no tiene alquiler activo", false);
            } catch (AlquilerNoEncontradoException e) {
                comprobar("Sin alquiler activo -> AlquilerNoEncontradoException", true);
            }
            logica.alquilar("32333444", "LK-07", hoy);
            try {
                logica.devolver("LK-07", hoy.minusDays(1));
                comprobar("Debio rechazar una fecha anterior al alquiler", false);
            } catch (FechaInvalidaException e) {
                comprobar("Fecha anterior al alquiler -> FechaInvalidaException", true);
            }
            comprobar("Tras el error el alquiler sigue activo y el locker ocupado",
                    !locker.isDisponible() && logica.alquileresActivos().size() == 1);

            System.out.println("\n--- 7. Socio con alquiler vencido no puede retirar mas ---");
            // el locker se alquilo "hoy" con 1 dia de plazo; 5 dias despues esta vencido
            comprobar("tieneAlquilerVencido detecta el vencimiento",
                    logica.tieneAlquilerVencido("32333444", hoy.plusDays(5)));
            try {
                logica.alquilar("32333444", "PL-03", hoy.plusDays(5));
                comprobar("Debio rechazar: tiene equipamiento vencido", false);
            } catch (SocioConAlquilerVencidoException e) {
                comprobar("Alquiler vencido sin devolver -> SocioConAlquilerVencidoException",
                        e.getDni().equals("32333444"));
            }
            logica.devolver("LK-07", hoy.plusDays(5)); // 4 dias tarde x $300 = 1200
            comprobar("Valeria acumulo recargo de 1200", valeria.getRecargoPendiente() == 1200.0);

            System.out.println("\n--- 8. Persistencia: se 'reinicia' el programa ---");
            logica.alquilar("30111222", "PL-03", hoy.plusDays(6)); // queda un alquiler activo
            Logica reiniciada = new Logica(new AccesoDatos(archivoSocios.getPath(), archivoEquipo.getPath()));
            comprobar("Se recuperaron los 3 equipamientos", reiniciada.totalEquipamiento() == 3);
            comprobar("Se recuperaron los 4 alquileres", reiniciada.totalAlquileres() == 4);
            List<Alquiler> activos = reiniciada.alquileresActivos();
            comprobar("Sigue 1 alquiler activo (la pelota)",
                    activos.size() == 1 && activos.get(0).getEquipamiento().getCodigo().equals("PL-03"));
            comprobar("El alquiler apunta al mismo Equipamiento del inventario",
                    activos.get(0).getEquipamiento() == reiniciada.consultarEquipamiento("PL-03"));
            comprobar("La pelota quedo no disponible y la raqueta disponible",
                    !reiniciada.consultarEquipamiento("PL-03").isDisponible()
                            && reiniciada.consultarEquipamiento("RQ-01").isDisponible());
            comprobar("Se conserva el recargo y la advertencia de Carlos",
                    reiniciada.consultarPorDni("30111222").getRecargoPendiente() == 1500.0
                            && reiniciada.consultarPorDni("30111222").getAdvertencias().size() == 1);
            comprobar("Se conserva el recargo de Valeria",
                    reiniciada.consultarPorDni("32333444").getRecargoPendiente() == 1200.0);
            Alquiler nuevo = reiniciada.alquilar("32333444", "RQ-01", hoy.plusDays(7));
            comprobar("La numeracion continua despues de reiniciar (#5)", nuevo.getNumero() == 5);

            System.out.println("\n--- 9. Si falla el archivo, el alta se deshace ---");
            Logica sinDisco = new Logica(
                    new AccesoDatos(archivoSocios.getPath(), "carpeta_que_no_existe/Equipamiento.txt"));
            try {
                sinDisco.registrarEquipamiento(new Equipamiento("ZZ-01", "Prueba", TipoEquipamiento.OTRO, 1, 1));
                comprobar("Debio lanzar PersistenciaException", false);
            } catch (PersistenciaException e) {
                comprobar("Falla de archivo -> PersistenciaException de escritura",
                        e.getMotivo() == MotivoFallo.ESCRITURA);
            }
            comprobar("Rollback: el equipamiento no quedo en memoria", sinDisco.totalEquipamiento() == 0);

            System.out.println("\n--- 10. Datos invalidos en Equipamiento ---");
            try {
                new Equipamiento("A", "B", TipoEquipamiento.OTRO, 0, 1);
                comprobar("Debio rechazar 0 dias de prestamo", false);
            } catch (IllegalArgumentException e) {
                comprobar("0 dias de prestamo -> IllegalArgumentException", true);
            }
            try {
                new Equipamiento("   ", "B", TipoEquipamiento.OTRO, 1, 1);
                comprobar("Debio rechazar codigo vacio", false);
            } catch (IllegalArgumentException e) {
                comprobar("Codigo vacio -> IllegalArgumentException", true);
            }

        } finally {
            archivoSocios.delete();
            archivoEquipo.delete();
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
