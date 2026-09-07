package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import br.com.fiap.techchalleger3.agendamento.application.port.ServicoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Servico;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoUseCase {

    private static final String CHAVE_SERVICOS = "agendamento:cache:servicos:ativos";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ServicoRepositoryPort servicoPort;
    private final CachePort cachePort;

    public Servico criar(String nome, Integer duracaoMinutos, BigDecimal preco) {
        validarMultiploDe5(duracaoMinutos);
        Servico servico = Servico.builder()
                .nome(nome)
                .duracaoMinutos(duracaoMinutos)
                .preco(preco)
                .ativo(true)
                .dhInsert(LocalDateTime.now())
                .build();
        Servico salvo = servicoPort.salvar(servico);
        cachePort.invalidar(CHAVE_SERVICOS);
        return salvo;
    }

    @SuppressWarnings("unchecked")
    public Page<Servico> listar(Pageable pageable) {
        List<Servico> todos = cachePort.get(CHAVE_SERVICOS, List.class)
                .map(l -> (List<Servico>) l)
                .orElseGet(() -> {
                    List<Servico> doBanco = servicoPort.listarAtivos();
                    cachePort.put(CHAVE_SERVICOS, doBanco, TTL);
                    return doBanco;
                });
        return paginarEmMemoria(todos, pageable);
    }

    public Servico buscarPorId(Integer id) {
        return servicoPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Servico", id));
    }

    public Servico atualizar(Integer id, String nome, Integer duracaoMinutos, BigDecimal preco) {
        validarMultiploDe5(duracaoMinutos);
        Servico servico = buscarPorId(id);
        servico.setNome(nome);
        servico.setDuracaoMinutos(duracaoMinutos);
        servico.setPreco(preco);
        servico.setDhAtualizacao(LocalDateTime.now());
        Servico salvo = servicoPort.salvar(servico);
        cachePort.invalidar(CHAVE_SERVICOS);
        return salvo;
    }

    public void inativar(Integer id) {
        Servico servico = buscarPorId(id);
        servico.setAtivo(false);
        servico.setDhAtualizacao(LocalDateTime.now());
        servicoPort.salvar(servico);
        cachePort.invalidar(CHAVE_SERVICOS);
    }

    private void validarMultiploDe5(Integer duracaoMinutos) {
        if (duracaoMinutos == null || duracaoMinutos % 5 != 0) {
            throw new OperacaoInvalidaException("duracao_minutos deve ser múltiplo de 5.");
        }
    }

    private static <T> Page<T> paginarEmMemoria(List<T> lista, Pageable pageable) {
        int inicio = (int) pageable.getOffset();
        int fim = Math.min(inicio + pageable.getPageSize(), lista.size());
        List<T> pagina = inicio >= lista.size() ? List.of() : lista.subList(inicio, fim);
        return new PageImpl<>(pagina, pageable, lista.size());
    }
}
