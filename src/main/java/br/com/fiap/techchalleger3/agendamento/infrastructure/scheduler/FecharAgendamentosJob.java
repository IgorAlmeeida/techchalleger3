package br.com.fiap.techchalleger3.agendamento.infrastructure.scheduler;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FecharAgendamentosJob {

    private final AgendamentoRepositoryPort agendamentoPort;

    @Scheduled(fixedDelayString = "PT15M")
    @Transactional
    public void fecharAgendamentosPassados() {
        List<Agendamento> passados = agendamentoPort.buscarAgendadosPassados();
        log.info("FecharAgendamentosJob: {} agendamento(s) para fechar", passados.size());

        for (Agendamento agendamento : passados) {
            if (agendamento.getAgendamentoPaiId() != null) continue;

            StatusAgendamentoEnum novoStatus = Boolean.TRUE.equals(agendamento.getPresencaConfirmada())
                    ? StatusAgendamentoEnum.REALIZADO
                    : StatusAgendamentoEnum.NAO_REALIZADO;
            LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());

            agendamento.setStatus(novoStatus);
            agendamento.setDhAtualizacao(agora);
            agendamentoPort.salvar(agendamento);

            for (Agendamento filho : agendamentoPort.buscarFilhosPorPaiId(agendamento.getId())) {
                filho.setStatus(novoStatus);
                filho.setDhAtualizacao(agora);
                agendamentoPort.salvar(filho);
            }
        }
    }
}
