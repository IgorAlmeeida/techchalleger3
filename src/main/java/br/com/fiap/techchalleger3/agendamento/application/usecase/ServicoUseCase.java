package br.com.fiap.techchalleger3.agendamento.application.usecase;

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
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agrupa as operações de CRUD sobre serviços: criação, listagem, busca, atualização e
 * inativação. A duração em minutos deve ser múltiplo de 5 (granularidade dos slots).
 */
@Service
@RequiredArgsConstructor
public class ServicoUseCase {

    private final ServicoRepositoryPort servicoPort;

    public Servico criar(String nome, Integer duracaoMinutos, BigDecimal preco) {
        validarMultiploDe5(duracaoMinutos);
        Servico servico = Servico.builder()
                .nome(nome)
                .duracaoMinutos(duracaoMinutos)
                .preco(preco)
                .ativo(true)
                .dhInsert(LocalDateTime.now(ZoneId.systemDefault()))
                .build();
        return servicoPort.salvar(servico);
    }

    public Page<Servico> listar(Pageable pageable) {
        List<Servico> todos = servicoPort.listarAtivos();
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
        servico.setDhAtualizacao(LocalDateTime.now(ZoneId.systemDefault()));
        return servicoPort.salvar(servico);
    }

    public void deletar(Integer id) {
        buscarPorId(id);
        servicoPort.deletar(id);
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
