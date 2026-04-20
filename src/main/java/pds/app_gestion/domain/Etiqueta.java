package pds.app_gestion.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Value Object que representa una etiqueta para clasificar tarjetas.
 * 
 * Una etiqueta contiene un nombre y un color asociado.
 * Es un Value Object inmutable para garantizar que no cambie durante su ciclo de vida.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Etiqueta {
    private final String nombre;
    private final String color;

    /**
     * Crea una nueva etiqueta con el nombre y color especificados.
     * 
     * @param nombre nombre de la etiqueta (no puede ser nulo o vacío)
     * @param color código hexadecimal del color (ej: "#FF5733")
     * @throws IllegalArgumentException si el nombre está vacío o color inválido
     */
    public Etiqueta(String nombre, String color) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("El color debe ser un código hexadecimal válido (ej: #FF5733)");
        }
        this.nombre = nombre;
        this.color = color;
    }
}
