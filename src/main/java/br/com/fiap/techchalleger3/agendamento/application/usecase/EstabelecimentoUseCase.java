package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.CachePort;
import br.com.fiap.techchalleger3.agendamento.application.port.EstabelecimentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.ProfissionalVinculoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.OperacaoInvalidaException;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstabelecimentoUseCase {

    private static final String CHAVE_ESTABELECIMENTOS = "agendamento:cache:estabelecimentos:ativos";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final EstabelecimentoRepositoryPort estabelecimentoPort;
    private final ProfissionalVinculoRepositoryPort profissionalVinculoPort;
    private final CachePort cachePort;

    @SuppressWarnings("java:S107")
    public Estabelecimento criar(String nome, String cnpj, String endereco, String telefone,
                                  String responsavelNome, String responsavelCpf, List<String> fotosUrls) {
        if (estabelecimentoPort.existePorCnpj(cnpj)) {
            throw new OperacaoInvalidaException("CNPJ já cadastrado: " + cnpj);
        }
        Estabelecimento estabelecimento = Estabelecimento.builder()
                .nome(nome)
                .cnpj(cnpj)
                .endereco(endereco)
                .telefone(telefone)
                .responsavelNome(responsavelNome)
                .responsavelCpf(responsavelCpf)
                .fotosUrls(fotosUrls)
                .ativo(true)
                .dhInsert(LocalDateTime.now(ZoneId.systemDefault()))
                .build();
        Estabelecimento salvo = estabelecimentoPort.salvar(estabelecimento);
        cachePort.invalidar(CHAVE_ESTABELECIMENTOS);
        return salvo;
    }

    @SuppressWarnings("unchecked")
    public Page<Estabelecimento> listar(Pageable pageable) {
        List<Estabelecimento> todos = cachePort.get(CHAVE_ESTABELECIMENTOS, List.class)
                .map(l -> (List<Estabelecimento>) l)
                .orElseGet(() -> {
                    List<Estabelecimento> doBanco = estabelecimentoPort.listarAtivos();
                    cachePort.put(CHAVE_ESTABELECIMENTOS, doBanco, TTL);
                    return doBanco;
                });
        return paginarEmMemoria(todos, pageable);
    }

    public Estabelecimento buscarPorId(Integer id) {
        return estabelecimentoPort.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Estabelecimento", id));
    }

    @SuppressWarnings("java:S107")
    public Estabelecimento atualizar(Integer id, String nome, String cnpj, String endereco,
                                      String telefone, String responsavelNome, String responsavelCpf,
                                      List<String> fotosUrls) {
        Estabelecimento estabelecimento = buscarPorId(id);
        if (estabelecimentoPort.existePorCnpjExcluindoId(cnpj, id)) {
            throw new OperacaoInvalidaException("CNPJ já cadastrado por outro estabelecimento: " + cnpj);
        }
        estabelecimento.setNome(nome);
        estabelecimento.setCnpj(cnpj);
        estabelecimento.setEndereco(endereco);
        estabelecimento.setTelefone(telefone);
        estabelecimento.setResponsavelNome(responsavelNome);
        estabelecimento.setResponsavelCpf(responsavelCpf);
        estabelecimento.setFotosUrls(fotosUrls);
        estabelecimento.setDhAtualizacao(LocalDateTime.now(ZoneId.systemDefault()));
        Estabelecimento salvo = estabelecimentoPort.salvar(estabelecimento);
        cachePort.invalidar(CHAVE_ESTABELECIMENTOS);
        return salvo;
    }

    @Transactional
    public void inativar(Integer id) {
        Estabelecimento estabelecimento = buscarPorId(id);
        if (profissionalVinculoPort.existeVinculoAtivoPorEstabelecimento(id)) {
            throw new OperacaoInvalidaException(
                    "Estabelecimento possui vínculos ativos. Encerre os vínculos antes de inativar.");
        }
        estabelecimento.setAtivo(false);
        estabelecimento.setDhAtualizacao(LocalDateTime.now(ZoneId.systemDefault()));
        estabelecimentoPort.salvar(estabelecimento);
        cachePort.invalidar(CHAVE_ESTABELECIMENTOS);
    }

    private static <T> Page<T> paginarEmMemoria(List<T> lista, Pageable pageable) {
        int inicio = (int) pageable.getOffset();
        int fim = Math.min(inicio + pageable.getPageSize(), lista.size());
        List<T> pagina = inicio >= lista.size() ? List.of() : lista.subList(inicio, fim);
        return new PageImpl<>(pagina, pageable, lista.size());
    }
}
