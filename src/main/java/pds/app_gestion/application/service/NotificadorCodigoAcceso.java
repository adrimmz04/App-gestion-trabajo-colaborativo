package pds.app_gestion.application.service;

public interface NotificadorCodigoAcceso {
    String enviarCodigoAcceso(String emailDestino, String codigoAcceso, long minutosValidez);
}