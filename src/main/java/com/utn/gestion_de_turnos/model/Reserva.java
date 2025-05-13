package com.utn.gestion_de_turnos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Entity
@Table (name = "reserva" )
public class Reserva {

    private int id;
    private int cliente_id;
    private int sala_id;
    @Getter
    @Setter
    private Date fecha_inicio;
    @Getter
    @Setter
    private Date fecha_final;
    @Getter
    @Setter
    private String tipoPago; // <-- lo hacemos un ENUM (efectivo, transferencia, tarjeta) o un String solamente?
    @Getter
    @Setter
    private Estado estado;

    public enum Estado {
        RESERVADO,CANCELADO,DISPONIBLE
    }

    //Contructor para crear una Reserva
    public Reserva(Date fecha_inicio, Date fecha_final, String tipoPago, Estado estado, int cliente_id, int sala_id){
        this.fecha_inicio = fecha_inicio;
        this.fecha_final = fecha_final;
        this.tipoPago = tipoPago;
        this.estado = estado;
        this.cliente_id = cliente_id;
        this.sala_id = sala_id;
    }

    //Constructor para recibir la Reserva de Base de datos
    public Reserva(Date fecha_inicio, Date fecha_final, String tipoPago, Estado estado,int id, int cliente_id, int sala_id){
        this.fecha_inicio = fecha_inicio;
        this.fecha_final = fecha_final;
        this.tipoPago = tipoPago;
        this.estado = estado;
        this.id = id;
        this.cliente_id = cliente_id;
        this.sala_id = sala_id;
    }

}
