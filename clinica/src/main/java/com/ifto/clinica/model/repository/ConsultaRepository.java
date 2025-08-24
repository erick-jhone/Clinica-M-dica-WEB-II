package com.ifto.clinica.model.repository;

import com.ifto.clinica.model.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    @Query("SELECT c FROM Consulta c WHERE c.agenda.data = :data")
    List<Consulta> findByData(@Param("data") LocalDate data);

    List<Consulta> findByPacienteId(Long pacienteId);

}
