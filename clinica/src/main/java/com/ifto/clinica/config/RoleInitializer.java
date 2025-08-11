package com.ifto.clinica.config;

import com.ifto.clinica.model.entity.Role;
import com.ifto.clinica.model.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleInitializer {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initRoles() {
        List<String> rolesPadrao = Arrays.asList("ROLE_CLIENTE", "ROLE_ADMIN", "ROLE_MEDICO", "ROLE_SECRETARIO");

        for (String roleName : rolesPadrao) {
            Role role = roleRepository.findByNome(roleName);
            if (role == null) {
                role = new Role();
                role.setNome(roleName);
                roleRepository.save(role);
            }
        }
    }
}
