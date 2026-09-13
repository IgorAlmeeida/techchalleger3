package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.repository;

import br.com.fiap.techchalleger3.agendamento.application.port.IntegracaoCalendarioRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.IntegracaoCalendarioExterno;
import br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity.IntegracaoCalendarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IntegracaoCalendarioRepositoryPortImpl implements IntegracaoCalendarioRepositoryPort {

    private final IntegracaoCalendarioRepository repository;

    @Override
    public IntegracaoCalendarioExterno salvar(IntegracaoCalendarioExterno integracao) {
        IntegracaoCalendarioEntity entity = toEntity(integracao);
        IntegracaoCalendarioEntity salvo = repository.save(entity);
        return toDomain(salvo);
    }

    @Override
    public Optional<IntegracaoCalendarioExterno> buscarAtivaByClienteId(Integer clienteId) {
        return repository.findByCodClienteAndAtivoTrue(clienteId).map(this::toDomain);
    }

    @Override
    public Optional<IntegracaoCalendarioExterno> buscarAtivaByProfissionalId(Integer profissionalId) {
        return repository.findByCodProfissionalAndAtivoTrue(profissionalId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void desativarPorClienteId(Integer clienteId) {
        repository.desativarPorCodCliente(clienteId);
    }

    @Override
    @Transactional
    public void desativarPorProfissionalId(Integer profissionalId) {
        repository.desativarPorCodProfissional(profissionalId);
    }

    private IntegracaoCalendarioEntity toEntity(IntegracaoCalendarioExterno d) {
        return IntegracaoCalendarioEntity.builder()
                .codigo(d.getId())
                .codCliente(d.getClienteId())
                .codProfissional(d.getProfissionalId())
                .provedor(d.getProvedor())
                .accessToken(d.getAccessToken())
                .refreshToken(d.getRefreshToken())
                .expiraEm(d.getExpiraEm())
                .ativo(d.getAtivo())
                .build();
    }

    private IntegracaoCalendarioExterno toDomain(IntegracaoCalendarioEntity e) {
        return IntegracaoCalendarioExterno.builder()
                .id(e.getCodigo())
                .clienteId(e.getCodCliente())
                .profissionalId(e.getCodProfissional())
                .provedor(e.getProvedor())
                .accessToken(e.getAccessToken())
                .refreshToken(e.getRefreshToken())
                .expiraEm(e.getExpiraEm())
                .ativo(e.getAtivo())
                .build();
    }
}
