package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GerarAgendaUseCase {

    private static final Map<DiaSemanaEnum, DayOfWeek> DIA_PARA_DOW = new EnumMap<>(Map.of(
            DiaSemanaEnum.DOMINGO, DayOfWeek.SUNDAY,
            DiaSemanaEnum.SEGUNDA, DayOfWeek.MONDAY,
            DiaSemanaEnum.TERCA,   DayOfWeek.TUESDAY,
            DiaSemanaEnum.QUARTA,  DayOfWeek.WEDNESDAY,
            DiaSemanaEnum.QUINTA,  DayOfWeek.THURSDAY,
            DiaSemanaEnum.SEXTA,   DayOfWeek.FRIDAY,
            DiaSemanaEnum.SABADO,  DayOfWeek.SATURDAY
    ));

    private final EscalaRepositoryPort escalaPort;
    private final EscalaItemRepositoryPort escalaItemPort;
    private final AgendaRepositoryPort agendaPort;
    private final AgendaItemRepositoryPort agendaItemPort;
    private final AgendamentoRepositoryPort agendamentoPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;

    @Transactional
    public List<Agenda> executar(Integer escalaId, LocalDate dataInicio, LocalDate dataFim,
                                  String keycloakSub, boolean isAdmin) {
        Escala escala = escalaPort.buscarPorId(escalaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Escala", escalaId));

        if (!isAdmin) {
            validarPosse(escala, keycloakSub);
        }

        List<EscalaItem> itensAtivos = escalaItemPort.listarAtivosPorEscalaId(escalaId);
        if (itensAtivos.isEmpty()) {
            throw new OperacaoInvalidaException("Escala não possui itens ativos para geração de agenda.");
        }

        DayOfWeek diaDaSemana = DIA_PARA_DOW.get(escala.getDiaSemana());
        List<Agenda> agendasGeradas = new ArrayList<>();
        LocalDate cursor = dataInicio;

        while (!cursor.isAfter(dataFim)) {
            if (cursor.getDayOfWeek() == diaDaSemana) {
                agendasGeradas.add(gerarAgendaParaData(escala, itensAtivos, cursor));
            }
            cursor = cursor.plusDays(1);
        }

        if (agendasGeradas.isEmpty()) {
            throw new OperacaoInvalidaException(
                    "Nenhuma data no intervalo informado cai no dia da semana da escala (" + escala.getDiaSemana() + ").");
        }

        return agendasGeradas;
    }

    private void validarPosse(Escala escala, String keycloakSub) {
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));

        Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional para usuário", usuario.getId()));

        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(escala.getProfissionalVinculoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", escala.getProfissionalVinculoId()));

        if (!vinculo.getProfissionalId().equals(profissional.getId())) {
            throw new AcessoNegadoException("Profissional não autorizado a gerar agenda para esta escala.");
        }
    }

    private Agenda gerarAgendaParaData(Escala escala, List<EscalaItem> itensAtivos, LocalDate data) {
        if (agendaPort.existeAgendaPorVinculoEData(escala.getProfissionalVinculoId(), data)) {
            throw new OperacaoInvalidaException(
                    "Já existe uma agenda para o vínculo " + escala.getProfissionalVinculoId() + " na data " + data + ".");
        }

        LocalDateTime agora = LocalDateTime.now();

        Agenda agenda = agendaPort.salvar(Agenda.builder()
                .escalaId(escala.getId())
                .dataAgenda(data)
                .diaSemana(escala.getDiaSemana())
                .horaInicio(escala.getHoraInicio())
                .horaFim(escala.getHoraFim())
                .estabelecimentoId(escala.getEstabelecimentoId())
                .profissionalVinculoId(escala.getProfissionalVinculoId())
                .dhInsert(agora)
                .build());

        for (EscalaItem escalaItem : itensAtivos) {
            agendaItemPort.salvar(AgendaItem.builder()
                    .agendaId(agenda.getId())
                    .servicoId(escalaItem.getServicoId())
                    .dhInsert(agora)
                    .build());
        }

        gerarSlots5Min(agenda, agora);

        return agenda;
    }

    private void gerarSlots5Min(Agenda agenda, LocalDateTime agora) {
        LocalTime cursor = agenda.getHoraInicio();
        while (!cursor.plusMinutes(5).isAfter(agenda.getHoraFim())) {
            agendamentoPort.salvar(Agendamento.builder()
                    .agendaId(agenda.getId())
                    .horaInicio(cursor)
                    .horaFim(cursor.plusMinutes(5))
                    .status(StatusAgendamentoEnum.DISPONIVEL)
                    .presencaConfirmada(false)
                    .dhInsert(agora)
                    .build());
            cursor = cursor.plusMinutes(5);
        }
    }
}
