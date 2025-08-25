
package com.ifto.clinica.model.repository;

import com.ifto.clinica.model.entity.Agenda;
import com.ifto.clinica.model.entity.Agenda.StatusAgenda;
import com.ifto.clinica.model.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

    public interface AgendaRepository extends JpaRepository<Agenda, Long> {

        @Query("SELECT a FROM Agenda a WHERE a.status = :status")
        List<Agenda> findByStatus(StatusAgenda status);

        @Query("SELECT a FROM Agenda a WHERE a.medico.id = :medicoId AND a.data = :data AND a.status = 'DISPONIVEL'")
        List<Agenda> findDisponiveisByMedicoAndData(Long medicoId, LocalDate data);

        boolean existsByMedicoIdAndDataAndHorarioInicio(Long medicoId, LocalDate data, java.time.LocalTime horarioInicio);

        boolean existsByMedicoAndDataBetween(Medico medico, LocalDate inicio, LocalDate fim);

        List<Agenda> findByMedicoIdAndDataBetweenOrderByDataAscHorarioInicioAsc(Long medicoId, LocalDate inicio, LocalDate fim);

        @Query("SELECT a FROM Agenda a WHERE a.medico.id = :medicoId AND a.data BETWEEN :inicio AND :fim AND a.status = 'DISPONIVEL' ORDER BY a.data, a.horarioInicio")
        List<Agenda> findDisponiveisByMedicoIdAndDataBetween(Long medicoId, LocalDate inicio, LocalDate fim);

        List<Agenda> findByMedicoIdAndDataOrderByHorarioInicioAsc(Long medicoId, LocalDate data);

        List<Agenda> findByMedicoIdAndStatus(Long medicoId, Agenda.StatusAgenda status);
        @Transactional
        @Modifying
        @Query("UPDATE Agenda a SET a.status = 'INDISPONIVEL' WHERE a.id = :id")
        int marcarComoIndisponivel(Long id);
    }
