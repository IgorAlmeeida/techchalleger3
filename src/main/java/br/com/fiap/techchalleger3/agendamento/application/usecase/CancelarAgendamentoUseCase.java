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
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Cliente;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.RoleEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CancelarAgendamentoUseCase {

    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final ClienteRepositoryPort clientePort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final AgendamentoRepositoryPort agendamentoPort;
    private final AgendaRepositoryPort agendaPort;
    private final ServicoRepositoryPort servicoPort;
    private final EmailSenderPort emailSenderPort;

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String ENTIDADE_AGENDAMENTO = "Agendamento";
    private static final String ENTIDADE_AGENDA = "Agenda";
    private static final String TIPO_EMAIL_CANCELAMENTO = "CANCELAMENTO";

    @Transactional
    public Agendamento executar(Integer agendamentoId, String keycloakSub) {
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));

        if (RoleEnum.CLIENTE.equals(usuario.getRole())) {
            return executarComoCliente(agendamentoId, usuario);
        } else if (RoleEnum.PROFISSIONAL.equals(usuario.getRole())) {
            return executarComoProfissional(agendamentoId, usuario);
        } else if (RoleEnum.ADMIN.equals(usuario.getRole())) {
            return executarComoAdmin(agendamentoId);
        }
        throw new AcessoNegadoException("Role não autorizada a cancelar agendamentos.");
    }

    private Agendamento executarComoCliente(Integer agendamentoId, Usuario usuario) {
        Cliente cliente = clientePort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Cliente para usuário", usuario.getId()));

        Agendamento agendamento = agendamentoPort.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_AGENDAMENTO, agendamentoId));

        if (!StatusAgendamentoEnum.AGENDADO.equals(agendamento.getStatus())) {
            throw new OperacaoInvalidaException("Apenas agendamentos com status AGENDADO podem ser cancelados.");
        }
        if (agendamento.getAgendamentoPaiId() != null) {
            throw new OperacaoInvalidaException("Use o ID do agendamento pai para cancelar o grupo.");
        }
        if (!cliente.getId().equals(agendamento.getClienteId())) {
            throw new AcessoNegadoException("Cliente não autorizado a cancelar este agendamento.");
        }

        Agenda agenda = agendaPort.buscarPorId(agendamento.getAgendaId())
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_AGENDA, agendamento.getAgendaId()));

        LocalTime horaFimOriginal = agendamento.getHoraFim();
        Integer servicoIdOriginal = agendamento.getServicoId();
        Integer clienteIdOriginal = agendamento.getClienteId();

        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
        List<Agendamento> filhos = agendamentoPort.buscarFilhosPorPaiId(agendamentoId);

        agendamento.setStatus(StatusAgendamentoEnum.DISPONIVEL);
        agendamento.setClienteId(null);
        agendamento.setServicoId(null);
        agendamento.setHoraFim(agendamento.getHoraInicio().plusMinutes(5));
        agendamento.setPresencaConfirmada(false);
        agendamento.setDhAtualizacao(agora);
        agendamentoPort.salvar(agendamento);

        for (Agendamento filho : filhos) {
            filho.setStatus(StatusAgendamentoEnum.DISPONIVEL);
            filho.setClienteId(null);
            filho.setServicoId(null);
            filho.setAgendamentoPaiId(null);
            filho.setPresencaConfirmada(false);
            filho.setDhAtualizacao(agora);
            agendamentoPort.salvar(filho);
        }

        emailSenderPort.enviar(new EmailMensagem(
                cliente.getEmail(),
                TIPO_EMAIL_CANCELAMENTO,
                dadosCancelamento(agendamentoId, agenda, servicoIdOriginal)
        ));

        return Agendamento.builder()
                .id(agendamento.getId())
                .agendaId(agendamento.getAgendaId())
                .dataAgenda(agenda.getDataAgenda())
                .servicoId(servicoIdOriginal)
                .horaInicio(agendamento.getHoraInicio())
                .horaFim(horaFimOriginal)
                .clienteId(clienteIdOriginal)
                .status(StatusAgendamentoEnum.CANCELADO)
                .presencaConfirmada(false)
                .build();
    }

    private Agendamento executarComoAdmin(Integer agendamentoId) {
        Agendamento agendamento = agendamentoPort.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_AGENDAMENTO, agendamentoId));

        if (!StatusAgendamentoEnum.AGENDADO.equals(agendamento.getStatus())) {
            throw new OperacaoInvalidaException("Apenas agendamentos com status AGENDADO podem ser cancelados.");
        }
        if (agendamento.getAgendamentoPaiId() != null) {
            throw new OperacaoInvalidaException("Use o ID do agendamento pai para cancelar o grupo.");
        }

        Agenda agenda = agendaPort.buscarPorId(agendamento.getAgendaId())
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_AGENDA, agendamento.getAgendaId()));

        Integer clienteId = agendamento.getClienteId();
        Integer servicoId = agendamento.getServicoId();
        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
        List<Agendamento> filhos = agendamentoPort.buscarFilhosPorPaiId(agendamentoId);

        agendamento.setStatus(StatusAgendamentoEnum.CANCELADO);
        agendamento.setDhAtualizacao(agora);
        agendamentoPort.salvar(agendamento);
        agendamento.setDataAgenda(agenda.getDataAgenda());

        for (Agendamento filho : filhos) {
            filho.setStatus(StatusAgendamentoEnum.CANCELADO);
            filho.setDhAtualizacao(agora);
            agendamentoPort.salvar(filho);
        }

        if (clienteId != null) {
            Cliente clienteAfetado = clientePort.buscarPorId(clienteId)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Cliente", clienteId));
            emailSenderPort.enviar(new EmailMensagem(
                    clienteAfetado.getEmail(),
                    TIPO_EMAIL_CANCELAMENTO,
                    dadosCancelamento(agendamentoId, agenda, servicoId)
            ));
        }

        return agendamento;
    }

    private Agendamento executarComoProfissional(Integer agendamentoId, Usuario usuario) {
        Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional para usuário", usuario.getId()));

        Agendamento agendamento = agendamentoPort.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_AGENDAMENTO, agendamentoId));

        if (!StatusAgendamentoEnum.AGENDADO.equals(agendamento.getStatus())) {
            throw new OperacaoInvalidaException("Apenas agendamentos com status AGENDADO podem ser cancelados.");
        }
        if (agendamento.getAgendamentoPaiId() != null) {
            throw new OperacaoInvalidaException("Use o ID do agendamento pai para cancelar o grupo.");
        }

        Agenda agenda = agendaPort.buscarPorId(agendamento.getAgendaId())
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_AGENDA, agendamento.getAgendaId()));

        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", agenda.getProfissionalVinculoId()));

        if (!vinculo.getProfissionalId().equals(profissional.getId())) {
            throw new AcessoNegadoException("Profissional não autorizado a cancelar este agendamento.");
        }

        Integer clienteId = agendamento.getClienteId();
        Integer servicoId = agendamento.getServicoId();
        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
        List<Agendamento> filhos = agendamentoPort.buscarFilhosPorPaiId(agendamentoId);

        agendamento.setStatus(StatusAgendamentoEnum.CANCELADO);
        agendamento.setDhAtualizacao(agora);
        agendamentoPort.salvar(agendamento);
        agendamento.setDataAgenda(agenda.getDataAgenda());

        for (Agendamento filho : filhos) {
            filho.setStatus(StatusAgendamentoEnum.CANCELADO);
            filho.setDhAtualizacao(agora);
            agendamentoPort.salvar(filho);
        }

        if (clienteId != null) {
            Cliente clienteAfetado = clientePort.buscarPorId(clienteId)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Cliente", clienteId));
            emailSenderPort.enviar(new EmailMensagem(
                    clienteAfetado.getEmail(),
                    TIPO_EMAIL_CANCELAMENTO,
                    dadosCancelamento(agendamentoId, agenda, servicoId)
            ));
        }

        return agendamento;
    }

    private Map<String, Object> dadosCancelamento(Integer agendamentoId, Agenda agenda, Integer servicoId) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("agendamentoId", agendamentoId);
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
