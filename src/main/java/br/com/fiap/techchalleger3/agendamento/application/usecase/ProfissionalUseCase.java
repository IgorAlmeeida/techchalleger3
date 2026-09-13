package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendaRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.UsuarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.AcessoNegadoException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Profissional;
import br.com.fiap.techchalleger3.agendamento.domain.model.ProfissionalVinculo;
import br.com.fiap.techchalleger3.agendamento.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agrupa as operações de CRUD sobre profissionais: listagem, busca, atualização e inativação.
 * Profissionais só podem acessar e alterar seus próprios dados; admins têm acesso irrestrito.
 */
@Service
@RequiredArgsConstructor
public class ProfissionalUseCase {

    private final ProfissionalRepositoryPort profissionalPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final AgendaRepositoryPort agendaPort;

    private static final String ENTIDADE_PROFISSIONAL = "Profissional";

    public Page<Profissional> listar(String nome, String especialidade, boolean incluirInativos, Pageable pageable) {
        return profissionalPort.listarComFiltros(nome, especialidade, incluirInativos, pageable);
    }

    public Profissional buscarPorId(Integer id, String userSub, boolean isAdmin) {
        Profissional profissional = profissionalPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_PROFISSIONAL, id));
        if (!isAdmin) {
            validarProprioAcesso(profissional, userSub);
        }
        return profissional;
    }

    @Transactional
    public Profissional atualizar(Integer id, String nome, List<String> especialidades,
                                   String endereco, String userSub, boolean isAdmin) {
        Profissional profissional = profissionalPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_PROFISSIONAL, id));
        if (!isAdmin) {
            validarProprioAcesso(profissional, userSub);
        }
        profissional.setNome(nome);
        profissional.setEspecialidades(especialidades);
        profissional.setEndereco(endereco);
        profissional.setDhAtualizacao(LocalDateTime.now(ZoneId.systemDefault()));
        return profissionalPort.salvar(profissional);
    }

    @Transactional
    public void deletar(Integer id) {
        Profissional profissional = profissionalPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(ENTIDADE_PROFISSIONAL, id));

        List<ProfissionalVinculo> vinculos = profissionalVinculoPort.listarPorProfissionalId(id);
        boolean temAgendaFutura = vinculos.stream()
                .anyMatch(v -> agendaPort.existeAgendaFuturaPorVinculo(v.getId()));
        if (temAgendaFutura) {
            throw new OperacaoInvalidaException(
                    "Profissional possui agendas futuras. Cancele as agendas antes de excluir.");
        }

        profissionalPort.deletar(id);
        usuarioPort.deletar(profissional.getUsuarioId());
    }

    private void validarProprioAcesso(Profissional profissional, String userSub) {
        Usuario usuario = usuarioPort.buscarPorUuid(userSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", userSub));
        if (!profissional.getUsuarioId().equals(usuario.getId())) {
            throw new AcessoNegadoException("Profissional não autorizado a acessar este recurso.");
        }
    }
}
