package com.ifto.clinica.controller;

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

    @GetMapping("register")
    public String registerForm() {
        return "register/register";
    }

//    @PostMapping("/register")
//    public String register(Usuario user) {
//        return "redirect:/login?registered";
//    }
}
