package LogicaNegocio;

import java.io.*;
import java.util.*;

public class PruebaSocios {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA SPRINT 1 - SOCIOS Y CUOTAS POLIMORFICAS ===");

        double cuotaBase = 10000;

        List<Socio> socios = new ArrayList<>();
        socios.add(new SocioEstandar("30111222", "Carlos", "Gomez", "carlos@mail.com", "11-4567-8901", cuotaBase));
        socios.add(new SocioVIP("32333444", "Valeria", "Lopez", "valeria@mail.com", "11-4567-8902", cuotaBase, 4000.0));
        socios.add(
                new SocioBecado("35555666", "Lucas", "Fernandez", "lucas@mail.com", "11-4567-8903", cuotaBase, 50.0));
        socios.add(
                new SocioBecado("36777888", "Martina", "Perez", "martina@mail.com", "11-4567-8904", cuotaBase, 100.0));

        System.out.println("\n--- Calculo Polimorfico de Cuotas ---");
        for (Socio s : socios) {
            System.out.println(s.getTipoMembresia() + " - DNI: " + s.getDni() + " " + s.getApellido() +
                    " | Cuota Final: $" + s.calcularCuotaMensual());
        }

        System.out.println("\n--- Prueba de Control de Acceso ---");
        Socio prueba = socios.get(0);
        System.out.println(
                "Acceso normal: " + prueba.isHabilitadoAcceso() + " (" + prueba.getMotivoRechazoAcceso() + ")");

        prueba.marcarMora();
        System.out.println("Con mora: " + prueba.isHabilitadoAcceso() + " (" + prueba.getMotivoRechazoAcceso() + ")");

        prueba.registrarPagoCuota();
        prueba.aplicarSancion("Devolucion tardia de equipamiento");
        System.out
                .println("Con sancion: " + prueba.isHabilitadoAcceso() + " (" + prueba.getMotivoRechazoAcceso() + ")");

        System.out.println("\n--- Prueba de Ordenamiento por DNI ---");
        Collections.sort(socios);
        for (Socio s : socios) {
            System.out.println(s.getDni() + " - " + s.getApellido() + ", " + s.getNombre());
        }

        System.out.println("\n--- Prueba de Serializacion en Memoria ---");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(socios.get(1));
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            Socio recuperado = (Socio) ois.readObject();
            ois.close();

            System.out.println("Socio recuperado OK: " + recuperado.getNombre() + " | Cuota: $"
                    + recuperado.calcularCuotaMensual());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
