package com.ifto.clinica.model.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
public class HorarioAgenda {
    @Id
    @GeneratedValue
    private Long id;

    private LocalTime inicio;
    private LocalTime fim;

    @Enumerated(EnumType.STRING)
    private StatusHorario status;

    @ManyToOne
    private Agenda agenda;

    @OneToOne
    private Consulta consulta;
}
