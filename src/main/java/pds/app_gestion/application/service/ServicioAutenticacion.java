package pds.app_gestion.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pds.app_gestion.application.dto.SesionAutenticadaResponse;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoRequest;
import pds.app_gestion.application.dto.SolicitarCodigoAccesoResponse;
import pds.app_gestion.application.exception.AutenticacionException;
import pds.app_gestion.application.exception.ErrorValidacionException;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServicioAutenticacion {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final NotificadorCodigoAcceso notificadorCodigoAcceso;
    private final Clock clock;
    private final Duration validezCodigo;
    private final boolean incluirCodigoEnRespuesta;
    private final Map<String, CodigoAcceso> codigosPorValor;
    private final Map<String, String> codigoPorEmail;

    @Autowired
    public ServicioAutenticacion(
            NotificadorCodigoAcceso notificadorCodigoAcceso,
            @Value("${app.auth.codigo.minutos-validez:5}") long minutosValidez,
            @Value("${app.auth.include-code-in-response:false}") boolean incluirCodigoEnRespuesta) {
        this(
            notificadorCodigoAcceso,
            Clock.systemDefaultZone(),
            Duration.ofMinutes(Math.max(1, minutosValidez)),
            incluirCodigoEnRespuesta,
            new ConcurrentHashMap<>(),
            new ConcurrentHashMap<>()
        );
    }

    ServicioAutenticacion(
            NotificadorCodigoAcceso notificadorCodigoAcceso,
            Clock clock,
            Duration validezCodigo,
            boolean incluirCodigoEnRespuesta,
            Map<String, CodigoAcceso> codigosPorValor,
            Map<String, String> codigoPorEmail) {
        this.notificadorCodigoAcceso = notificadorCodigoAcceso;
        this.clock = clock;
        this.validezCodigo = validezCodigo;
        this.incluirCodigoEnRespuesta = incluirCodigoEnRespuesta;
        this.codigosPorValor = codigosPorValor;
        this.codigoPorEmail = codigoPorEmail;
    }

    public SolicitarCodigoAccesoResponse solicitarCodigo(SolicitarCodigoAccesoRequest request) {
        if (request == null) {
            throw new ErrorValidacionException("Debe indicarse un email para solicitar el código de acceso");
        }

        return solicitarCodigo(request.getEmail());
    }

    public SolicitarCodigoAccesoResponse solicitarCodigo(String email) {
        validarEmail(email);
        limpiarCodigosExpirados();

        String emailNormalizado = email.trim();
        invalidarCodigoAnterior(emailNormalizado);

        String codigoAcceso = generarCodigoAcceso();
        Instant expiracion = clock.instant().plus(validezCodigo);
        CodigoAcceso codigo = new CodigoAcceso(codigoAcceso, emailNormalizado, expiracion);

        codigosPorValor.put(codigoAcceso, codigo);
        codigoPorEmail.put(emailNormalizado, codigoAcceso);

        String modoEntrega = notificadorCodigoAcceso.enviarCodigoAcceso(
            emailNormalizado,
            codigoAcceso,
            validezCodigo.toMinutes()
        );

        return SolicitarCodigoAccesoResponse.builder()
            .email(emailNormalizado)
            .expiraEn(LocalDateTime.ofInstant(expiracion, clock.getZone()))
            .modoEntrega(modoEntrega)
            .mensaje("Código de acceso generado correctamente")
            .codigoDesarrollo(incluirCodigoEnRespuesta ? codigoAcceso : null)
            .build();
    }

    public String resolverEmailDesdeCodigo(String codigoAcceso) {
        CodigoAcceso codigo = obtenerCodigoValido(codigoAcceso);
        Instant nuevaExpiracion = clock.instant().plus(validezCodigo);
        CodigoAcceso refrescado = codigo.refrescar(nuevaExpiracion);
        codigosPorValor.put(refrescado.valor(), refrescado);
        codigoPorEmail.put(refrescado.email(), refrescado.valor());
        return refrescado.email();
    }

    public SesionAutenticadaResponse obtenerSesionActiva(String codigoAcceso) {
        String email = resolverEmailDesdeCodigo(codigoAcceso);
        CodigoAcceso codigo = codigosPorValor.get(codigoAcceso.trim());

        return SesionAutenticadaResponse.builder()
            .email(email)
            .codigoAcceso(codigo.valor())
            .expiraEn(LocalDateTime.ofInstant(codigo.expiraEn(), clock.getZone()))
            .autenticado(true)
            .build();
    }

    public void cerrarSesion(String codigoAcceso) {
        if (codigoAcceso == null || codigoAcceso.isBlank()) {
            return;
        }

        CodigoAcceso eliminado = codigosPorValor.remove(codigoAcceso.trim());
        if (eliminado != null) {
            codigoPorEmail.remove(eliminado.email(), eliminado.valor());
        }
    }

    private CodigoAcceso obtenerCodigoValido(String codigoAcceso) {
        if (codigoAcceso == null || codigoAcceso.trim().isEmpty()) {
            throw new AutenticacionException("Debe indicarse un código de acceso válido");
        }

        limpiarCodigosExpirados();
        CodigoAcceso codigo = codigosPorValor.get(codigoAcceso.trim());
        if (codigo == null) {
            throw new AutenticacionException("El código de acceso no existe o ha expirado");
        }

        if (codigo.haExpirado(clock.instant())) {
            eliminarCodigo(codigo);
            throw new AutenticacionException("El código de acceso ha expirado");
        }

        return codigo;
    }

    private void limpiarCodigosExpirados() {
        Instant ahora = clock.instant();
        codigosPorValor.values().removeIf(codigo -> {
            boolean expirado = codigo.haExpirado(ahora);
            if (expirado) {
                codigoPorEmail.remove(codigo.email(), codigo.valor());
            }
            return expirado;
        });
    }

    private void invalidarCodigoAnterior(String email) {
        String codigoAnterior = codigoPorEmail.remove(email);
        if (codigoAnterior != null) {
            codigosPorValor.remove(codigoAnterior);
        }
    }

    private void eliminarCodigo(CodigoAcceso codigo) {
        codigosPorValor.remove(codigo.valor());
        codigoPorEmail.remove(codigo.email(), codigo.valor());
    }

    private void validarEmail(String email) {
        if (email == null || !email.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ErrorValidacionException("Email inválido: " + email);
        }
    }

    private String generarCodigoAcceso() {
        String codigo;
        do {
            codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        } while (codigosPorValor.containsKey(codigo));
        return codigo;
    }

    private record CodigoAcceso(String valor, String email, Instant expiraEn) {
        private boolean haExpirado(Instant ahora) {
            return !expiraEn.isAfter(ahora);
        }

        private CodigoAcceso refrescar(Instant nuevaExpiracion) {
            return new CodigoAcceso(valor, email, nuevaExpiracion);
        }
    }
}