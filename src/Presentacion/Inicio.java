package Presentacion;

import Datos.AccesoDatos;
import Datos.PersistenciaException;
import LogicaNegocio.Logica;

public class Inicio {

    public static void main(String[] args) {
        try {
            AccesoDatos persistencia = new AccesoDatos();
            Logica logica = new Logica(persistencia);
            Menu menu = new Menu(logica);
            menu.ejecutar();
        } catch (PersistenciaException e) {
            System.out.println(Mensajes.errorPersistencia(e));
        }
    }
}
