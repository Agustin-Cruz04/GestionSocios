package LogicaNegocio;

import java.util.regex.Pattern;

public final class Validaciones {

    private Validaciones() {

    }

    private static final Pattern DNI = Pattern.compile("\\d{7,8}");
    private static final Pattern NOMBRE = Pattern.compile("\\p{L}+([ '\\-]\\p{L}+)*");
    private static final Pattern EMAIL = Pattern
            .compile("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}");
    private static final Pattern TELEFONO = Pattern.compile("[0-9 +()\\-]+");

    public static boolean dniValido(String dni) {
        return dni != null && DNI.matcher(dni.trim()).matches();
    }

    public static boolean nombreValido(String texto) {
        if (texto == null) {
            return false;
        }
        String limpio = texto.trim();
        return limpio.length() >= 2 && limpio.length() <= 40 && NOMBRE.matcher(limpio).matches();
    }

    public static boolean emailValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean telefonoValido(String telefono) {
        if (telefono == null) {
            return false;
        }
        String limpio = telefono.trim();
        if (!TELEFONO.matcher(limpio).matches()) {
            return false;
        }
        int digitos = limpio.replaceAll("[^0-9]", "").length();
        return digitos >= 6 && digitos <= 15;
    }
}
