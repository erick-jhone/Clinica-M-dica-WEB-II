package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.Usuario;
import com.ifto.clinica.model.security.UsuarioDetails;

import com.ifto.clinica.model.security.UsuarioDetailsConfig;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home/visitante")
    public String homeVisitante(Model model, Authentication authentication) {
        UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();

        String nomeUsuario = "Visitante";
        if (usuarioDetails != null
                && usuarioDetails.getUsuario() != null
                && usuarioDetails.getUsuario().getPaciente() != null) {
            nomeUsuario = usuarioDetails.getUsuario().getPaciente().getNome();
        }

        model.addAttribute("title", "Home");
        model.addAttribute("pagina", "visitante/home");
        model.addAttribute("nome", nomeUsuario);

        return "fragments/main-cliente";
    }
}