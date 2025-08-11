package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.Role;
import com.ifto.clinica.model.entity.Usuario;
import com.ifto.clinica.model.repository.RoleRepository;
import com.ifto.clinica.model.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class RegistroController {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroController(UsuarioRepository usuarioRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String formRegister() {
        return "register/register";
    }

    @PostMapping("/register")
    public String salvarNovoUsuario(@RequestParam String username,
                                    @RequestParam String password,
                                    @RequestParam(required = false) List<String> roles) {

        if (usuarioRepository.findByLogin(username).isPresent()) {
            return "redirect:/register?error=userexists";
        }

        List<Role> rolesSelecionadas = new ArrayList<>();

        if (roles == null || roles.isEmpty()) {
            roles = Collections.singletonList("ROLE_USUARIO");
        }

        for (String roleName : roles) {
            Role role = roleRepository.findByNome(roleName);
            if (role == null) {
                role = new Role();
                role.setNome(roleName);
                roleRepository.save(role);
            }
            rolesSelecionadas.add(role);
        }

        Usuario novo = new Usuario();
        novo.setLogin(username);
        novo.setPassword(passwordEncoder.encode(password));
        novo.setRoles(rolesSelecionadas);

        usuarioRepository.save(novo);

        return "redirect:/login?registered";
    }
}