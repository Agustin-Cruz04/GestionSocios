package LogicaNegocio;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RegistroEquipamiento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Equipamiento> inventario;
    private final List<Alquiler> alquileres;

    public RegistroEquipamiento(Map<String, Equipamiento> inventario, List<Alquiler> alquileres) {
        this.inventario = inventario;
        this.alquileres = alquileres;
    }

    public Map<String, Equipamiento> getInventario() {
        return inventario;
    }

    public List<Alquiler> getAlquileres() {
        return alquileres;
    }
}
