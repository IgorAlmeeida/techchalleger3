package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AgendamentoJaExistenteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ConflitoDeHorarioClienteException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.domain.service.BuscadorDeJanelaDeSlots;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CriarAgendamentoUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final ClienteRepositoryPort clientePort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final AgendamentoRepositoryPort agendamentoPort;
    private final AgendaRepositoryPort agendaPort;
    private final AgendaItemRepositoryPort agendaItemPort;
    private final ServicoRepositoryPort servicoPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final EmailSenderPort emailSenderPort;

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public Agendamento executar(String keycloakSub, Integer profissionalVinculoId,
                                Integer servicoId, LocalDate dataPreferencia,
                                Integer agendamentoIdEspecifico) {

        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));

        Cliente cliente = clientePort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Cliente para usuário", usuario.getId()));

        profissionalVinculoPort.buscarPorId(profissionalVinculoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", profissionalVinculoId));

        Servico servico = servicoPort.buscarPorId(servicoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", servicoId));
        int n = servico.getDuracaoMinutos() / 5;

        if (agendamentoPort.existeAgendadoPorClienteVinculoServico(cliente.getId(), profissionalVinculoId, servicoId)) {
            throw new AgendamentoJaExistenteException(
                    "Cliente já possui agendamento ativo para este profissional/serviço.");
        }

        if (agendamentoIdEspecifico != null) {
            return reservarSlotEspecifico(agendamentoIdEspecifico, cliente, servico, n);
        }

        List<Agendamento> disponiveis = agendamentoPort.buscarDisponiveisPorVinculo(profissionalVinculoId);
        Map<Integer, List<Agendamento>> porAgenda = BuscadorDeJanelaDeSlots.agrupar(disponiveis);
        Map<Integer, List<Agendamento>> porAgendaComServico = filtrarAgendas(porAgenda, servicoId);

        Optional<List<Agendamento>> janela = BuscadorDeJanelaDeSlots.buscarPrimeiraJanela(porAgendaComServico, n);
        if (janela.isPresent()) {
            return reservarGrupo(janela.get(), cliente, servico);
        }

        throw new OperacaoInvalidaException(
                "Sem horários disponíveis para o profissional e serviço solicitados.");
    }

    private Agendamento reservarSlotEspecifico(Integer agendamentoId, Cliente cliente,
                                               Servico servico, int n) {
        Agendamento slotSolicitado = agendamentoPort.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Agendamento", agendamentoId));

        if (!StatusAgendamentoEnum.DISPONIVEL.equals(slotSolicitado.getStatus())) {
            throw new OperacaoInvalidaException("O agendamento informado não está disponível.");
        }

        List<Agendamento> candidatos = agendamentoPort.listarPorAgendaId(slotSolicitado.getAgendaId())
                .stream()
                .filter(s -> StatusAgendamentoEnum.DISPONIVEL.equals(s.getStatus()))
                .filter(s -> !s.getHoraInicio().isBefore(slotSolicitado.getHoraInicio()))
                .sorted(Comparator.comparing(Agendamento::getHoraInicio))
                .toList();

        Optional<List<Agendamento>> janela = BuscadorDeJanelaDeSlots.buscarPrimeiraJanela(
                Map.of(slotSolicitado.getAgendaId(), candidatos), n);

        if (janela.isEmpty() || !janela.get().get(0).getId().equals(agendamentoId)) {
            throw new OperacaoInvalidaException(
                    "Não há slots consecutivos suficientes a partir do horário informado.");
        }

        return reservarGrupo(janela.get(), cliente, servico);
    }

    private Agendamento reservarGrupo(List<Agendamento> janela, Cliente cliente, Servico servico) {
        Integer servicoId = servico.getId();
        LocalDateTime agora = LocalDateTime.now();
        LocalTime novaHoraInicio = janela.get(0).getHoraInicio();
        LocalTime novaHoraFim = janela.get(janela.size() - 1).getHoraFim();

        Agenda agenda = agendaPort.buscarPorId(janela.get(0).getAgendaId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Agenda", janela.get(0).getAgendaId()));

        List<Agendamento> conflitos = agendamentoPort.buscarAgendadosPorClienteNaData(
                cliente.getId(), agenda.getDataAgenda());
        for (Agendamento existente : conflitos) {
            if (novaHoraInicio.isBefore(existente.getHoraFim()) && novaHoraFim.isAfter(existente.getHoraInicio())) {
                throw new ConflitoDeHorarioClienteException(
                        "Cliente já possui um agendamento das " + existente.getHoraInicio()
                        + " às " + existente.getHoraFim() + " nesta data.");
            }
        }

        Agendamento pai = janela.get(0);
        pai.setServicoId(servicoId);
        pai.setClienteId(cliente.getId());
        pai.setStatus(StatusAgendamentoEnum.AGENDADO);
        pai.setHoraFim(novaHoraFim);
        pai.setDhAtualizacao(agora);
        Agendamento paiSalvo = agendamentoPort.salvar(pai);

        for (int i = 1; i < janela.size(); i++) {
            Agendamento filho = janela.get(i);
            filho.setAgendamentoPaiId(paiSalvo.getId());
            filho.setServicoId(servicoId);
            filho.setClienteId(cliente.getId());
            filho.setStatus(StatusAgendamentoEnum.AGENDADO);
            filho.setDhAtualizacao(agora);
            agendamentoPort.salvar(filho);
        }

        paiSalvo.setDataAgenda(agenda.getDataAgenda());

        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", agenda.getProfissionalVinculoId()));
        String profissionalNome = profissionalPort.buscarPorId(vinculo.getProfissionalId())
                .map(p -> p.getNome()).orElse("Profissional");

        Map<String, Object> dadosEmail = new HashMap<>();
        dadosEmail.put("agendamentoId", paiSalvo.getId());
        dadosEmail.put("data", agenda.getDataAgenda().format(DATA_FMT));
        dadosEmail.put("horaInicio", novaHoraInicio.format(HORA_FMT));
        dadosEmail.put("horaFim", novaHoraFim.format(HORA_FMT));
        dadosEmail.put("servicoNome", servico.getNome());
        dadosEmail.put("profissionalNome", profissionalNome);

        emailSenderPort.enviar(new EmailMensagem(
                cliente.getEmail(),
                "CONFIRMACAO_AGENDAMENTO",
                dadosEmail
        ));

        return paiSalvo;
    }

    private Map<Integer, List<Agendamento>> filtrarAgendas(Map<Integer, List<Agendamento>> porAgenda,
                                                            Integer servicoId) {
        return porAgenda.entrySet().stream()
                .filter(e -> agendaItemPort.existePorAgendaIdsEServico(List.of(e.getKey()), servicoId))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
