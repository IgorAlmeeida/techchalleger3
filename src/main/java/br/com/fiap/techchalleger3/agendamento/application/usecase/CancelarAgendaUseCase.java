package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ClienteRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailMensagem;
import br.com.fiap.techchalleger3.agendamento.application.port.EmailSenderPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CancelarAgendaUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final AgendaRepositoryPort agendaPort;
    private final AgendamentoRepositoryPort agendamentoPort;
    private final ClienteRepositoryPort clientePort;
    private final ServicoRepositoryPort servicoPort;
    private final EmailSenderPort emailSenderPort;

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public void executar(Integer agendaId, String keycloakSub, boolean isAdmin) {
        Agenda agenda = agendaPort.buscarPorId(agendaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Agenda", agendaId));

        if (!isAdmin) {
            Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));

            Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional para usuário", usuario.getId()));

            ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", agenda.getProfissionalVinculoId()));

            if (!vinculo.getProfissionalId().equals(profissional.getId())) {
                throw new AcessoNegadoException("Profissional não autorizado a cancelar esta agenda.");
            }
        }

        List<Agendamento> todos = agendamentoPort.listarPorAgendaId(agendaId);
        LocalDateTime agora = LocalDateTime.now();

        for (Agendamento agendamento : todos) {
            if (agendamento.getAgendamentoPaiId() != null) continue;

            if (StatusAgendamentoEnum.AGENDADO.equals(agendamento.getStatus())) {
                Integer clienteId = agendamento.getClienteId();
                Integer servicoId = agendamento.getServicoId();

                agendamento.setStatus(StatusAgendamentoEnum.CANCELADO);
                agendamento.setDhAtualizacao(agora);
                agendamentoPort.salvar(agendamento);

                for (Agendamento filho : agendamentoPort.buscarFilhosPorPaiId(agendamento.getId())) {
                    filho.setStatus(StatusAgendamentoEnum.CANCELADO);
                    filho.setDhAtualizacao(agora);
                    agendamentoPort.salvar(filho);
                }

                if (clienteId != null) {
                    clientePort.buscarPorId(clienteId).ifPresent(c ->
                            emailSenderPort.enviar(new EmailMensagem(
                                    c.getEmail(),
                                    "CANCELAMENTO_AGENDA",
                                    dadosCancelamentoAgenda(agendaId, agenda, servicoId)
                            )));
                }
            } else if (StatusAgendamentoEnum.DISPONIVEL.equals(agendamento.getStatus())
                    || StatusAgendamentoEnum.RESERVADO.equals(agendamento.getStatus())) {
                agendamento.setStatus(StatusAgendamentoEnum.CANCELADO);
                agendamento.setDhAtualizacao(agora);
                agendamentoPort.salvar(agendamento);
            }
        }
    }

    private Map<String, Object> dadosCancelamentoAgenda(Integer agendaId, Agenda agenda, Integer servicoId) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("agendaId", agendaId);
        dados.put("data", agenda.getDataAgenda().format(DATA_FMT));
        if (servicoId != null) {
            servicoPort.buscarPorId(servicoId).ifPresent(s -> dados.put("servicoNome", s.getNome()));
        }
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId()).orElse(null);
        if (vinculo != null) {
            profissionalPort.buscarPorId(vinculo.getProfissionalId()).ifPresent(p -> dados.put("profissionalNome", p.getNome()));
        }
        return dados;
    }
}
