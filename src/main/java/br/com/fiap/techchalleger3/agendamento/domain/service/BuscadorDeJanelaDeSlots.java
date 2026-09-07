package br.com.fiap.techchalleger3.agendamento.domain.service;

import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BuscadorDeJanelaDeSlots {

    private BuscadorDeJanelaDeSlots() {}

    public static Map<Integer, List<Agendamento>> agrupar(List<Agendamento> slots) {
        return slots.stream()
                .sorted(Comparator.comparing(Agendamento::getHoraInicio))
                .collect(Collectors.groupingBy(
                        Agendamento::getAgendaId,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    public static Optional<List<Agendamento>> buscarPrimeiraJanela(
            Map<Integer, List<Agendamento>> porAgenda, int nSlots) {
        for (Map.Entry<Integer, List<Agendamento>> entry : porAgenda.entrySet()) {
            List<Agendamento> slots = entry.getValue();
            Optional<List<Agendamento>> janela = buscarJanelaConsecutiva(slots, nSlots);
            if (janela.isPresent()) {
                return janela;
            }
        }
        return Optional.empty();
    }

    private static Optional<List<Agendamento>> buscarJanelaConsecutiva(
            List<Agendamento> slots, int nSlots) {
        if (slots.size() < nSlots) return Optional.empty();
        for (int i = 0; i <= slots.size() - nSlots; i++) {
            List<Agendamento> candidatos = slots.subList(i, i + nSlots);
            if (saoConsecutivos(candidatos)) {
                return Optional.of(new ArrayList<>(candidatos));
            }
        }
        return Optional.empty();
    }

    public static List<Agendamento> buscarIniciosDeJanelas(
            Map<Integer, List<Agendamento>> porAgenda, int nSlots) {
        List<Agendamento> inicios = new ArrayList<>();
        for (Map.Entry<Integer, List<Agendamento>> entry : porAgenda.entrySet()) {
            List<Agendamento> slots = entry.getValue();
            for (int i = 0; i <= slots.size() - nSlots; i++) {
                List<Agendamento> candidatos = slots.subList(i, i + nSlots);
                if (saoConsecutivos(candidatos)) {
                    inicios.add(candidatos.get(0));
                }
            }
        }
        return inicios;
    }

    private static boolean saoConsecutivos(List<Agendamento> slots) {
        for (int i = 0; i < slots.size() - 1; i++) {
            if (!slots.get(i).getHoraFim().equals(slots.get(i + 1).getHoraInicio())) {
                return false;
            }
        }
        return true;
    }
}
