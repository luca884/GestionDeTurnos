package com.utn.gestion_de_turnos.API_Calendar.Service;

import com.utn.gestion_de_turnos.model.Reserva;
import org.springframework.data.jpa.domain.Specification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class ReservaSpecification {

    public static Specification<Reserva> hasSalaId(Optional<Long> salaId) {
        return (root, query, cb) ->
                salaId.map(id -> cb.equal(root.get("sala").get("id"), id))
                        .orElse(cb.conjunction());
    }

    public static Specification<Reserva> hasClienteId(Optional<Long> clienteId) {
        return (root, query, cb) ->
                clienteId.map(id -> cb.equal(root.get("cliente").get("id"), id))
                        .orElse(cb.conjunction());
    }

    public static Specification<Reserva> hasFecha(Optional<LocalDate> fecha) {
        return (root, query, cb) ->
                fecha.map(f ->
                        cb.equal(
                                cb.function("DATE", LocalDate.class, root.get("fechaInicio")),
                                f
                        )
                ).orElse(cb.conjunction());
    }

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

    public static Specification<Reserva> hasDiaSemana(Optional<DayOfWeek> dia) {
        return (root, query, cb) ->
                dia.map(d -> {
                    int mysqlDayValue = d.getValue() + 1; // ajuste para MySQL (domingo=1)
                    if (mysqlDayValue == 8) mysqlDayValue = 1;
                    return cb.equal(
                            cb.function("DAYOFWEEK", Integer.class, root.get("fechaInicio")),
                            mysqlDayValue
                    );
                }).orElse(cb.conjunction());
    }

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
