package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.Paciente;
import com.ifto.clinica.model.entity.Pessoa;
import com.ifto.clinica.model.repository.ConsultaRepository;
import com.ifto.clinica.model.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;

    public PacienteController(PacienteRepository pacienteRepository, ConsultaRepository consultaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    @GetMapping("/form")
    public ModelAndView form(Pessoa pessoa){
        return new ModelAndView("/paciente/form");
    }

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
    public ModelAndView save(@Valid Paciente pessoa, BindingResult result){
        if(result.hasErrors())
            return form(pessoa);

        pacienteRepository.save(pessoa);
        return new ModelAndView("redirect:/pacientes");
    }

    @PostMapping("/salvar")
    public String salvar(Paciente paciente) {
        pacienteRepository.save(paciente);
        return "redirect:/pacientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacienteRepository.findById(id).orElseThrow());
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


}
