package pds.app_gestion.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Value Object que representa la posición de una tarjeta en una lista.
 * 
 * Almacena el índice de la posición para determinar el orden de las tarjetas.
 */
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Posicion {
    private final int indice;

    /**
     * Crea una nueva posición con el índice especificado.
     * 
     * @param indice posición de la tarjeta (debe ser >= 0)
     * @throws IllegalArgumentException si el índice es negativo
     */
    public static Posicion crear(int indice) {
        if (indice < 0) {
            throw new IllegalArgumentException("El índice de posición no puede ser negativo");
        }
        return new Posicion(indice);
    }

    /**
     * Obtiene la siguiente posición después de la actual.
     */
    public Posicion siguiente() {
        return new Posicion(this.indice + 1);
    }
}
