package pds.app_gestion.ui.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pds.app_gestion.application.exception.*;

/**
 * Controlador de excepciones global para la API REST.
 * 
 * Traduce las excepciones de aplicación a respuestas HTTP apropiadas.
 */
@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    /**
     * Maneja excepciones de recurso no encontrado.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja excepciones de validación.
     */
    @ExceptionHandler(ErrorValidacionException.class)
    public ResponseEntity<ErrorResponse> manejarErrorValidacion(ErrorValidacionException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de operación de dominio.
     */
    @ExceptionHandler(ErrorOperacionDominioException.class)
    public ResponseEntity<ErrorResponse> manejarErrorOperacionDominio(ErrorOperacionDominioException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones de permiso negado.
     */
    @ExceptionHandler(PermisoNegadoException.class)
    public ResponseEntity<ErrorResponse> manejarPermisoNegado(PermisoNegadoException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja excepciones generales de aplicación.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> manejarAppException(AppException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Maneja excepciones generales no capturadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarExcepcionGeneral(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno del servidor: " + ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * DTO para respuestas de error.
     */
    public static class ErrorResponse {
        public int codigo;
        public String mensaje;
        public long timestamp;

        public ErrorResponse(int codigo, String mensaje, long timestamp) {
            this.codigo = codigo;
            this.mensaje = mensaje;
            this.timestamp = timestamp;
        }

        public int getCodigo() {
            return codigo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
