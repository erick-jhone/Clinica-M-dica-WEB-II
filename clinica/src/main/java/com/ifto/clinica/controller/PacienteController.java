package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.*;
import com.ifto.clinica.model.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


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

//    @GetMapping("/form")
//    public String form(Model model) {
//        model.addAttribute("paciente", new Paciente());
//        return "paciente/form";
//    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findAll());
        return "paciente/listar";
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

        // 🔹 Processa endereço
        Endereco endereco = paciente.getEndereco();
        if (endereco != null) {

            // 1️⃣ Processa Estado
            Estado estado = endereco.getCidade() != null ? endereco.getCidade().getEstado() : null;
            if (estado != null) {
                if (estado.getId() != null) {
                    estado = estadoRepository.findById(estado.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Estado inválido!"));
                } else {
                    // tenta encontrar pelo nome sigla, caso já exista
                    Optional<Estado> estadoExistente = estadoRepository.findByUf(estado.getUf());
                    if (estadoExistente.isPresent()) {
                        estado = estadoExistente.get();
                    } else {
                        estado = estadoRepository.save(estado); // salva se não existir
                    }
                }
            }

            // 2️⃣ Processa Cidade
            Cidade cidade = endereco.getCidade();
            if (cidade != null) {
                cidade.setEstado(estado); // garante que cidade aponta para o estado persistido
                if (cidade.getId() != null) {
                    cidade = cidadeRepository.findById(cidade.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Cidade inválida!"));
                } else {
                    Optional<Cidade> cidadeExistente = cidadeRepository.findByNomeAndEstado(cidade.getNome(), estado);
                    if (cidadeExistente.isPresent()) {
                        cidade = cidadeExistente.get();
                    } else {
                        cidade = cidadeRepository.save(cidade); // salva cidade se não existir
                    }
                }
            } else {
                throw new IllegalArgumentException("Cidade deve estar cadastrada antes de usar no endereço.");
            }

            // 3️⃣ Processa Endereco
            endereco.setCidade(cidade); // seta cidade persistida
            Optional<Endereco> enderecoExistente = enderecoRepository
                    .findByCepAndNumeroAndCidade(endereco.getCep(), endereco.getNumero(), cidade);
            if (enderecoExistente.isPresent()) {
                paciente.setEndereco(enderecoExistente.get());
            } else {
                endereco = enderecoRepository.save(endereco); // salva novo endereço
                paciente.setEndereco(endereco);
            }
        }

        Usuario usuario = paciente.getUsuario();
        if (usuario != null && usuario.getLogin() != null && !usuario.getLogin().isBlank()
                && usuario.getPassword() != null && !usuario.getPassword().isBlank()) {

            // Criptografa senha antes de salvar
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            // Garante ROLE_CLIENTE por padrão
            if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                Role roleCliente = roleRepository.findByNome("ROLE_CLIENTE");
                if (roleCliente == null) {
                    roleCliente = new Role();
                    roleCliente.setNome("ROLE_CLIENTE");
                    roleRepository.save(roleCliente);
                }
                usuario.setRoles(Collections.singletonList(roleCliente));
            }

            usuarioRepository.save(usuario);
            paciente.setUsuario(usuario);
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
        return "paciente/listar";
    }


    @GetMapping("/prontuario/{id}")
    public String prontuario(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        // Carregar consultas e exames associados
        List<Consulta> consultas = consultaRepository.findByPacienteId(id);

        model.addAttribute("paciente", paciente);
        model.addAttribute("consultas", consultas);

        return "paciente/prontuario";
    }

}
