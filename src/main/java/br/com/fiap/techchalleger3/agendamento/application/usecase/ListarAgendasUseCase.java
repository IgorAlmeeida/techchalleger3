package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agenda;
import br.com.fiap.techchalleger3.agendamento.domain.model.AgendaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.AgendaDetalhadaResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalVinculoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarAgendasUseCase {

    private final AgendaRepositoryPort agendaPort;
    private final AgendaItemRepositoryPort agendaItemPort;
    private final ServicoRepositoryPort servicoPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final UsuarioRepositoryPort usuarioPort;

    public Page<AgendaDetalhadaResponse> executar(
            Integer profissionalVinculoId,
            Integer estabelecimentoId,
            LocalDate dataInicio,
            LocalDate dataFim,
            String keycloakSub,
            boolean isAdmin,
            Pageable pageable) {

        if (profissionalVinculoId == null && estabelecimentoId == null) {
            throw new OperacaoInvalidaException("Ao menos um filtro (profissionalVinculoId ou estabelecimentoId) é obrigatório.");
        }

        if (!isAdmin && profissionalVinculoId != null) {
            Integer profissionalId = resolveProfissionalId(keycloakSub);
            ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(profissionalVinculoId)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", profissionalVinculoId));
            if (!vinculo.getProfissionalId().equals(profissionalId)) {
                throw new AcessoNegadoException("Profissional não autorizado a listar agendas deste vínculo.");
            }
        }

        Page<Agenda> agendas = agendaPort.listarPorFiltros(profissionalVinculoId, estabelecimentoId, dataInicio, dataFim, pageable);
        List<AgendaDetalhadaResponse> respostas = agendas.getContent().stream()
                .map(this::toDetalhada)
                .toList();
        return new PageImpl<>(respostas, pageable, agendas.getTotalElements());
    }

    private Integer resolveProfissionalId(String keycloakSub) {
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));
        Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", usuario.getId()));
        return profissional.getId();
    }

    private AgendaDetalhadaResponse toDetalhada(Agenda agenda) {
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(agenda.getProfissionalVinculoId()).orElseThrow();
        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(vinculo.getEstabelecimentoId()).orElseThrow();
        ProfissionalVinculoResumo vinculoResumo = new ProfissionalVinculoResumo(
                vinculo.getId(),
                new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome())
        );

        List<AgendaItem> agendaItens = agendaItemPort.listarPorAgendaId(agenda.getId());
        List<ServicoResumo> servicos = agendaItens.stream()
                .map(ai -> {
                    Servico servico = servicoPort.buscarPorId(ai.getServicoId())
                            .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", ai.getServicoId()));
                    return new ServicoResumo(servico.getId(), servico.getNome(), servico.getDuracaoMinutos());
                })
                .toList();
        return new AgendaDetalhadaResponse(
                agenda.getId(),
                agenda.getDataAgenda(),
                agenda.getHoraInicio(),
                agenda.getHoraFim(),
                vinculoResumo,
                servicos
        );
    }
}
