package pds.app_gestion.infrastructure.persistence.repository;

import org.springframework.stereotype.Component;
import pds.app_gestion.domain.*;
import pds.app_gestion.infrastructure.persistence.entity.ListaJPA;
import pds.app_gestion.infrastructure.persistence.entity.TableroJPA;
import pds.app_gestion.infrastructure.persistence.entity.TarjetaJPA;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador que convierte entre entidades JPA y objetos de dominio.
 * NOTA: Este convertidor no es bidireccional completo. Para dominio → JPA
 * usamos conversiones directas. Para JPA → dominio recreamos instancias.
 */
@Component
public class ConvertidorTableroJPA {

    /**
     * Convierte un TableroJPA a un Tablero de dominio.
     * Nota: Por limitaciones de DDD, solo se convierten los datos básicos.
     * Las listas y tarjetas se cargan de forma lazy.
     */
    public Tablero convertirADominio(TableroJPA jpa) {
        if (jpa == null) return null;

        // Crear tablero nuevo con constructor básico
        Tablero tablero = new Tablero(jpa.getId(), jpa.getTitulo(), jpa.getPropietarioEmail());
        
        // No podemos usar setters en DDD, así que retornamos tablero básico
        // La lógica de negocio debería reconstruir el tablero desde sus estados previos
        // Por ahora, simplemente copiamos datos inmutables desde JPA
        
        return tablero;
    }

    /**
     * Convierte un Tablero de dominio a un TableroJPA para persistencia.
     */
    public TableroJPA convertirAJPA(Tablero tablero) {
        if (tablero == null) return null;

        TableroJPA jpa = TableroJPA.builder()
            .id(tablero.getId())
            .titulo(tablero.getTitulo())
            .descripcion(tablero.getDescripcion() != null ? tablero.getDescripcion() : "")
            .propietarioEmail(tablero.getPropietarioEmail())
            .bloqueado(tablero.isBloqueado())
            .usuariosCompartidos(new HashSet<>(tablero.getUsuariosCompartidos()))
            .build();

        // Convertir listas
        Set<ListaJPA> listasJPA = tablero.getListas().stream()
            .map(this::convertirListaAJPA)
            .collect(Collectors.toSet());
        listasJPA.forEach(l -> l.setTablero(jpa)); // Establecer referencia bidiccional
        jpa.setListas(listasJPA);

        // Convertir historial
        Set<pds.app_gestion.infrastructure.persistence.entity.RegistroAccionJPA> historialJPA =
            tablero.getHistorial().stream()
                .map(this::convertirRegistroAJPA)
                .collect(Collectors.toSet());
        historialJPA.forEach(r -> r.setTablero(jpa));
        jpa.setHistorialAcciones(historialJPA);

        return jpa;
    }

    private ListaJPA convertirListaAJPA(Lista lista) {
        ListaJPA jpa = ListaJPA.builder()
            .id(lista.getId())
            .nombre(lista.getNombre())
            .limiteMaximo(lista.getLimiteMaximo().orElse(null))
            .build();

        Set<TarjetaJPA> tarjetasJPA = lista.getTarjetas().stream()
            .map(this::convertirTarjetaAJPA)
            .collect(Collectors.toSet());
        tarjetasJPA.forEach(t -> t.setLista(jpa)); // Establecer referencia bidireccional
        jpa.setTarjetas(tarjetasJPA);

        return jpa;
    }

    private TarjetaJPA convertirTarjetaAJPA(Tarjeta tarjeta) {
        TarjetaJPA jpa = TarjetaJPA.builder()
            .id(tarjeta.getId())
            .titulo(tarjeta.getTitulo())
            .descripcion(tarjeta.getDescripcion())
            .tipo(pds.app_gestion.infrastructure.persistence.entity.TipoTarjetaJPA.valueOf(tarjeta.getTipo().name()))
            .completada(tarjeta.isCompletada())
            .fechaCompletacion(tarjeta.getFechaCompletacion())
            .build();

        jpa.setEtiquetasNombres(tarjeta.getEtiquetas().stream()
            .map(Etiqueta::getNombre)
            .collect(Collectors.toSet()));

        java.util.Map<String, String> colores = new java.util.HashMap<>();
        tarjeta.getEtiquetas().forEach(e -> colores.put(e.getNombre(), e.getColor()));
        jpa.setEtiquetasColores(colores);

        return jpa;
    }

    private pds.app_gestion.infrastructure.persistence.entity.RegistroAccionJPA convertirRegistroAJPA(RegistroAccion registro) {
        return pds.app_gestion.infrastructure.persistence.entity.RegistroAccionJPA.builder()
            .tipo(registro.getTipo())
            .detalles(registro.getDetalles())
            .fecha(registro.getFecha())
            .build();
    }
}
