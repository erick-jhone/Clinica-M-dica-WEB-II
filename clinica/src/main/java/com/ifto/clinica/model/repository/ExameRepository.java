package com.ifto.clinica.model.repository;

import com.ifto.clinica.model.entity.Exame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExameRepository extends JpaRepository<Exame, Long> {
    List<Exame> findByConsultaId(Long consultaId);
}