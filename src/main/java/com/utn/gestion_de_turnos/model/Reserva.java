package com.utn.gestion_de_turnos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "turnos")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_final", nullable = false)
    private LocalDateTime fechaFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false)
    private TipoPago tipoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(name = "google_event_id")
    private String googleEventId;

    public enum Estado {
        ACTIVO,
        CANCELADO,
        FINALIZADO
    }

    public enum TipoPago {
        EFECTIVO,
        TRANSFERENCIA,
        TARJETA
    }
}