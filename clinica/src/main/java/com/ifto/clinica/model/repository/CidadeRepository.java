package com.ifto.clinica.model.repository;

import com.ifto.clinica.model.entity.Cidade;
import com.ifto.clinica.model.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {
    Optional<Cidade> findByNomeAndEstado(String nome, Estado estado);

    // Opcional: busca por nome somente
    Optional<Cidade> findByNome(String nome);
}
