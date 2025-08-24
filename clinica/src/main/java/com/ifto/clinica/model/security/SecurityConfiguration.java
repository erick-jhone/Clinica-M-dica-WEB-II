package com.ifto.clinica.model.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers("/pacientes/novo").permitAll()
                        .requestMatchers(HttpMethod.POST, "/pacientes/novo").permitAll()
                        .requestMatchers("/medicos/list").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pessoafisica/save").permitAll()
                        .requestMatchers("/login/register", "/templates/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/register").permitAll()
                        .requestMatchers("/register", "/register/**").permitAll()
                        .requestMatchers("/pacientes/form", "/pacientes/save", "/pacientes/novo").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(customizer -> customizer
                        .loginPage("/login")
                        .defaultSuccessUrl("/consultas", true)
                        .permitAll()
                )
                .httpBasic(withDefaults())
                .logout(LogoutConfigurer::permitAll)
                .rememberMe(withDefaults());
        return http.build();
    }

    // 🔑 O encoder para criptografar senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔑 Configura o AuthenticationManager usando o UserDetailsService padrão
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http, PasswordEncoder passwordEncoder,
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService
    ) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }
}
