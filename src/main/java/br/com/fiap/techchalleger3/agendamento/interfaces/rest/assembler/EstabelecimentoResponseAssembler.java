package br.com.fiap.techchalleger3.agendamento.interfaces.rest.assembler;

import br.com.fiap.techchalleger3.agendamento.domain.model.Estabelecimento;
import br.com.fiap.techchalleger3.agendamento.interfaces.rest.dto.EstabelecimentoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstabelecimentoResponseAssembler {

    public EstabelecimentoResponse toResponse(Estabelecimento e) {
        return new EstabelecimentoResponse(
                e.getId(), e.getNome(), e.getCnpj(), e.getEndereco(), e.getTelefone(),
                e.getResponsavelNome(), e.getResponsavelCpf(), e.getFotosUrls(),
                e.getAtivo(), e.getDhInsert(), e.getDhAtualizacao());
    }
}
