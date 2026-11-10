package Pruebas;

import Datos.AccesoDatos;
import Datos.PersistenciaException;
import LogicaNegocio.DniInvalidoException;
import LogicaNegocio.Logica;
import LogicaNegocio.OcupacionActividad;
import LogicaNegocio.Socio;
import LogicaNegocio.SocioBecado;
import LogicaNegocio.SocioDuplicadoException;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioNoEncontradoException;
import LogicaNegocio.SocioVIP;
import LogicaNegocio.TipoFactura;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PruebaReportes {

    private static int fallos = 0;

    public static void main(String[] args) throws IOException, PersistenciaException {
        System.out.println("=== PRUEBA REPORTES (Collections.sort + binarySearch) ===");

        File archivoSocios = File.createTempFile("socios_reportes", ".dat");
        archivoSocios.delete();
        File archivoEquipo = File.createTempFile("equipo_reportes", ".dat");
        archivoEquipo.delete();
        File archivoReservas = File.createTempFile("reservas_reportes", ".dat");
        archivoReservas.delete();
        File archivoFacturas = File.createTempFile("facturas_reportes", ".dat");
        archivoFacturas.delete();

        try {
            Logica logica = new Logica(new AccesoDatos(archivoSocios.getPath(), archivoEquipo.getPath(),
                    archivoReservas.getPath(), archivoFacturas.getPath()));

            System.out.println("\n--- Reportes sin datos ---");
            comprobar("Sin socios: reporte por apellido vacio", logica.reporteSociosPorApellidoNombre().isEmpty());
            comprobar("Sin socios: busqueda por apellido vacia", logica.buscarPorApellido("Gomez").isEmpty());
            comprobar("Sin socios: busqueda por cuota vacia", logica.buscarPorCuota(10000).isEmpty());
            comprobar("Sin reservas: ocupacion vacia", logica.reporteOcupacionActividades().isEmpty());
            comprobar("Sin facturas: recaudacion vacia", logica.recaudacionPorPeriodo().isEmpty());

            double base = logica.valorBaseCuota();
            Socio perez = new SocioEstandar("30111222", "Carlos", "Pérez", "c@mail.com", "11-1111", base);
            Socio gomezA = new SocioEstandar("32333444", "Ana", "Gomez", "a@mail.com", "11-2222", base);
            Socio gomezB = new SocioVIP("35555666", "Beto", "Gomez", "b@mail.com", "11-3333", base, 4000);
            Socio alvarez = new SocioBecado("28999000", "Lucia", "Alvarez", "l@mail.com", "11-4444", base, 50);
            Socio zarate = new SocioEstandar("40111000", "Zoe", "Zarate", "z@mail.com", "11-5555", base);
            try {
                logica.darDeAlta(perez);
                logica.darDeAlta(zarate);
                logica.darDeAlta(gomezB);
                logica.darDeAlta(alvarez);
                logica.darDeAlta(gomezA);
            } catch (SocioDuplicadoException | DniInvalidoException e) {
                comprobar("Las altas validas no deben fallar", false);
            }

            System.out.println("\n--- Orden por apellido y nombre ---");
            List<Socio> porApellido = logica.reporteSociosPorApellidoNombre();
            comprobar("Hay 5 socios", porApellido.size() == 5);
            comprobar("Orden: Alvarez, Gomez Ana, Gomez Beto, Pérez, Zarate",
                    porApellido.get(0) == alvarez && porApellido.get(1) == gomezA && porApellido.get(2) == gomezB
                            && porApellido.get(3) == perez && porApellido.get(4) == zarate);

            System.out.println("\n--- Orden por monto de cuota ---");
            List<Socio> porCuota = logica.reporteSociosPorCuota();
            boolean ascendente = true;
            for (int i = 1; i < porCuota.size(); i++) {
                if (porCuota.get(i - 1).calcularCuotaMensual() > porCuota.get(i).calcularCuotaMensual()) {
                    ascendente = false;
                }
            }
            comprobar("Las cuotas quedan de menor a mayor", ascendente);
            comprobar("La cuota mas baja es la del becado (50%)", porCuota.get(0) == alvarez);
            comprobar("La cuota mas alta es la del VIP", porCuota.get(4) == gomezB);

            System.out.println("\n--- Busqueda binaria por apellido ---");
            List<Socio> gomez = logica.buscarPorApellido("Gomez");
            comprobar("Existente con 2 socios: devuelve los 2", gomez.size() == 2);
            comprobar("Son Ana y Beto", gomez.contains(gomezA) && gomez.contains(gomezB));
            comprobar("Existente (primer elemento de la lista): Alvarez", logica.buscarPorApellido("Alvarez").size() == 1);
            comprobar("Existente (ultimo elemento de la lista): Zarate", logica.buscarPorApellido("Zarate").size() == 1);
            comprobar("Ignora mayusculas y espacios: '  gomez '", logica.buscarPorApellido("  gomez ").size() == 2);
            comprobar("Ignora tildes: 'Perez' encuentra 'Pérez'", logica.buscarPorApellido("Perez").size() == 1);
            comprobar("No existente (esta en el medio): 'Martinez'", logica.buscarPorApellido("Martinez").isEmpty());
            comprobar("No existente (antes del primero): 'Abad'", logica.buscarPorApellido("Abad").isEmpty());
            comprobar("No existente (despues del ultimo): 'Zuniga'", logica.buscarPorApellido("Zuniga").isEmpty());

            System.out.println("\n--- Busqueda binaria por monto de cuota ---");
            double cuotaVip = gomezB.calcularCuotaMensual();
            comprobar("Existente: cuota de 10000 la pagan 3 socios", logica.buscarPorCuota(base).size() == 3);
            comprobar("Existente: la cuota del VIP la paga solo el VIP",
                    logica.buscarPorCuota(cuotaVip).size() == 1 && logica.buscarPorCuota(cuotaVip).get(0) == gomezB);
            comprobar("No existente: nadie paga 12345", logica.buscarPorCuota(12345).isEmpty());
            comprobar("No existente: nadie paga 0", logica.buscarPorCuota(0).isEmpty());

            System.out.println("\n--- Morosos y sancionados ---");
            comprobar("Al principio no hay morosos", logica.reporteMorososYSancionados().isEmpty());
            zarate.marcarMora();
            alvarez.aplicarSancion("Material no devuelto");
            List<Socio> morosos = logica.reporteMorososYSancionados();
            comprobar("Ahora hay 2 (uno moroso y uno sancionado)", morosos.size() == 2);
            comprobar("Salen ordenados por apellido: Alvarez, Zarate", morosos.get(0) == alvarez && morosos.get(1) == zarate);
            comprobar("El socio al dia y sin sancion no aparece", !morosos.contains(perez));

            System.out.println("\n--- Ocupacion de actividades ---");
            try {
                logica.reservar("30111222", "Natacion", "Lunes 18:00");
                logica.reservar("32333444", "Natacion", "Lunes 18:00");
                logica.reservar("35555666", "Natacion", "Lunes 18:00");
                logica.reservar("30111222", "Spinning", "Martes 19:00");
                logica.reservar("32333444", "Tenis", "Jueves 10:00");
            } catch (Exception e) {
                comprobar("Las reservas validas no deben fallar", false);
            }
            List<OcupacionActividad> ocupacion = logica.reporteOcupacionActividades();
            comprobar("Hay 3 actividades", ocupacion.size() == 3);
            comprobar("La mas ocupada es Natacion con 3", ocupacion.get(0).getActividad().equals("Natacion")
                    && ocupacion.get(0).getReservas() == 3);
            comprobar("Spinning y Tenis empatan con 1 y salen por nombre", ocupacion.get(1).getActividad().equals("Spinning")
                    && ocupacion.get(2).getActividad().equals("Tenis"));

            System.out.println("\n--- Recaudacion mensual ---");
            try {
                logica.emitirFactura("30111222", TipoFactura.CUOTA, 10000, LocalDate.of(2026, 11, 5));
                logica.emitirFactura("32333444", TipoFactura.CUOTA, 10000, LocalDate.of(2026, 11, 6));
                logica.emitirFactura("32333444", TipoFactura.ALQUILER, 800, LocalDate.of(2026, 10, 20));
                logica.emitirFactura("35555666", TipoFactura.CUOTA, 14000, LocalDate.of(2026, 12, 1));
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("Las facturas validas no deben fallar", false);
            }
            comprobar("Noviembre 2026 recaudo 20000", logica.recaudacionDelPeriodo(2026, 11) == 20000);
            comprobar("Octubre 2026 recaudo 800", logica.recaudacionDelPeriodo(2026, 10) == 800);
            comprobar("Un mes sin facturas recauda 0", logica.recaudacionDelPeriodo(2026, 1) == 0);
            Map<String, Double> porPeriodo = logica.recaudacionPorPeriodo();
            comprobar("Hay 3 periodos", porPeriodo.size() == 3);
            comprobar("Los periodos salen en orden cronologico",
                    porPeriodo.keySet().toString().equals("[2026/10, 2026/11, 2026/12]"));

        } finally {
            archivoSocios.delete();
            archivoEquipo.delete();
            archivoReservas.delete();
            archivoFacturas.delete();
        }

        System.out.println("\n=== " + (fallos == 0 ? "TODAS LAS PRUEBAS OK" : fallos + " PRUEBA(S) FALLARON") + " ===");
    }

    private static void comprobar(String descripcion, boolean condicion) {
        if (condicion) {
            System.out.println("[OK]    " + descripcion);
        } else {
            System.out.println("[FALLA] " + descripcion);
            fallos++;
        }
    }
}
