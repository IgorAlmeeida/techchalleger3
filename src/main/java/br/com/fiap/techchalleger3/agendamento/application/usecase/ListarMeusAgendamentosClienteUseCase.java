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
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ClienteResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarMeusAgendamentosClienteUseCase {

    private static final List<StatusAgendamentoEnum> STATUS_PADRAO = List.of(
            StatusAgendamentoEnum.AGENDADO, StatusAgendamentoEnum.RESERVADO);

    private final UsuarioRepositoryPort usuarioPort;
    private final ClienteRepositoryPort clientePort;
    private final AgendamentoRepositoryPort agendamentoPort;
    private final AgendaRepositoryPort agendaPort;
    private final ServicoRepositoryPort servicoPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;

    public List<AgendamentoEnriquecido> executar(
            String keycloakSub,
            List<StatusAgendamentoEnum> statuses,
            LocalDate dataInicio,
            LocalDate dataFim) {

        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));

        Cliente cliente = clientePort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Cliente para usuário", usuario.getId()));

        List<StatusAgendamentoEnum> filtroStatus = (statuses == null || statuses.isEmpty()) ? STATUS_PADRAO : statuses;

        List<Agendamento> agendamentos = agendamentoPort.buscarPaisPorClienteId(
                cliente.getId(), filtroStatus, dataInicio, dataFim);

        return agendamentos.stream().map(this::enriquecer).toList();
    }

    private AgendamentoEnriquecido enriquecer(Agendamento a) {
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

        return AgendamentoEnriquecido.builder()
                .id(a.getId())
                .agendaId(a.getAgendaId())
                .dataAgenda(agenda.getDataAgenda())
                .horaInicio(a.getHoraInicio())
                .horaFim(a.getHoraFim())
                .status(a.getStatus())
                .servico(servico)
                .profissional(new ProfissionalResumo(profissional.getId(), profissional.getNome()))
                .cliente(null)
                .estabelecimento(new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome()))
                .presencaConfirmada(a.getPresencaConfirmada())
                .build();
    }

    @Getter
    @Builder
    public static class AgendamentoEnriquecido {
        private final Integer id;
        private final Integer agendaId;
        private final LocalDate dataAgenda;
        private final LocalTime horaInicio;
        private final LocalTime horaFim;
        private final StatusAgendamentoEnum status;
        private final ServicoResumo servico;
        private final ProfissionalResumo profissional;
        private final ClienteResumo cliente;
        private final EstabelecimentoResumo estabelecimento;
        private final Boolean presencaConfirmada;
    }
}
