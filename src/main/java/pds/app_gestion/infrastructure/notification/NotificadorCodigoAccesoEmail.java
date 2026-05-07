package pds.app_gestion.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import pds.app_gestion.application.exception.AutenticacionException;
import pds.app_gestion.application.service.NotificadorCodigoAcceso;

import java.nio.charset.StandardCharsets;

@Component
public class NotificadorCodigoAccesoEmail implements NotificadorCodigoAcceso {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificadorCodigoAccesoEmail.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean envioCorreoHabilitado;
    private final String remitente;
    private final String nombreRemitente;

    public NotificadorCodigoAccesoEmail(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.auth.mail.enabled:false}") boolean envioCorreoHabilitado,
            @Value("${app.auth.mail.from:no-reply@app-gestion.local}") String remitente,
            @Value("${app.auth.mail.sender-name:APP-GESTION}") String nombreRemitente) {
        this.mailSenderProvider = mailSenderProvider;
        this.envioCorreoHabilitado = envioCorreoHabilitado;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
    }

    @Override
    public String enviarCodigoAcceso(String emailDestino, String codigoAcceso, long minutosValidez) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (envioCorreoHabilitado && mailSender != null) {
            try {
                var mensaje = mailSender.createMimeMessage();
                var helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());

                if (nombreRemitente == null || nombreRemitente.isBlank()) {
                    helper.setFrom(remitente);
                } else {
                    helper.setFrom(remitente, nombreRemitente.trim());
                }

                helper.setTo(emailDestino);
                helper.setSubject("Código de acceso - App Gestión de Trabajo Colaborativo");
                helper.setText(
                    "Tu código de acceso es: " + codigoAcceso + System.lineSeparator()
                        + "Validez: " + minutosValidez + " minutos." + System.lineSeparator()
                        + "Cada uso renovará automáticamente su validez durante el mismo periodo."
                );
                mailSender.send(mensaje);
                LOGGER.info("Código de acceso enviado por correo real a {} desde {}", emailDestino, remitente);
                return "email";
            } catch (Exception e) {
                throw new AutenticacionException(
                    "No se pudo enviar el código de acceso por correo: " + e.getMessage(),
                    e
                );
            }
        }

        LOGGER.warn(
            "Modo desarrollo activo: no se enviará correo real. Código generado para {}: {} (válido durante {} minutos)",
            emailDestino,
            codigoAcceso,
            minutosValidez
        );
        return "desarrollo";
    }
}