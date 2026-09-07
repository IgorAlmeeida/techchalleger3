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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfissionalUseCase {

    private final ProfissionalRepositoryPort profissionalPort;
    private final UsuarioRepositoryPort usuarioPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final AgendaRepositoryPort agendaPort;

    public Page<Profissional> listar(String nome, String especialidade, boolean incluirInativos, Pageable pageable) {
        return profissionalPort.listarComFiltros(nome, especialidade, incluirInativos, pageable);
    }

    public Profissional buscarPorId(Integer id, String keycloakSub, boolean isAdmin) {
        Profissional profissional = profissionalPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", id));
        if (!isAdmin) {
            validarProprioAcesso(profissional, keycloakSub);
        }
        return profissional;
    }

    @Transactional
    public Profissional atualizar(Integer id, String nome, List<String> especialidades,
                                   String endereco, String keycloakSub, boolean isAdmin) {
        Profissional profissional = profissionalPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", id));
        if (!isAdmin) {
            validarProprioAcesso(profissional, keycloakSub);
        }
        profissional.setNome(nome);
        profissional.setEspecialidades(especialidades);
        profissional.setEndereco(endereco);
        profissional.setDhAtualizacao(LocalDateTime.now());
        return profissionalPort.salvar(profissional);
    }

    @Transactional
    public void inativar(Integer id) {
        Profissional profissional = profissionalPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Profissional", id));

        List<ProfissionalVinculo> vinculos = profissionalVinculoPort.listarPorProfissionalId(id);
        boolean temAgendaFutura = vinculos.stream()
                .anyMatch(v -> agendaPort.existeAgendaFuturaPorVinculo(v.getId()));
        if (temAgendaFutura) {
            throw new OperacaoInvalidaException(
                    "Profissional possui agendas futuras. Cancele as agendas antes de inativar.");
        }

        profissional.setAtivo(false);
        profissional.setDhAtualizacao(LocalDateTime.now());
        profissionalPort.salvar(profissional);
    }

    private void validarProprioAcesso(Profissional profissional, String keycloakSub) {
        Usuario usuario = usuarioPort.buscarPorCodKeycloak(keycloakSub)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuario", keycloakSub));
        if (!profissional.getUsuarioId().equals(usuario.getId())) {
            throw new AcessoNegadoException("Profissional não autorizado a acessar este recurso.");
        }
    }
}
