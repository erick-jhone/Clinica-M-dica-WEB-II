package com.ifto.clinica.model.repository;

import com.ifto.clinica.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByNome(String nome);
}
