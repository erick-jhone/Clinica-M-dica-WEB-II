package com.ifto.clinica.model.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectUrl = "/"; // fallback padrão

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_ADMIN":
                    redirectUrl = "/consultas";
                    break;
                case "ROLE_CLIENTE":
                    redirectUrl = "/home/visitante";
                    break;
                case "ROLE_MEDICO":
                    redirectUrl = "/medicos/";
                    break;
                case "ROLE_SECRETARIO":
                    redirectUrl = "/consultas";
                    break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
