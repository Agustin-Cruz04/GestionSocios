package Datos;

public enum MotivoFallo {
    LECTURA, // no se pudo abrir o leer el archivo
    ESCRITURA, // no se pudo crear o escribir el archivo
    FORMATO_INVALIDO // el archivo existe pero su contenido no es el correcto
}
