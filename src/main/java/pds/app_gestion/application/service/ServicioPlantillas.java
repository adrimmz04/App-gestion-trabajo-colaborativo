package pds.app_gestion.application.service;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.DumperOptions;
import pds.app_gestion.application.dto.*;
import pds.app_gestion.domain.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioPlantillas {
    private final Yaml yaml;
    public ServicioPlantillas() { 
        LoaderOptions loaderOptions = new LoaderOptions();
        DumperOptions dumperOptions = new DumperOptions();
        Representer representer = new Representer(dumperOptions);
        this.yaml = new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions);
    }
    public String exportarTableroComoYAML(Tablero tablero) { PlantillaTableroYAML p = convertirTableroAPlantilla(tablero); StringWriter w = new StringWriter(); yaml.dump(p, w); return w.toString(); }
    public PlantillaTableroYAML importarPlantillaYAML(String y) { return yaml.loadAs(y, PlantillaTableroYAML.class); }
    private PlantillaTableroYAML convertirTableroAPlantilla(Tablero t) { return PlantillaTableroYAML.builder().titulo(t.getTitulo()).descripcion(t.getDescripcion()).listas(t.obtenerListas().stream().map(this::convertirListaAPlantilla).collect(Collectors.toList())).version("1.0").build(); }
    private PlantillaListaYAML convertirListaAPlantilla(Lista l) { return PlantillaListaYAML.builder().nombre(l.getNombre()).limiteMaximo(l.getLimiteMaximo().orElse(null)).listasPrevias(l.obtenerListasPrevias()).tarjetas(l.getTarjetas().stream().map(this::convertirTarjetaAPlantilla).collect(Collectors.toList())).build(); }
    private PlantillaTarjetaYAML convertirTarjetaAPlantilla(Tarjeta t) { return PlantillaTarjetaYAML.builder().titulo(t.getTitulo()).descripcion(t.getDescripcion()).tipo(t.getTipo().name()).etiquetas(t.getEtiquetas().stream().map(e -> PlantillaEtiquetaYAML.builder().nombre(e.getNombre()).color(e.getColor()).build()).collect(Collectors.toList())).completada(t.isCompletada()).build(); }
    public Tablero crearTableroDesdePlantilla(String id, String titulo, String email, PlantillaTableroYAML p) { Tablero t = new Tablero(id, titulo, email); Map<String,Lista> m = new HashMap<>(); if (p.getListas() != null) { for (PlantillaListaYAML pl : p.getListas()) { String lid = UUID.randomUUID().toString(); Lista l = new Lista(lid, pl.getNombre()); if (pl.getLimiteMaximo() != null && pl.getLimiteMaximo() > 0) l.establecerLimiteMaximo(pl.getLimiteMaximo()); if (pl.getTarjetas() != null) { for (PlantillaTarjetaYAML pt : pl.getTarjetas()) { String tid = UUID.randomUUID().toString(); Tarjeta.TipoTarjeta tt = "CHECKLIST".equals(pt.getTipo()) ? Tarjeta.TipoTarjeta.CHECKLIST : Tarjeta.TipoTarjeta.TAREA; Tarjeta ta = new Tarjeta(tid, pt.getTitulo(), pt.getDescripcion() != null ? pt.getDescripcion() : "", tt); if (pt.getEtiquetas() != null) for (PlantillaEtiquetaYAML e : pt.getEtiquetas()) ta.agregarEtiqueta(new Etiqueta(e.getNombre(), e.getColor())); if (pt.getCompletada() != null && pt.getCompletada()) ta.marcarComoCompletada(); l.agregarTarjeta(ta); } } t.agregarLista(l); m.put(pl.getNombre(), l); } for (PlantillaListaYAML pl : p.getListas()) { Lista l = m.get(pl.getNombre()); if (l != null && pl.getListasPrevias() != null) for (String pn : pl.getListasPrevias()) { Lista lp = m.get(pn); if (lp != null) l.agregarListaPrevia(lp.getId()); } } } return t; }
    public List<PlantillaTableroYAML> obtenerPlantillasEjemplo() { 
        List<PlantillaTableroYAML> plantillas = new ArrayList<>();
        plantillas.add(PlantillaTableroYAML.builder()
            .titulo("Kanban Básico")
            .descripcion("Plantilla Kanban simple")
            .listas(Arrays.asList(
                PlantillaListaYAML.builder().nombre("Por Hacer").limiteMaximo(null).listasPrevias(new ArrayList<>()).tarjetas(new ArrayList<>()).build(),
                PlantillaListaYAML.builder().nombre("En Progreso").limiteMaximo(5).listasPrevias(new ArrayList<>()).tarjetas(new ArrayList<>()).build(),
                PlantillaListaYAML.builder().nombre("Hecho").limiteMaximo(null).listasPrevias(new ArrayList<>()).tarjetas(new ArrayList<>()).build()
            ))
            .version("1.0")
            .build());
        return plantillas;
    }
}