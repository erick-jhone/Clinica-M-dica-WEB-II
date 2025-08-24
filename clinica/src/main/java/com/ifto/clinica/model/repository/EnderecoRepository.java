package com.ifto.clinica.model.repository;

import com.ifto.clinica.model.entity.Cidade;
import com.ifto.clinica.model.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    Optional<Endereco> findByCepAndNumeroAndCidade(String cep, String numero, Cidade cidade);

}
