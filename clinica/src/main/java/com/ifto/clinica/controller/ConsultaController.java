package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.*;
import com.ifto.clinica.model.repository.AgendaRepository;
import com.ifto.clinica.model.repository.ConsultaRepository;
import com.ifto.clinica.model.repository.MedicoRepository;
import com.ifto.clinica.model.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final AgendaRepository agendaRepository;

    public ConsultaController(ConsultaRepository consultaRepository,
                              PacienteRepository pacienteRepository,
                              MedicoRepository medicoRepository,
                              AgendaRepository agendaRepository) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.agendaRepository = agendaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("consultas", consultaRepository.findAll());
        return "consulta/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model,
                       @RequestParam(required = false) Long medicoId,
                       @RequestParam(required = false) Long agendaId) {

        Consulta consulta = new Consulta();

        if (agendaId != null) {
            Agenda agenda = agendaRepository.findById(agendaId)
                    .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada"));
            consulta.setAgenda(agenda);
            consulta.setMedico(agenda.getMedico());
        }

        model.addAttribute("consulta", consulta);
        model.addAttribute("pacientes", pacienteRepository.findAll());
        model.addAttribute("medicos", medicoRepository.findAll());

        if (medicoId != null) {
            List<Agenda> agendas = agendaRepository.findByMedicoIdAndStatus(medicoId, Agenda.StatusAgenda.DISPONIVEL);
            model.addAttribute("agendas", agendas);
            model.addAttribute("medicoSelecionado", medicoId);
        } else if (consulta.getMedico() != null) {
            List<Agenda> agendas = agendaRepository.findByMedicoIdAndStatus(consulta.getMedico().getId(), Agenda.StatusAgenda.DISPONIVEL);
            model.addAttribute("agendas", agendas);
            model.addAttribute("medicoSelecionado", consulta.getMedico().getId());
        } else {
            model.addAttribute("agendas", List.of());
        }

        return "consulta/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("consulta", consultaRepository.findById(id).orElseThrow());
        return "consulta/form";
    }

    @PostMapping("/save")
    public String save(@Valid Consulta consulta, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("medicos", medicoRepository.findAll());

            if (consulta.getMedico() != null && consulta.getMedico().getId() != null) {
                List<Agenda> agendas = agendaRepository.findByMedicoIdAndStatus(consulta.getMedico().getId(), Agenda.StatusAgenda.DISPONIVEL);
                model.addAttribute("agendas", agendas);
                model.addAttribute("medicoSelecionado", consulta.getMedico().getId());
            } else {
                model.addAttribute("agendas", List.of());
            }

            return "consulta/form";
        }

        consultaRepository.save(consulta);
        return "redirect:/consultas";
    }

    @GetMapping("/detalhe/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));

        List<Exame> exames = consulta.getExames();

        model.addAttribute("consulta", consulta);
        model.addAttribute("exames", exames);

        return "consulta/detalhe";
    }

}
