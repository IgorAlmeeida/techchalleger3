package br.com.fiap.techchalleger3.agendamento.application.port;

import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EstabelecimentoRepositoryPort {
    Optional<Estabelecimento> buscarPorId(Integer id);
    boolean existePorCnpj(String cnpj);
    boolean existePorCnpjExcluindoId(String cnpj, Integer id);
    Page<Estabelecimento> listarAtivos(Pageable pageable);
    List<Estabelecimento> listarAtivos();
    List<Estabelecimento> listarPorIds(List<Integer> ids);
    Page<Estabelecimento> buscarComFiltros(String nome, String localizacao, Integer servicoId,
                                           BigDecimal precoMin, BigDecimal precoMax,
                                           Double notaMinima, LocalDate data, Pageable pageable);
    Estabelecimento salvar(Estabelecimento estabelecimento);
}
