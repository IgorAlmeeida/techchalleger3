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
public class AtualizarEscalaUseCase {

    private final EscalaRepositoryPort escalaPort;
    private final EscalaItemRepositoryPort escalaItemPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final ProfissionalVinculoServicoRepositoryPort vinculoServicoPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalRepositoryPort profissionalPort;

    @Transactional
    public Escala executar(
            Integer escalaId,
            DiaSemanaEnum diaSemana,
            LocalTime horaInicio,
            LocalTime horaFim,
            List<Integer> servicoIds,
            String keycloakSub,
            boolean isAdmin) {

        Escala escala = escalaPort.buscarPorId(escalaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Escala", escalaId));

        ProfissionalVinculo vinculo = profissionalVinculoPort.buscarPorId(escala.getProfissionalVinculoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("ProfissionalVinculo", escala.getProfissionalVinculoId()));

        if (!isAdmin) {
            Integer profissionalId = resolveProfissionalId(keycloakSub);
            if (!vinculo.getProfissionalId().equals(profissionalId)) {
                throw new AcessoNegadoException("Profissional não autorizado a atualizar esta escala.");
            }
        }

        Set<Integer> servicosPermitidos = vinculoServicoPort.listarPorProfissionalVinculoId(escala.getProfissionalVinculoId())
                .stream().map(ProfissionalVinculoServico::getServicoId).collect(Collectors.toSet());

        for (Integer servicoId : servicoIds) {
            if (!servicosPermitidos.contains(servicoId)) {
                throw new ItemNaoPermitidoException(servicoId, escala.getProfissionalVinculoId());
            }
        }

        List<Escala> escalasExistentes = profissionalVinculoPort.listarPorProfissionalId(vinculo.getProfissionalId()).stream()
                .flatMap(v -> escalaPort.listarPorProfissionalVinculoId(v.getId()).stream())
                .filter(e -> !e.getId().equals(escalaId))
                .toList();

        escala.setDiaSemana(diaSemana);
        escala.setHoraInicio(horaInicio);
        escala.setHoraFim(horaFim);

        new ValidadorConflitoEscala().validar(escala, escalasExistentes);

        Escala salva = escalaPort.salvar(escala);

        escalaItemPort.deletarPorEscalaId(escalaId);
        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
        for (Integer servicoId : servicoIds) {
            escalaItemPort.salvar(EscalaItem.builder()
                    .escalaId(escalaId)
                    .servicoId(servicoId)
                    .ativa(true)
                    .dhInsert(agora)
                    .build());
        }

        return salva;
    }

    private Integer resolveProfissionalId(String keycloakSub) {
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));
        Profissional profissional = profissionalPort.buscarPorUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", usuario.getId()));
        return profissional.getId();
    }
}
