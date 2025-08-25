package com.ifto.clinica.controller;


import com.ifto.clinica.model.entity.Medico;
import com.ifto.clinica.model.entity.Paciente;
import com.ifto.clinica.model.entity.Pessoa;
import com.ifto.clinica.model.repository.MedicoRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoRepository medicoRepository;

    public MedicoController(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @GetMapping("/form")
    public ModelAndView form(Medico pessoa){
        return new ModelAndView("/medico/form");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("medicos", medicoRepository.findAll());
        return "medico/listar";
    }
    @GetMapping("/visitante")
    public String listarMedicos(Model model) {
        model.addAttribute("medicos", medicoRepository.findAll());
        model.addAttribute("title", "Médicos Disponíveis");
        model.addAttribute("pagina", "medico/lista-visitantes");
        return "fragments/main-cliente";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("medico", new Medico());
        return "medico/form";
    }

    @PostMapping("/save")
    public ModelAndView save(@Valid Medico pessoa, BindingResult result){
        if(result.hasErrors())
            return form(pessoa);

        medicoRepository.save(pessoa);
        return new ModelAndView("redirect:/medicos");
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Medico medico) {
        Medico medicoSalvo = medicoRepository.save(medico);

        if (medicoSalvo.getIntervalos() != null) {
            medicoSalvo.getIntervalos().forEach(intervalo -> intervalo.setMedico(medicoSalvo));
            medicoRepository.save(medicoSalvo);
        }

        return "redirect:/medicos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("medico", medicoRepository.findById(id).orElseThrow());
        return "medico/form";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        medicoRepository.deleteById(id);
        return "redirect:/medicos";
    }

    @GetMapping("/{id}/consultas")
    public String consultasDoMedico(@PathVariable Long id, Model model) {
        Medico medico = medicoRepository.findById(id).orElseThrow();
        model.addAttribute("consultas", medico.getConsultas());
        model.addAttribute("medico", medico);
        return "medico/consultas";
    }

    @GetMapping("/buscar")
    public String buscarPorNome(@RequestParam("nome") String nome, Model model) {
        model.addAttribute("medicos", medicoRepository.buscarPorNome(nome));
        return "medico/listar";
    }


}