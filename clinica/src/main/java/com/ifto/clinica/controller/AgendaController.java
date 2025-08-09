package com.ifto.clinica.controller;

import com.ifto.clinica.model.entity.Agenda;
import com.ifto.clinica.model.entity.IntervaloMedico;
import com.ifto.clinica.model.entity.Medico;
import com.ifto.clinica.model.repository.AgendaRepository;
import com.ifto.clinica.model.repository.MedicoRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/secretaria")
public class AgendaController {

    private final MedicoRepository medicoRepository;
    private final AgendaRepository agendaRepository;

    public AgendaController(MedicoRepository medicoRepository, AgendaRepository agendaRepository) {
        this.medicoRepository = medicoRepository;
        this. agendaRepository = agendaRepository;
    }

    @GetMapping("/{id}/agenda/calendario")
    public String verCalendario(@PathVariable Long id, Model model) {
        YearMonth mesAtual = YearMonth.now();
        LocalDate primeiroDia = mesAtual.atDay(1);
        LocalDate ultimoDia = mesAtual.atEndOfMonth();

        Medico medico = medicoRepository.findById(id).orElseThrow();

        boolean jaTemAgendas = agendaRepository.existsByMedicoAndDataBetween(medico, primeiroDia, ultimoDia);

        if (!jaTemAgendas) {
            List<Agenda> agendasParaMes = new ArrayList<>();
            for (LocalDate dia = primeiroDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
                agendasParaMes.addAll(gerarAgendasComIntervalo(medico, dia, Duration.ofMinutes(30)));
            }
            agendaRepository.saveAll(agendasParaMes);
        }

        // Geração do calendário visual
        LocalDate inicio = primeiroDia.minusDays(primeiroDia.getDayOfWeek().getValue() % 7);
        List<List<DiaCalendario>> semanas = new ArrayList<>();

        LocalDate data = inicio;
        for (int i = 0; i < 6; i++) {
            List<DiaCalendario> semana = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                semana.add(data.getMonth() == mesAtual.getMonth() ? new DiaCalendario(data.getDayOfMonth(), data) : null);
                data = data.plusDays(1);
            }
            semanas.add(semana);
        }

        model.addAttribute("medico", medico);
        model.addAttribute("calendario", semanas);
        model.addAttribute("mesNome", mesAtual.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")));
        model.addAttribute("ano", mesAtual.getYear());

        return "agenda/agenda-calendario";
    }


    @GetMapping("/agendas-disponiveis")
    @ResponseBody
    public List<Agenda> agendasDisponiveis(@RequestParam Long medicoId) {
        return agendaRepository.findByMedicoIdAndStatus(medicoId, Agenda.StatusAgenda.DISPONIVEL);
    }

    @GetMapping("/{id}/agenda/horarios")
    public String horarios(@PathVariable Long id,
                           @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                           Model model) {

        List<Agenda> agendas = agendaRepository.findByMedicoIdAndDataOrderByHorarioInicioAsc(id, data);

        model.addAttribute("agendas", agendas);
        model.addAttribute("medicoId", id);
        model.addAttribute("data", data);
        return "agenda/list-horarios-agenda";
    }

    public List<String> gerarHorarios(LocalTime inicio, LocalTime fim, int minutosPorConsulta) {
        List<String> horarios = new ArrayList<>();
        LocalTime atual = inicio;

        while (!atual.plusMinutes(minutosPorConsulta).isAfter(fim)) {
            String horario = atual + " às " + atual.plusMinutes(minutosPorConsulta);
            horarios.add(horario);
            atual = atual.plusMinutes(minutosPorConsulta);
        }

        return horarios;
    }

    static class DiaCalendario {
        public int dia;
        public LocalDate data;

        public DiaCalendario(int dia, LocalDate data) {
            this.dia = dia;
            this.data = data;
        }

        public int getDia() { return dia; }
        public LocalDate getData() { return data; }
    }

    public List<Agenda> gerarAgendasParaData(
            Medico medico,
            LocalDate data,
            LocalTime inicio,
            LocalTime fim,
            Duration intervalo
    ) {
        List<Agenda> agendas = new ArrayList<>();

        LocalTime horarioAtual = inicio;
        while (horarioAtual.plus(intervalo).compareTo(fim) <= 0) {
            Agenda agenda = new Agenda();
            agenda.setData(data);
            agenda.setHorarioInicio(horarioAtual);
            agenda.setHorarioFim(horarioAtual.plus(intervalo));
            agenda.setMedico(medico);
            agenda.setStatus(Agenda.StatusAgenda.DISPONIVEL);
            agendas.add(agenda);

            horarioAtual = horarioAtual.plus(intervalo);
        }

        return agendas;
    }

    private List<Agenda> gerarAgendasComIntervalo(
            Medico medico,
            LocalDate data,
            Duration duracaoConsulta
    ) {
        List<Agenda> agendas = new ArrayList<>();
        LocalTime horaAtual = medico.getHorarioInicioAtendimento();
        LocalTime fim = medico.getHorarioFimAtendimento();

        List<IntervaloMedico> intervalos = medico.getIntervalos();

        while (horaAtual.plus(duracaoConsulta).minusMinutes(1).isBefore(fim)) {
            LocalTime proximoHorario = horaAtual.plus(duracaoConsulta);

            LocalTime finalHoraAtual = horaAtual;
            boolean emIntervalo = intervalos.stream().anyMatch(intervalo ->
                    !finalHoraAtual.isAfter(intervalo.getHoraFim()) && !proximoHorario.isBefore(intervalo.getHoraInicio())
            );

            if (!emIntervalo) {
                Agenda agenda = new Agenda();
                agenda.setMedico(medico);
                agenda.setData(data);
                agenda.setHorarioInicio(horaAtual);
                agenda.setHorarioFim(proximoHorario);
                agenda.setStatus(Agenda.StatusAgenda.DISPONIVEL);
                agendas.add(agenda);
            }

            horaAtual = proximoHorario;
        }

        return agendas;
    }


}