package br.com.fiap.techchalleger3.agendamento.application.usecase;

import br.com.fiap.techchalleger3.agendamento.application.port.AgendamentoRepositoryPort;
import br.com.fiap.techchalleger3.agendamento.application.port.CalendarioExportPort;
import br.com.fiap.techchalleger3.agendamento.domain.exception.RegistroNaoEncontradoException;
import br.com.fiap.techchalleger3.agendamento.domain.model.Agendamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportarAgendamentoIcsUseCaseTest {

    @Mock private AgendamentoRepositoryPort agendamentoPort;
    @Mock private CalendarioExportPort calendarioExportPort;

    @InjectMocks private ExportarAgendamentoIcsUseCase useCase;

    @Test
    void deveLancarExcecao_quandoAgendamentoNaoEncontrado() {
        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.exportar(1))
                .isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    void deveRetornarIcs_quandoAgendamentoEncontrado() {
        Agendamento agendamento = Agendamento.builder().id(1).build();
        byte[] icsBytes = "BEGIN:VCALENDAR".getBytes(StandardCharsets.UTF_8);

        when(agendamentoPort.buscarPorId(1)).thenReturn(Optional.of(agendamento));
        when(calendarioExportPort.exportarIcs(agendamento)).thenReturn(icsBytes);

        byte[] resultado = useCase.exportar(1);

        assertThat(resultado).isEqualTo(icsBytes);
        verify(calendarioExportPort).exportarIcs(agendamento);
    }
}
