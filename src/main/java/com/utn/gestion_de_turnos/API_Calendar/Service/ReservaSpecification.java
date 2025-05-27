package com.utn.gestion_de_turnos.API_Calendar.Service;

import com.utn.gestion_de_turnos.model.Reserva;
import org.springframework.data.jpa.domain.Specification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Clase que contiene especificaciones dinámicas (filtros) para la entidad Reserva.
 * Se utiliza para construir consultas flexibles según parámetros opcionales del usuario.
 */
public class ReservaSpecification {

    /**
     * Filtra por ID de sala si está presente.
     */
    public static Specification<Reserva> hasSalaId(Optional<Long> salaId) {
        return (root, query, cb) ->
                salaId.map(id -> cb.equal(root.get("sala").get("id"), id))
                        .orElse(cb.conjunction()); // sin filtro si no hay valor
    }

    /**
     * Filtra por ID de cliente si está presente.
     */
    public static Specification<Reserva> hasClienteId(Optional<Long> clienteId) {
        return (root, query, cb) ->
                clienteId.map(id -> cb.equal(root.get("cliente").get("id"), id))
                        .orElse(cb.conjunction());
    }

    /**
     * Filtra por fecha exacta (ignorando hora) si está presente.
     * Utiliza la función SQL DATE para extraer solo la parte de fecha.
     */
    public static Specification<Reserva> hasFecha(Optional<LocalDate> fecha) {
        return (root, query, cb) ->
                fecha.map(f ->
                        cb.equal(
                                cb.function("DATE", LocalDate.class, root.get("fechaInicio")),
                                f
                        )
                ).orElse(cb.conjunction());
    }

    /**
     * Filtra las reservas que ocurren entre dos fechas, si están presentes.
     */
    public static Specification<Reserva> entreFechas(Optional<LocalDate> desde, Optional<LocalDate> hasta) {
        return (root, query, cb) -> {
            if (desde.isPresent() && hasta.isPresent()) {
                return cb.between(
                        cb.function("DATE", LocalDate.class, root.get("fechaInicio")),
                        desde.get(),
                        hasta.get()
                );
            } else if (desde.isPresent()) {
                return cb.greaterThanOrEqualTo(
                        cb.function("DATE", LocalDate.class, root.get("fechaInicio")),
                        desde.get()
                );
            } else if (hasta.isPresent()) {
                return cb.lessThanOrEqualTo(
                        cb.function("DATE", LocalDate.class, root.get("fechaInicio")),
                        hasta.get()
                );
            }
            return cb.conjunction();
        };
    }

    /**
     * Filtra las reservas por día de la semana (ej. lunes, martes...), si está presente.
     * Se adapta al sistema MySQL donde el domingo es 1.
     */
    public static Specification<Reserva> hasDiaSemana(Optional<DayOfWeek> dia) {
        return (root, query, cb) ->
                dia.map(d -> {
                    int mysqlDayValue = d.getValue() + 1; // Java: lunes=1 -> MySQL: lunes=2
                    if (mysqlDayValue == 8) mysqlDayValue = 1; // Ajuste para domingo
                    return cb.equal(
                            cb.function("DAYOFWEEK", Integer.class, root.get("fechaInicio")),
                            mysqlDayValue
                    );
                }).orElse(cb.conjunction());
    }

    /**
     * Filtra por hora de inicio, considerando el campo fechaInicio.
     * Extrae solo la hora con TIME() para comparar correctamente.
     */
    public static Specification<Reserva> entreHoras(Optional<LocalTime> desde, Optional<LocalTime> hasta) {
        return (root, query, cb) -> {
            if (desde.isPresent() && hasta.isPresent()) {
                return cb.between(
                        cb.function("TIME", LocalTime.class, root.get("fechaInicio")),
                        desde.get(),
                        hasta.get()
                );
            } else if (desde.isPresent()) {
                return cb.greaterThanOrEqualTo(
                        cb.function("TIME", LocalTime.class, root.get("fechaInicio")),
                        desde.get()
                );
            } else if (hasta.isPresent()) {
                return cb.lessThanOrEqualTo(
                        cb.function("TIME", LocalTime.class, root.get("fechaInicio")),
                        hasta.get()
                );
            }
            return cb.conjunction();
        };
    }
}
