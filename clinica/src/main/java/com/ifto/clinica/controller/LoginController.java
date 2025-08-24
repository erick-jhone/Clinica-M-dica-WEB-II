package com.ifto.clinica.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public String login() {
        return "login/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        // Pega o usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Se não tiver ROLE_ADMIN, encaminha para formulário de paciente
        if (auth != null && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/pacientes/novo";
        }

        // Caso seja admin, encaminha para tela de registro de usuários/admins
        return "register/register";
    }
}
