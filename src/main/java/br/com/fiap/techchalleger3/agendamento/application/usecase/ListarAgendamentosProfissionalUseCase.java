package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ClienteResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarAgendamentosProfissionalUseCase {

    private static final List<StatusAgendamentoEnum> STATUS_PADRAO = List.of(
            StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO);

    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final AgendamentoRepositoryPort agendamentoPort;
    private final AgendaRepositoryPort agendaPort;
    private final ServicoRepositoryPort servicoPort;
    private final ClienteRepositoryPort clientePort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;

    public List<ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido> executar(
            String keycloakSub,
            Integer profissionalVinculoIdFiltro,
            List<StatusAgendamentoEnum> statuses,
            LocalDate dataInicio,
            LocalDate dataFim,
            boolean isAdmin) {

        List<Integer> vinculoIds;
        if (isAdmin) {
            if (profissionalVinculoIdFiltro == null) {
                throw new br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException(
                        "Admin deve informar profissionalVinculoId para listar agendamentos do profissional.");
            }
            vinculoIds = List.of(profissionalVinculoIdFiltro);
        } else {
            Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));

            Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional para usuário", usuario.getId()));

            if (profissionalVinculoIdFiltro != null) {
                vinculoIds = List.of(profissionalVinculoIdFiltro);
            } else {
                vinculoIds = profissionalVinculoPort.listarPorProfissionalId(profissional.getId())
                        .stream().map(ProfissionalVinculo::getId).toList();
            }
        }

        List<StatusAgendamentoEnum> filtroStatus = (statuses == null || statuses.isEmpty()) ? STATUS_PADRAO : statuses;

        List<Agendamento> agendamentos = agendamentoPort.buscarPaisPorProfissionalVinculoIds(
                vinculoIds, filtroStatus, dataInicio, dataFim);

        return agendamentos.stream().map(this::enriquecer).toList();
    }

    private ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido enriquecer(Agendamento a) {
        Agenda agenda = agendaPort.buscarPorId(a.getAgendaId()).orElseThrow();
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId()).orElseThrow();
        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(vinculo.getEstabelecimentoId()).orElseThrow();

        ServicoResumo servico = null;
        if (a.getServicoId() != null) {
            servico = servicoPort.buscarPorId(a.getServicoId())
                    .map(s -> new ServicoResumo(s.getId(), s.getNome(), s.getDuracaoMinutos()))
                    .orElse(null);
        }
        ClienteResumo cliente = null;
        if (a.getClienteId() != null) {
            cliente = clientePort.buscarPorId(a.getClienteId())
                    .map(c -> new ClienteResumo(c.getId(), c.getNome()))
                    .orElse(null);
        }

        return ListarMeusAgendamentosClienteUseCase.AgendamentoEnriquecido.builder()
                .id(a.getId())
                .agendaId(a.getAgendaId())
                .dataAgenda(agenda.getDataAgenda())
                .horaInicio(a.getHoraInicio())
                .horaFim(a.getHoraFim())
                .status(a.getStatus())
                .servico(servico)
                .profissional(new ProfissionalResumo(profissional.getId(), profissional.getNome()))
                .cliente(cliente)
                .estabelecimento(new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome()))
                .presencaConfirmada(a.getPresencaConfirmada())
                .build();
    }
}
