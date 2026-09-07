package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EscalaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EscalaDetalhadaResponse;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ProfissionalVinculoResumo;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.ServicoResumo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarEscalasUseCase {

    private final EscalaRepositoryPort escalaPort;
    private final EscalaItemRepositoryPort escalaItemPort;
    private final ServicoRepositoryPort servicoPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalRepositoryPort profissionalPort;
    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final UsuarioRepositoryPort usuarioPort;

    public List<EscalaDetalhadaResponse> executar(
            Integer estabelecimentoId,
            Integer profissionalVinculoId,
            String keycloakSub,
            boolean isAdmin) {

        if (estabelecimentoId == null && profissionalVinculoId == null) {
            throw new OperacaoInvalidaException("Ao menos um filtro (estabelecimentoId ou profissionalVinculoId) é obrigatório.");
        }

        if (!isAdmin && profissionalVinculoId != null) {
            Integer profissionalId = resolveProfissionalId(keycloakSub);
            ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(profissionalVinculoId)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", profissionalVinculoId));
            if (!vinculo.getProfissionalId().equals(profissionalId)) {
                throw new AcessoNegadoException("Profissional não autorizado a listar escalas deste vínculo.");
            }
        }

        List<Escala> escalas = escalaPort.listarPorFiltros(estabelecimentoId, profissionalVinculoId);
        return escalas.stream().map(this::toDetalhada).toList();
    }

    private Integer resolveProfissionalId(String keycloakSub) {
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));
        Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", usuario.getId()));
        return profissional.getId();
    }

    private EscalaDetalhadaResponse toDetalhada(Escala escala) {
        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(escala.getProfissionalVinculoId()).orElseThrow();
        Profissional profissional = profissionalPort.buscarPorId(vinculo.getProfissionalId()).orElseThrow();
        Estabelecimento estabelecimento = estabelecimentoPort.buscarPorId(vinculo.getEstabelecimentoId()).orElseThrow();
        ProfissionalVinculoResumo vinculoResumo = new ProfissionalVinculoResumo(
                vinculo.getId(),
                new ProfissionalResumo(profissional.getId(), profissional.getNome()),
                new EstabelecimentoResumo(estabelecimento.getId(), estabelecimento.getNome())
        );

        List<EscalaItem> escalaItens = escalaItemPort.listarAtivosPorEscalaId(escala.getId());
        List<ServicoResumo> servicos = escalaItens.stream()
                .map(ei -> {
                    Servico servico = servicoPort.buscarPorId(ei.getServicoId())
                            .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", ei.getServicoId()));
                    return new ServicoResumo(servico.getId(), servico.getNome(), servico.getDuracaoMinutos());
                })
                .toList();
        return new EscalaDetalhadaResponse(
                escala.getId(),
                vinculoResumo,
                escala.getDiaSemana(),
                escala.getHoraInicio(),
                escala.getHoraFim(),
                servicos
        );
    }
}
