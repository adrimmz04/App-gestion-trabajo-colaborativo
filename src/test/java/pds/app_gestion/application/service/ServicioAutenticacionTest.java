package pds.app_gestion.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pds.app_gestion.application.dto.SesionAutenticadaResponse;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoResponse;
import pds.app_gestion.application.exception.AutenticacionException;
import pds.app_gestion.application.exception.ErrorValidacionException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ServicioAutenticacionTest {

    private ClockMutable clock;
    private ServicioAutenticacion servicioAutenticacion;

    @BeforeEach
    void setUp() {
        clock = new ClockMutable(Instant.parse("2026-04-24T10:00:00Z"), ZoneId.of("UTC"));
        servicioAutenticacion = new ServicioAutenticacion(
            (emailDestino, codigoAcceso, minutosValidez) -> "desarrollo",
            clock,
            Duration.ofMinutes(5),
            true,
            new ConcurrentHashMap<>(),
            new ConcurrentHashMap<>()
        );
    }

    @Test
    void solicitarCodigoGeneraCodigoYPermiteResolverUsuario() {
        SolicitarCodigoAccesoResponse response = servicioAutenticacion.solicitarCodigo("usuario@test.com");

        assertEquals("usuario@test.com", response.getEmail());
        assertEquals("desarrollo", response.getModoEntrega());
        assertNotNull(response.getCodigoDesarrollo());
        assertEquals("usuario@test.com", servicioAutenticacion.resolverEmailDesdeCodigo(response.getCodigoDesarrollo()));
    }

    @Test
    void solicitarCodigoConEmailInvalidoLanzaError() {
        assertThrows(ErrorValidacionException.class, () -> servicioAutenticacion.solicitarCodigo("correo-invalido"));
    }

    @Test
    void resolverCodigoInvalidoLanzaErrorAutenticacion() {
        assertThrows(AutenticacionException.class, () -> servicioAutenticacion.resolverEmailDesdeCodigo("999999"));
    }

    @Test
    void solicitarNuevoCodigoInvalidaElAnterior() {
        SolicitarCodigoAccesoResponse primero = servicioAutenticacion.solicitarCodigo("usuario@test.com");
        SolicitarCodigoAccesoResponse segundo = servicioAutenticacion.solicitarCodigo("usuario@test.com");

        assertNotEquals(primero.getCodigoDesarrollo(), segundo.getCodigoDesarrollo());
        assertThrows(AutenticacionException.class, () -> servicioAutenticacion.resolverEmailDesdeCodigo(primero.getCodigoDesarrollo()));
        assertEquals("usuario@test.com", servicioAutenticacion.resolverEmailDesdeCodigo(segundo.getCodigoDesarrollo()));
    }

    @Test
    void cadaUsoRenuevaLaValidezDelCodigo() {
        SolicitarCodigoAccesoResponse response = servicioAutenticacion.solicitarCodigo("usuario@test.com");

        clock.avanzar(Duration.ofMinutes(4));
        SesionAutenticadaResponse sesionRefrescada = servicioAutenticacion.obtenerSesionActiva(response.getCodigoDesarrollo());
        assertEquals("usuario@test.com", sesionRefrescada.getEmail());

        clock.avanzar(Duration.ofMinutes(4));
        assertEquals("usuario@test.com", servicioAutenticacion.resolverEmailDesdeCodigo(response.getCodigoDesarrollo()));
    }

    @Test
    void codigoExpiraSiNoSeUsa() {
        SolicitarCodigoAccesoResponse response = servicioAutenticacion.solicitarCodigo("usuario@test.com");

        clock.avanzar(Duration.ofMinutes(6));

        assertThrows(AutenticacionException.class, () -> servicioAutenticacion.resolverEmailDesdeCodigo(response.getCodigoDesarrollo()));
    }

    private static final class ClockMutable extends Clock {

        private Instant instant;
        private final ZoneId zone;

        private ClockMutable(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new ClockMutable(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void avanzar(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}