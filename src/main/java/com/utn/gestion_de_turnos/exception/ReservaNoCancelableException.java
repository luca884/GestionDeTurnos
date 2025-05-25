package com.utn.gestion_de_turnos.exception;

public class ReservaNoCancelableException extends RuntimeException {
    public ReservaNoCancelableException(String message) {
        super(message);
    }
}
