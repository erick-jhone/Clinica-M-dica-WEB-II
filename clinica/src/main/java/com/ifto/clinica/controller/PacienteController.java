package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.*;
import com.ifto.clinica.model.repository.*;
import com.ifto.clinica.model.security.UsuarioDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private final EnderecoRepository enderecoRepository;
    private final CidadeRepository cidadeRepository;
    private final EstadoRepository estadoRepository;

    public PacienteController(PacienteRepository pacienteRepository,
                              ConsultaRepository consultaRepository,
                              UsuarioRepository usuarioRepository,
                              RoleRepository roleRepository,
                              CidadeRepository cidadeRepository,
                              EnderecoRepository enderecoRepository,
                              EstadoRepository estadoRepository,
                              PasswordEncoder passwordEncoder) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.cidadeRepository = cidadeRepository;
        this.enderecoRepository = enderecoRepository;
        this.estadoRepository = estadoRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @GetMapping("/form")
    public ModelAndView form(Paciente paciente) {
        return new ModelAndView("/paciente/form");
    }


    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findAll());
        model.addAttribute("pagina", "paciente/listar");
        return "fragments/main";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("paciente", new Paciente());

        return "paciente/form";
    }

    @PostMapping("/save")
    public ModelAndView save(@Valid Paciente paciente, BindingResult result,
                             @RequestParam(required = false) String username,
                             @RequestParam(required = false) String password) {
        if (result.hasErrors()) {
            return form(paciente);
        }

        Endereco endereco = paciente.getEndereco();
        if (endereco != null) {

            Estado estado = endereco.getCidade() != null ? endereco.getCidade().getEstado() : null;
            if (estado != null) {
                if (estado.getId() != null) {
                    estado = estadoRepository.findById(estado.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Estado inválido!"));
                } else {
                    Optional<Estado> estadoExistente = estadoRepository.findByUf(estado.getUf());
                    if (estadoExistente.isPresent()) {
                        estado = estadoExistente.get();
                    } else {
                        estado = estadoRepository.save(estado);
                    }
                }
            }

            Cidade cidade = endereco.getCidade();
            if (cidade != null) {
                cidade.setEstado(estado);
                if (cidade.getId() != null) {
                    cidade = cidadeRepository.findById(cidade.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Cidade inválida!"));
                } else {
                    Optional<Cidade> cidadeExistente = cidadeRepository.findByNomeAndEstado(cidade.getNome(), estado);
                    if (cidadeExistente.isPresent()) {
                        cidade = cidadeExistente.get();
                    } else {
                        cidade = cidadeRepository.save(cidade);
                    }
                }
            } else {
                throw new IllegalArgumentException("Cidade deve estar cadastrada antes de usar no endereço.");
            }

            endereco.setCidade(cidade);
            Optional<Endereco> enderecoExistente = enderecoRepository
                    .findByCepAndNumeroAndCidade(endereco.getCep(), endereco.getNumero(), cidade);
            if (enderecoExistente.isPresent()) {
                paciente.setEndereco(enderecoExistente.get());
            } else {
                endereco = enderecoRepository.save(endereco);
                paciente.setEndereco(endereco);
            }
        }
        if (paciente.getId() == null) {
            Usuario usuario = paciente.getUsuario();

            if (usuario != null && usuario.getLogin() != null && !usuario.getLogin().isBlank()
                    && usuario.getPassword() != null && !usuario.getPassword().isBlank()) {

                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

                if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                    Role roleCliente = roleRepository.findByNome("ROLE_CLIENTE");
                    if (roleCliente == null) {
                        roleCliente = new Role();
                        roleCliente.setNome("ROLE_CLIENTE");
                        roleRepository.save(roleCliente);
                    }

                    List<Role> roles = new ArrayList<>();
                    roles.add(roleCliente);
                    usuario.setRoles(roles);
                }

                usuarioRepository.save(usuario);
                paciente.setUsuario(usuario);
            } else {
                Usuario usuarioExistente = pacienteRepository.findById(paciente.getId()).get().getUsuario();
                paciente.setUsuario(usuarioExistente);
            }

        }


        pacienteRepository.save(paciente);
        return new ModelAndView("redirect:/pacientes");
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteRepository.findById(id).orElseThrow();
        model.addAttribute("paciente", paciente);
        return "paciente/form";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        pacienteRepository.deleteById(id);
        return "redirect:/pacientes";
    }

    @GetMapping("/{id}/consultas")
    public String consultasDoPaciente(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteRepository.findById(id).orElseThrow();
        model.addAttribute("consultas", paciente.getConsultas());
        model.addAttribute("paciente", paciente);
        return "paciente/consultas";
    }

    @GetMapping("/buscar")
    public String buscarPorNome(@RequestParam("nome") String nome, Model model) {
        model.addAttribute("pacientes", pacienteRepository.buscarPorNome(nome));
        model.addAttribute("pagina", "paciente/listar");
        return "fragments/main";
    }


    @GetMapping("/prontuario/{id}")
    public String prontuario(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        List<Consulta> consultas = consultaRepository.findByPacienteId(id);

        model.addAttribute("paciente", paciente);
        model.addAttribute("consultas", consultas);
        model.addAttribute("pagina", "paciente/prontuario");
        return "fragments/main";
    }

    @GetMapping("/prontuario/visitante")
    public String prontuarioVisitante(Model model, Authentication authentication) {

        UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();

        if (usuarioDetails != null
                && usuarioDetails.getUsuario() != null
                && usuarioDetails.getUsuario().getPaciente() != null) {

            Paciente paciente = usuarioDetails.getUsuario().getPaciente();

            List<Consulta> consultas = consultaRepository.findByPacienteId(paciente.getId());

            model.addAttribute("paciente", paciente);
            model.addAttribute("consultas", consultas);
            model.addAttribute("pagina", "paciente/prontuario");
            return "fragments/main-cliente";
        }

        throw new AccessDeniedException("Você não tem permissão para acessar este prontuário.");
    }
}
