package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.Consulta;
import com.ifto.clinica.model.entity.Exame;
import com.ifto.clinica.model.repository.ConsultaRepository;
import com.ifto.clinica.model.repository.ExameRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/exames")
public class ExameController {

    private final ExameRepository exameRepository;
    private final ConsultaRepository consultaRepository;

    public ExameController(ExameRepository exameRepository, ConsultaRepository consultaRepository) {
        this.exameRepository = exameRepository;
        this.consultaRepository = consultaRepository;
    }

    @GetMapping("/novo/{consultaId}")
    public String novo(@PathVariable Long consultaId, Model model) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));

        Exame exame = new Exame();
        exame.setConsulta(consulta);

        model.addAttribute("exame", exame);
        model.addAttribute("consulta", consulta);

        model.addAttribute("pagina","exame/form");
        return "fragments/main";
    }

    @PostMapping("/save")
    public String save(@Valid Exame exame, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pagina","exame/form");
            return "fragments/main";
        }
        exameRepository.save(exame);
        return "redirect:/consultas/detalhe/" + exame.getConsulta().getId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        Exame exame = exameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exame não encontrado"));
        Long consultaId = exame.getConsulta().getId();
        exameRepository.delete(exame);
        return "redirect:/consultas/detalhe/" + consultaId;
    }
}