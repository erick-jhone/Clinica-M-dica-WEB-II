package com.ifto.clinica.model.repository;

import com.ifto.clinica.model.entity.Cidade;
import com.ifto.clinica.model.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Long> {
    Optional<Estado> findByUf(String uf);

}
