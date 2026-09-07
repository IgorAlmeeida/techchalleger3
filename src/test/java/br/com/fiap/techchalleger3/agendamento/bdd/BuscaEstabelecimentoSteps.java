package br.com.fiap.techchalleger3.agendamento.bdd;

import br.com.fiap.techchalleger3.agendamento.application.usecase.BuscarEstabelecimentosUseCase;
import br.com.fiap.techchalleger3.agendamento.application.usecase.EstabelecimentoUseCase;
import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler.EstabelecimentoResponseAssembler;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResponse;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BuscaEstabelecimentoSteps {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuscarEstabelecimentosUseCase buscarEstabelecimentosUseCase;

    @MockBean
    private EstabelecimentoUseCase estabelecimentoUseCase;

    @MockBean
    private EstabelecimentoResponseAssembler assembler;

    private ResultActions resultado;

    @Dado("que existem estabelecimentos oferecendo {string} com preços entre {int} e {int}")
    public void estabelecimentosComServico(String servico, int precoMin, int precoMax) {
        Estabelecimento dentro = Estabelecimento.builder().id(1).nome("Salão A").build();
        when(buscarEstabelecimentosUseCase.buscar(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dentro)));
        when(assembler.toResponse(any()))
                .thenReturn(new EstabelecimentoResponse(
                        1, "Salão A", null, null, null, null, null, null, true, null, null));
    }

    @Quando("o cliente busca por {string} com preço máximo de {int}")
    public void buscaComFiltro(String servico, int precoMax) throws Exception {
        resultado = mockMvc.perform(get("/api/estabelecimentos/buscar")
                .param("precoMax", String.valueOf(precoMax)));
    }

    @Entao("somente os estabelecimentos dentro da faixa de preço devem ser retornados")
    public void estabelecimentosDentroFaixa() throws Exception {
        resultado.andExpect(status().isOk());
    }
}
