package pds.app_gestion.infrastructure.persistence.repository;

import org.springframework.stereotype.Component;
import pds.app_gestion.domain.*;
import pds.app_gestion.infrastructure.persistence.entity.ListaJPA;
import pds.app_gestion.infrastructure.persistence.entity.RegistroAccionJPA;
import pds.app_gestion.infrastructure.persistence.entity.TableroJPA;
import pds.app_gestion.infrastructure.persistence.entity.TarjetaJPA;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
     * Restaura el estado del tablero incluyendo estado de bloqueo.
     */
    public Tablero convertirADominio(TableroJPA jpa) {
        if (jpa == null) {
            return null;
        }

        Tablero tablero = new Tablero(jpa.getId(), jpa.getTitulo(), jpa.getPropietarioEmail());

        try {
            setFieldValue(tablero, "descripcion", jpa.getDescripcion() != null ? jpa.getDescripcion() : "");
            setFieldValue(tablero, "bloqueado", jpa.isBloqueado());
            setFieldValue(tablero, "fechaDesbloqueo", 
                jpa.getFechaBloqueo() != null ? Optional.of(jpa.getFechaBloqueo()) : Optional.empty());
            setFieldValue(tablero, "fechaCreacion", jpa.getFechaCreacion());
            setFieldValue(tablero, "fechaActualizacion", jpa.getFechaActualizacion());

            tablero.getUsuariosCompartidos().clear();
            if (jpa.getUsuariosCompartidos() != null) {
                tablero.getUsuariosCompartidos().addAll(jpa.getUsuariosCompartidos());
            }

            tablero.getListas().clear();
            if (jpa.getListas() != null) {
                tablero.getListas().addAll(jpa.getListas().stream()
                    .sorted(Comparator.comparing(ListaJPA::getId))
                    .map(this::convertirListaADominio)
                    .toList());
            }

            tablero.getHistorial().clear();
            if (jpa.getHistorialAcciones() != null) {
                tablero.getHistorial().addAll(jpa.getHistorialAcciones().stream()
                    .sorted(Comparator.comparing(RegistroAccionJPA::getFecha))
                    .map(this::convertirRegistroADominio)
                    .toList());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir TableroJPA a Tablero", e);
        }

        return tablero;
    }
    
    private void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * Convierte un Tablero de dominio a un TableroJPA para persistencia.
     */
    public TableroJPA convertirAJPA(Tablero tablero) {
        if (tablero == null) {
            return null;
        }

        TableroJPA jpa = TableroJPA.builder()
            .id(tablero.getId())
            .titulo(tablero.getTitulo())
            .descripcion(tablero.getDescripcion() != null ? tablero.getDescripcion() : "")
            .propietarioEmail(tablero.getPropietarioEmail())
            .bloqueado(tablero.isBloqueado())
            .fechaBloqueo(tablero.getFechaDesbloqueo().orElse(null))
            .fechaCreacion(tablero.getFechaCreacion())
            .fechaActualizacion(tablero.getFechaActualizacion())
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
            .listaPrerrequisitoId(lista.obtenerListasPrevias().isEmpty() ? null : lista.obtenerListasPrevias().get(0))
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
            .fechaCreacion(tarjeta.getFechaCreacion())
            .fechaActualizacion(tarjeta.getFechaActualizacion())
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

    private Lista convertirListaADominio(ListaJPA jpa) {
        Lista lista = new Lista(jpa.getId(), jpa.getNombre());

        if (jpa.getLimiteMaximo() != null) {
            lista.establecerLimiteMaximo(jpa.getLimiteMaximo());
        }

        if (jpa.getListaPrerrequisitoId() != null && !jpa.getListaPrerrequisitoId().isBlank()) {
            lista.agregarListaPrevia(jpa.getListaPrerrequisitoId());
        }

        if (jpa.getTarjetas() != null) {
            List<Tarjeta> tarjetas = jpa.getTarjetas().stream()
                .sorted(Comparator.comparing(TarjetaJPA::getId))
                .map(this::convertirTarjetaADominio)
                .toList();
            lista.getTarjetas().addAll(tarjetas);
        }

        return lista;
    }

    private Tarjeta convertirTarjetaADominio(TarjetaJPA jpa) {
        Tarjeta tarjeta = new Tarjeta(
            jpa.getId(),
            jpa.getTitulo(),
            jpa.getDescripcion(),
            Tarjeta.TipoTarjeta.valueOf(jpa.getTipo().name())
        );

        try {
            setFieldValue(tarjeta, "completada", jpa.isCompletada());
            setFieldValue(tarjeta, "fechaCompletacion", jpa.getFechaCompletacion());
            setFieldValue(tarjeta, "fechaCreacion", jpa.getFechaCreacion());
            setFieldValue(tarjeta, "fechaActualizacion", jpa.getFechaActualizacion());

            if (jpa.getEtiquetasNombres() != null) {
                for (String nombreEtiqueta : jpa.getEtiquetasNombres()) {
                    String color = jpa.getEtiquetasColores() != null
                        ? jpa.getEtiquetasColores().getOrDefault(nombreEtiqueta, "#808080")
                        : "#808080";
                    tarjeta.getEtiquetas().add(new Etiqueta(nombreEtiqueta, color));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir TarjetaJPA a Tarjeta", e);
        }

        return tarjeta;
    }

    private RegistroAccion convertirRegistroADominio(RegistroAccionJPA jpa) {
        return new RegistroAccion(jpa.getTipo(), jpa.getDetalles(), jpa.getFecha());
    }
}
