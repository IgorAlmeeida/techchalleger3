package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.EscalaItemRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.EscalaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.ItemNaoPermitidoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
import br.com.fiap.techchalleger3.agendamento.domain.model.Escala;
import br.com.fiap.techchalleger3.agendamento.domain.model.EscalaItem;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculoServico;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import br.com.fiap.techchalleger3.agendamento.domain.service.ValidadorConflitoEscala;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CriarEscalaUseCase {

    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    private final EscalaRepositoryPort escalaPort;
    private final EscalaItemRepositoryPort escalaItemPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;

    @Transactional
    public Escala executar(Integer profissionalVinculoId, DiaSemanaEnum diaSemana,
                           LocalTime horaInicio, LocalTime horaFim, List<Integer> servicoIds,
                           String keycloakSub, boolean isAdmin) {

        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(profissionalVinculoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", profissionalVinculoId));

        if (!vinculo.isAtivo()) {
            throw new RegistroNaoEncontradoException("ProfissionalVinculo ativo", profissionalVinculoId);
        }

        if (!isAdmin) {
            Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));
            Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional para usuário", usuario.getId()));
            if (!vinculo.getProfissionalId().equals(profissional.getId())) {
                throw new AcessoNegadoException("Profissional não autorizado a criar escala para este vínculo.");
            }
        }

        Set<Integer> servicosPermitidos = vinculoServicoPort.listarPorProfissionalVinculoId(profissionalVinculoId)
                .stream()
                .map(ProfissionalVinculoServico::getServicoId)
                .collect(Collectors.toSet());

        for (Integer servicoId : servicoIds) {
            if (!servicosPermitidos.contains(servicoId)) {
                throw new ItemNaoPermitidoException(servicoId, profissionalVinculoId);
            }
        }

        List<ProfissionalVinculo> todosVinculos = profissionalVinculoPort.listarPorProfissionalId(vinculo.getProfissionalId());
        List<Integer> todosVinculoIds = todosVinculos.stream()
                .map(ProfissionalVinculo::getId)
                .toList();

        List<Escala> escalasExistentes = todosVinculoIds.stream()
                .flatMap(vid -> escalaPort.listarPorProfissionalVinculoId(vid).stream())
                .toList();

        Escala nova = Escala.builder()
                .profissionalVinculoId(profissionalVinculoId)
                .estabelecimentoId(vinculo.getEstabelecimentoId())
                .diaSemana(diaSemana)
                .horaInicio(horaInicio)
                .horaFim(horaFim)
                .dhInsert(LocalDateTime.now(ZoneId.systemDefault()))
                .build();

        new ValidadorConflitoEscala().validar(nova, escalasExistentes);

        Escala salva = escalaPort.salvar(nova);

        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
        for (Integer servicoId : servicoIds) {
            escalaItemPort.salvar(EscalaItem.builder()
                    .escalaId(salva.getId())
                    .servicoId(servicoId)
                    .ativa(true)
                    .dhInsert(agora)
                    .build());
        }

        return salva;
    }
}
