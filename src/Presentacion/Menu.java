package Presentacion;

import Datos.PersistenciaException;
import LogicaNegocio.AccesoDenegadoException;
import LogicaNegocio.Alquiler;
import LogicaNegocio.AlquilerNoEncontradoException;
import LogicaNegocio.DniInvalidoException;
import LogicaNegocio.Equipamiento;
import LogicaNegocio.EquipamientoDuplicadoException;
import LogicaNegocio.EquipamientoNoDisponibleException;
import LogicaNegocio.EquipamientoNoEncontradoException;
import LogicaNegocio.FechaInvalidaException;
import LogicaNegocio.Logica;
import LogicaNegocio.Reserva;
import LogicaNegocio.ReservaDuplicadaException;
import LogicaNegocio.Socio;
import LogicaNegocio.SocioBecado;
import LogicaNegocio.SocioConAlquilerVencidoException;
import LogicaNegocio.SocioDuplicadoException;
import LogicaNegocio.SocioEstandar;
import LogicaNegocio.SocioNoEncontradoException;
import LogicaNegocio.SocioVIP;
import LogicaNegocio.TipoEquipamiento;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class Menu {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
                    case 4:
                        registrarEquipamiento();
                        break;
                    case 5:
                        listarEquipamiento();
                        break;
                    case 6:
                        alquilarEquipamiento();
                        break;
                    case 7:
                        devolverEquipamiento();
                        break;
                    case 8:
                        verAlquileres();
                        break;
                    case 9:
                        reservarActividad();
                        break;
                    case 10:
                        verReservasDeActividad();
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

    // SOCIOS

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

    // EQUIPAMIENTO Y ALQUILERES

    private void registrarEquipamiento() {
        String codigo = pedirTexto(Mensajes.PEDIR_CODIGO_EQUIPO);
        String nombre = pedirTexto(Mensajes.PEDIR_NOMBRE_EQUIPO);
        TipoEquipamiento tipo = pedirTipoEquipamiento();
        int dias = pedirEnteroPositivo(Mensajes.PEDIR_DIAS_PRESTAMO);
        double recargo = pedirDecimal(Mensajes.PEDIR_RECARGO_DIA);

        Equipamiento nuevo = new Equipamiento(codigo, nombre, tipo, dias, recargo);
        try {
            logica.registrarEquipamiento(nuevo);
            mostrar(Mensajes.equipamientoRegistrado(nuevo));
        } catch (EquipamientoDuplicadoException e) {
            mostrar(Mensajes.errorEquipamientoDuplicado(e.getCodigo()));
        } catch (PersistenciaException e) {
            mostrar(Mensajes.errorPersistencia(e));
        }
    }

    private void listarEquipamiento() {
        List<Equipamiento> lista = logica.listadoEquipamientoOrdenado();
        if (lista.isEmpty()) {
            mostrar(Mensajes.SIN_EQUIPAMIENTO);
            return;
        }
        mostrar(Mensajes.ENCABEZADO_EQUIPAMIENTO);
        for (Equipamiento e : lista) {
            mostrar(Mensajes.lineaEquipamiento(e));
        }
    }

    private void alquilarEquipamiento() {
        String dni = pedirTexto(Mensajes.PEDIR_DNI_SOCIO);
        String codigo = pedirTexto(Mensajes.PEDIR_CODIGO_ALQUILER);
        try {
            Alquiler a = logica.alquilar(dni, codigo);
            mostrar(Mensajes.alquilerRegistrado(a));
        } catch (SocioNoEncontradoException e) {
            mostrar(Mensajes.errorSocioNoEncontrado(e.getDni()));
        } catch (DniInvalidoException e) {
            mostrar(Mensajes.ERROR_DNI_INVALIDO);
        } catch (EquipamientoNoEncontradoException e) {
            mostrar(Mensajes.errorEquipamientoNoEncontrado(e.getCodigo()));
        } catch (AccesoDenegadoException e) {
            mostrar(Mensajes.errorAccesoDenegado(e.getMotivo(), e.getMotivoSancion()));
        } catch (SocioConAlquilerVencidoException e) {
            mostrar(Mensajes.errorAlquilerVencido(e.getDni()));
        } catch (EquipamientoNoDisponibleException e) {
            mostrar(Mensajes.errorEquipamientoNoDisponible(e.getCodigo(), e.getNombre()));
        } catch (PersistenciaException e) {
            mostrar(Mensajes.errorPersistencia(e));
        }
    }

    private void devolverEquipamiento() {
        String codigo = pedirTexto(Mensajes.PEDIR_CODIGO_DEVOLUCION);
        LocalDate fecha = pedirFechaOpcional(Mensajes.PEDIR_FECHA_DEVOLUCION);
        try {
            Alquiler a = logica.devolver(codigo, fecha);
            mostrar(Mensajes.devolucionRegistrada(a));
            if (a.getRecargo() > 0) {
                mostrar(Mensajes.devolucionTardia(a));
            } else {
                mostrar(Mensajes.DEVUELTO_EN_TERMINO);
            }
        } catch (EquipamientoNoEncontradoException e) {
            mostrar(Mensajes.errorEquipamientoNoEncontrado(e.getCodigo()));
        } catch (AlquilerNoEncontradoException e) {
            mostrar(Mensajes.errorSinAlquilerActivo(e.getCodigo()));
        } catch (FechaInvalidaException e) {
            mostrar(Mensajes.ERROR_FECHA_DEVOLUCION);
        } catch (PersistenciaException e) {
            mostrar(Mensajes.errorPersistencia(e));
        }
    }

    private void verAlquileres() {
        List<Alquiler> lista = logica.listadoAlquileres();
        if (lista.isEmpty()) {
            mostrar(Mensajes.SIN_ALQUILERES);
            return;
        }
        LocalDate hoy = LocalDate.now();
        mostrar(Mensajes.ENCABEZADO_ALQUILERES);
        for (Alquiler a : lista) {
            mostrar(Mensajes.lineaAlquiler(a, hoy));
        }
    }

    // RESERVAS (TS2-02)

    private void reservarActividad() {
        String dni = pedirTexto(Mensajes.PEDIR_DNI_SOCIO);
        String actividad = pedirTexto(Mensajes.PEDIR_ACTIVIDAD);
        String horario = pedirTexto(Mensajes.PEDIR_HORARIO);
        try {
            logica.reservar(dni, actividad, horario);
            mostrar(Mensajes.reservaRegistrada(new Reserva(dni.trim(), actividad.trim(), horario.trim())));
        } catch (SocioNoEncontradoException e) {
            mostrar(Mensajes.errorSocioNoEncontrado(e.getDni()));
        } catch (DniInvalidoException e) {
            mostrar(Mensajes.ERROR_DNI_INVALIDO);
        } catch (ReservaDuplicadaException e) {
            mostrar(Mensajes.errorReservaDuplicada(e.getReserva()));
        }
    }

    private void verReservasDeActividad() {
        String actividad = pedirTexto(Mensajes.PEDIR_ACTIVIDAD);
        Set<Reserva> reservas = logica.reservasDeActividad(actividad);
        if (reservas.isEmpty()) {
            mostrar(Mensajes.sinReservas(actividad.trim()));
            return;
        }
        mostrar(Mensajes.ENCABEZADO_RESERVAS);
        for (Reserva r : reservas) {
            mostrar(Mensajes.lineaReserva(r));
        }
    }

    // Entrada por teclado

    private TipoEquipamiento pedirTipoEquipamiento() {
        while (true) {
            int opcion = pedirEntero(Mensajes.PEDIR_TIPO_EQUIPO);
            switch (opcion) {
                case 1:
                    return TipoEquipamiento.RAQUETA;
                case 2:
                    return TipoEquipamiento.LOCKER;
                case 3:
                    return TipoEquipamiento.PELOTA;
                case 4:
                    return TipoEquipamiento.OTRO;
                default:
                    mostrar(Mensajes.TIPO_EQUIPO_INVALIDO);
            }
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

    private int pedirEnteroPositivo(String pregunta) {
        while (true) {
            int valor = pedirEntero(pregunta);
            if (valor > 0) {
                return valor;
            }
            mostrar(Mensajes.ENTERO_POSITIVO);
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

    // Error en la fecha vacia
    private LocalDate pedirFechaOpcional(String pregunta) {
        while (true) {
            System.out.print(pregunta);
            String ingresado = teclado.nextLine().trim();
            if (ingresado.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(ingresado, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                mostrar(Mensajes.FECHA_INVALIDA);
            }
        }
    }
}
