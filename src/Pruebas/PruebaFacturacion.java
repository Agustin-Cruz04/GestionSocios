package Pruebas;

import Datos.AccesoDatos;
import Datos.MotivoFallo;
import Datos.PersistenciaException;
import LogicaNegocio.DniInvalidoException;
import LogicaNegocio.Factura;
import LogicaNegocio.Logica;
import LogicaNegocio.Socio;
import LogicaNegocio.SocioDuplicadoException;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioNoEncontradoException;
import LogicaNegocio.SocioVIP;
import LogicaNegocio.TipoFactura;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PruebaFacturacion {

    private static int fallos = 0;

    public static void main(String[] args) throws IOException, PersistenciaException {
        System.out.println("=== PRUEBA FACTURACION (MAP + LIST) ===");

        File archivoSocios = File.createTempFile("socios_facturas", ".dat");
        archivoSocios.delete();
        File archivoEquipo = File.createTempFile("equipo_facturas", ".dat");
        archivoEquipo.delete();
        File archivoReservas = File.createTempFile("reservas_facturas", ".dat");
        archivoReservas.delete();
        File archivoFacturas = File.createTempFile("facturas_facturas", ".dat");
        archivoFacturas.delete();

        try {
            Logica logica = new Logica(
                    new AccesoDatos(archivoSocios.getPath(), archivoEquipo.getPath(), archivoReservas.getPath(), archivoFacturas.getPath()));
            comprobar("Primer arranque sin archivo: empieza con 0 facturas", logica.totalFacturas() == 0);

            Socio carlos = new SocioEstandar("30111222", "Carlos", "Gomez", "c@mail.com", "11-1111",
                    logica.valorBaseCuota());
            Socio valeria = new SocioVIP("32333444", "Valeria", "Lopez", "v@mail.com", "11-2222",
                    logica.valorBaseCuota(), 4000);
            try {
                logica.darDeAlta(carlos);
                logica.darDeAlta(valeria);
            } catch (SocioDuplicadoException | DniInvalidoException e) {
                comprobar("Las altas validas no deben fallar", false);
            }

            LocalDate noviembre = LocalDate.of(2026, 11, 5);
            LocalDate diciembre = LocalDate.of(2026, 12, 5);

            System.out.println("\n--- Facturas de cuota ---");
            try {
                Factura f1 = logica.emitirFacturaCuota("30111222", noviembre);
                comprobar("Socio estandar: cuota de 10000", f1.getMonto() == 10000);
                comprobar("La primera factura es la numero 1", f1.getNumero() == 1);
                comprobar("El tipo es CUOTA", f1.getTipo() == TipoFactura.CUOTA);
                comprobar("Guarda el periodo (2026/11)", f1.getAnio() == 2026 && f1.getMes() == 11);

                Factura f2 = logica.emitirFacturaCuota("32333444", noviembre);
                comprobar("Socio VIP: cuota de 14000 (polimorfismo)", f2.getMonto() == 14000);
                comprobar("La numeracion avanza (#2)", f2.getNumero() == 2);
            } catch (SocioNoEncontradoException | DniInvalidoException | PersistenciaException e) {
                comprobar("Las facturas validas no deben fallar", false);
            }
            comprobar("Se creo el archivo Facturas", archivoFacturas.exists());

            System.out.println("\n--- Recargo pendiente por alquileres ---");
            carlos.sumarRecargo(1500);
            try {
                Factura conRecargo = logica.emitirFacturaCuota("30111222", diciembre);
                comprobar("La factura suma la cuota y el recargo (11500)", conRecargo.getMonto() == 11500);
                comprobar("El recargo pendiente queda en cero", carlos.getRecargoPendiente() == 0);
            } catch (SocioNoEncontradoException | DniInvalidoException | PersistenciaException e) {
                comprobar("La factura con recargo no debe fallar", false);
            }

            System.out.println("\n--- Otros tipos de factura ---");
            try {
                Factura alquiler = logica.emitirFactura("30111222", TipoFactura.ALQUILER, 800, diciembre);
                comprobar("Factura de alquiler por 800", alquiler.getMonto() == 800
                        && alquiler.getTipo() == TipoFactura.ALQUILER);
            } catch (SocioNoEncontradoException | DniInvalidoException | PersistenciaException e) {
                comprobar("La factura de alquiler no debe fallar", false);
            }
            comprobar("Hay 4 facturas en total", logica.totalFacturas() == 4);

            System.out.println("\n--- Historial por socio y periodo ---");
            try {
                List<Factura> deCarlos = logica.facturasDeSocio("30111222");
                comprobar("Carlos tiene 3 facturas", deCarlos.size() == 3);
                comprobar("Valeria tiene 1 factura", logica.facturasDeSocio("32333444").size() == 1);
                comprobar("Carlos en 2026/11 tiene 1 factura",
                        logica.facturasDeSocioEnPeriodo("30111222", 2026, 11).size() == 1);
                comprobar("Carlos en 2026/12 tiene 2 facturas",
                        logica.facturasDeSocioEnPeriodo("30111222", 2026, 12).size() == 2);
                comprobar("Carlos en 2026/10 no tiene facturas",
                        logica.facturasDeSocioEnPeriodo("30111222", 2026, 10).isEmpty());
                comprobar("Un DNI sin facturas devuelve una lista vacia",
                        logica.facturasDeSocio("99999999").isEmpty());
                deCarlos.clear();
                comprobar("Modificar la lista devuelta no altera el historial",
                        logica.facturasDeSocio("30111222").size() == 3);
            } catch (DniInvalidoException e) {
                comprobar("Las consultas validas no deben fallar", false);
            }

            System.out.println("\n--- Socio inexistente o DNI vacio ---");
            try {
                logica.emitirFacturaCuota("99999999", noviembre);
                comprobar("Debio rechazar al socio inexistente", false);
            } catch (SocioNoEncontradoException e) {
                comprobar("DNI inexistente -> SocioNoEncontradoException", e.getDni().equals("99999999"));
            } catch (DniInvalidoException | PersistenciaException e) {
                comprobar("Error equivocado para un socio inexistente", false);
            }
            try {
                logica.facturasDeSocio("  ");
                comprobar("Debio rechazar el DNI vacio", false);
            } catch (DniInvalidoException e) {
                comprobar("DNI vacio -> DniInvalidoException", true);
            }
            comprobar("Ninguna factura invalida se guardo", logica.totalFacturas() == 4);

            System.out.println("\n--- Persistencia (se 'reinicia' el programa) ---");
            Logica reiniciada = new Logica(
                    new AccesoDatos(archivoSocios.getPath(), archivoEquipo.getPath(), archivoReservas.getPath(), archivoFacturas.getPath()));
            comprobar("Al iniciar se cargan las 4 facturas del archivo", reiniciada.totalFacturas() == 4);
            try {
                comprobar("El historial de Carlos se recupera completo",
                        reiniciada.facturasDeSocio("30111222").size() == 3);
                comprobar("El recargo cobrado sigue en cero tras reiniciar",
                        reiniciada.consultarPorDni("30111222").getRecargoPendiente() == 0);
                Factura siguiente = reiniciada.emitirFacturaCuota("32333444", diciembre);
                comprobar("La numeracion continua despues de reiniciar (#5)", siguiente.getNumero() == 5);
            } catch (SocioNoEncontradoException | DniInvalidoException | PersistenciaException e) {
                comprobar("Tras reiniciar todo debe seguir funcionando", false);
            }

            System.out.println("\n--- Si falla el archivo, la factura se deshace ---");
            Logica sinDisco = new Logica(new AccesoDatos(archivoSocios.getPath(), archivoEquipo.getPath(),
                    archivoReservas.getPath(), "carpeta_que_no_existe/Facturas.txt"));
            int antes = sinDisco.totalFacturas();
            try {
                Socio conDeuda = sinDisco.consultarPorDni("30111222");
                conDeuda.sumarRecargo(700);
                sinDisco.emitirFacturaCuota("30111222", noviembre);
                comprobar("Debio lanzar PersistenciaException", false);
            } catch (PersistenciaException e) {
                comprobar("Falla de archivo -> PersistenciaException (ESCRITURA)",
                        e.getMotivo() == MotivoFallo.ESCRITURA);
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("El socio existe y el DNI es valido", false);
            }
            comprobar("Rollback: la factura no quedo en memoria", sinDisco.totalFacturas() == antes);
            try {
                comprobar("Rollback: el recargo vuelve al socio",
                        sinDisco.consultarPorDni("30111222").getRecargoPendiente() == 700);
            } catch (SocioNoEncontradoException | DniInvalidoException e) {
                comprobar("El socio debe seguir existiendo", false);
            }

        } finally {
            archivoSocios.delete();
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
