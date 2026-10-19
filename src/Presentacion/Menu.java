package Presentacion;

import Datos.PersistenciaException;
import LogicaNegocio.DniInvalidoException;
import LogicaNegocio.Logica;
import LogicaNegocio.Socio;
import LogicaNegocio.SocioBecado;
import LogicaNegocio.SocioDuplicadoException;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioNoEncontradoException;
import LogicaNegocio.SocioVIP;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Menu {

    private final Logica logica;
    private final Scanner teclado = new Scanner(System.in);

    public Menu(Logica logica) {
        this.logica = logica;
    }

    public void ejecutar() {
        int opcion = -1;
        try {
            do {
                mostrar(Mensajes.menuPrincipal());
                opcion = pedirEntero(Mensajes.PEDIR_OPCION);
                switch (opcion) {
                    case 1:
                        registrarSocio();
                        break;
                    case 2:
                        consultarSocio();
                        break;
                    case 3:
                        listarSocios();
                        break;
                    case 0:
                        mostrar(Mensajes.DESPEDIDA);
                        break;
                    default:
                        mostrar(Mensajes.OPCION_INVALIDA);
                }
            } while (opcion != 0);
        } catch (NoSuchElementException e) {

            mostrar(Mensajes.DESPEDIDA);
        }
    }

    private void registrarSocio() {
        String dni = pedirTexto(Mensajes.PEDIR_DNI);
        String nombre = pedirTexto(Mensajes.PEDIR_NOMBRE);
        String apellido = pedirTexto(Mensajes.PEDIR_APELLIDO);
        String email = pedirTexto(Mensajes.PEDIR_EMAIL);
        String telefono = pedirTexto(Mensajes.PEDIR_TELEFONO);
        int tipo = pedirEntero(Mensajes.PEDIR_TIPO);

        double base = logica.valorBaseCuota();

        Socio nuevo;
        switch (tipo) {
            case 1:
                nuevo = new SocioEstandar(dni, nombre, apellido, email, telefono, base);
                break;
            case 2:
                double adicional = pedirDecimal(Mensajes.PEDIR_ADICIONAL_VIP);
                nuevo = new SocioVIP(dni, nombre, apellido, email, telefono, base, adicional);
                break;
            case 3:
                double descuento = pedirPorcentaje(Mensajes.PEDIR_DESCUENTO);
                nuevo = new SocioBecado(dni, nombre, apellido, email, telefono, base, descuento);
                break;
            default:
                mostrar(Mensajes.TIPO_INVALIDO);
                return;
        }

        try {
            logica.darDeAlta(nuevo);
            mostrar(Mensajes.socioRegistrado(nuevo));
        } catch (SocioDuplicadoException e) {
            mostrar(Mensajes.errorDniDuplicado(e.getDni()));
        } catch (DniInvalidoException e) {
            mostrar(Mensajes.ERROR_DNI_INVALIDO);
        } catch (PersistenciaException e) {
            mostrar(Mensajes.errorPersistencia(e));
        }
    }

    private void consultarSocio() {
        String dni = pedirTexto(Mensajes.PEDIR_DNI);
        try {
            Socio encontrado = logica.consultarPorDni(dni);
            mostrar(Mensajes.fichaSocio(encontrado));
        } catch (SocioNoEncontradoException e) {
            mostrar(Mensajes.errorSocioNoEncontrado(e.getDni()));
        } catch (DniInvalidoException e) {
            mostrar(Mensajes.ERROR_DNI_INVALIDO);
        }
    }

    private void listarSocios() {
        List<Socio> socios = logica.listadoOrdenadoPorDni();
        if (socios.isEmpty()) {
            mostrar(Mensajes.SIN_SOCIOS);
            return;
        }
        mostrar(Mensajes.ENCABEZADO_LISTADO);
        for (Socio s : socios) {
            mostrar(Mensajes.lineaListado(s));
        }
    }

    private void mostrar(String texto) {
        System.out.println(texto);
    }

    private String pedirTexto(String pregunta) {
        while (true) {
            System.out.print(pregunta);
            String ingresado = teclado.nextLine().trim();
            if (!ingresado.isEmpty()) {
                return ingresado;
            }
            mostrar(Mensajes.DATO_VACIO);
        }
    }

    private int pedirEntero(String pregunta) {
        System.out.print(pregunta);
        try {
            return Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double pedirDecimal(String pregunta) {
        while (true) {
            System.out.print(pregunta);
            try {
                double valor = Double.parseDouble(teclado.nextLine().trim().replace(',', '.'));
                if (valor >= 0) {
                    return valor;
                }
            } catch (NumberFormatException e) {

            }
            mostrar(Mensajes.NUMERO_INVALIDO);
        }
    }

    private double pedirPorcentaje(String pregunta) {
        while (true) {
            double valor = pedirDecimal(pregunta);
            if (valor <= 100) {
                return valor;
            }
            mostrar(Mensajes.PORCENTAJE_INVALIDO);
        }
    }
}
