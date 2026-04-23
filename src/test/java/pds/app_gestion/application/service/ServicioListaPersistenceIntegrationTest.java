package pds.app_gestion.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import pds.app_gestion.application.dto.ConfigurarReglasListaRequest;
import pds.app_gestion.application.dto.CrearListaRequest;
import pds.app_gestion.application.dto.CrearTableroRequest;
import pds.app_gestion.application.dto.CrearTarjetaRequest;
import pds.app_gestion.domain.Lista;
import pds.app_gestion.domain.RepositorioTablero;
import pds.app_gestion.domain.Tablero;
import pds.app_gestion.domain.Tarjeta;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Integration Tests - Persistencia de reglas de listas")
class ServicioListaPersistenceIntegrationTest {

    @Autowired
    private ServicioTablero servicioTablero;

    @Autowired
    private ServicioLista servicioLista;

    @Autowired
    private ServicioTarjeta servicioTarjeta;

    @Autowired
    private RepositorioTablero repositorioTablero;

    @Test
    @DisplayName("Debe persistir múltiples listas previas en una lista")
    void debePersistirMultiplesListasPrevias() {
        String emailUsuario = "propietario@test.com";
        var tablero = servicioTablero.crearTablero(CrearTableroRequest.builder()
            .titulo("Tablero reglas")
            .descripcion("Prueba de persistencia")
            .propietarioEmail(emailUsuario)
            .build());

        var listaPreviaUno = servicioTablero.agregarLista(tablero.getId(), emailUsuario, new CrearListaRequest("Analisis", null));
        var listaPreviaDos = servicioTablero.agregarLista(tablero.getId(), emailUsuario, new CrearListaRequest("Desarrollo", null));
        var listaDestino = servicioTablero.agregarLista(tablero.getId(), emailUsuario, new CrearListaRequest("Validacion", null));

        servicioLista.configurarReglas(
            tablero.getId(),
            listaDestino.getId(),
            emailUsuario,
            ConfigurarReglasListaRequest.builder()
                .listasPrevias(List.of(listaPreviaUno.getId(), listaPreviaDos.getId()))
                .build()
        );

        var tableroPersistido = repositorioTablero.obtenerPorId(tablero.getId());

        assertThat(tableroPersistido).isPresent();
        var listaRecuperada = tableroPersistido.get().obtenerLista(listaDestino.getId());
        assertThat(listaRecuperada).isPresent();
        assertThat(listaRecuperada.get().obtenerListasPrevias())
            .containsExactlyInAnyOrder(listaPreviaUno.getId(), listaPreviaDos.getId());
    }

    @Test
    @DisplayName("Debe persistir el estado archivado de una tarjeta")
    void debePersistirEstadoArchivadoDeTarjeta() {
        Tablero tablero = new Tablero("tablero-archivado", "Tablero archivado", "propietario@test.com");
        Lista lista = new Lista("lista-archivado", "Hecho");
        Tarjeta tarjeta = new Tarjeta("tarjeta-archivada", "Cerrar incidencia", "Descripcion");
        tarjeta.marcarComoCompletada();
        tarjeta.archivar();
        lista.agregarTarjeta(tarjeta);
        tablero.agregarLista(lista);

        repositorioTablero.guardar(tablero);

        var tableroPersistido = repositorioTablero.obtenerPorId(tablero.getId());

        assertThat(tableroPersistido).isPresent();
        var tarjetaRecuperada = tableroPersistido.get()
            .obtenerLista(lista.getId())
            .flatMap(valor -> valor.obtenerTarjeta(tarjeta.getId()));
        assertThat(tarjetaRecuperada).isPresent();
        assertThat(tarjetaRecuperada.get().estaArchivada()).isTrue();
        assertThat(tarjetaRecuperada.get().getFechaArchivado()).isNotNull();
    }

    @Test
    @DisplayName("Debe persistir las listas por las que ha pasado una tarjeta")
    void debePersistirListasVisitadasDeUnaTarjeta() {
        String emailUsuario = "propietario@test.com";
        var tablero = servicioTablero.crearTablero(CrearTableroRequest.builder()
            .titulo("Tablero flujo")
            .descripcion("Prueba de listas visitadas")
            .propietarioEmail(emailUsuario)
            .build());

        var listaAnalisis = servicioTablero.agregarLista(tablero.getId(), emailUsuario, new CrearListaRequest("Analisis", null));
        var listaDesarrollo = servicioTablero.agregarLista(tablero.getId(), emailUsuario, new CrearListaRequest("Desarrollo", null));

        var tarjeta = servicioTarjeta.crearTarjeta(
            tablero.getId(),
            listaAnalisis.getId(),
            emailUsuario,
            CrearTarjetaRequest.builder()
                .titulo("Implementar login")
                .descripcion("Flujo completo")
                .tipo("TAREA")
                .build()
        );

        servicioLista.moverTarjeta(
            tablero.getId(),
            listaAnalisis.getId(),
            listaDesarrollo.getId(),
            tarjeta.getId(),
            emailUsuario
        );

        var tableroPersistido = repositorioTablero.obtenerPorId(tablero.getId());

        assertThat(tableroPersistido).isPresent();
        var tarjetaRecuperada = tableroPersistido.get()
            .obtenerLista(listaDesarrollo.getId())
            .flatMap(valor -> valor.obtenerTarjeta(tarjeta.getId()));

        assertThat(tarjetaRecuperada).isPresent();
        assertThat(tarjetaRecuperada.get().getListasVisitadas())
            .contains(listaAnalisis.getId(), listaDesarrollo.getId());
    }
}