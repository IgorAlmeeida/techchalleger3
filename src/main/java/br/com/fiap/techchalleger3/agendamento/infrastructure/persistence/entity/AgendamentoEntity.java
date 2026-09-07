package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity;

import br.com.fiap.techchalleger3.agendamento.domain.model.StatusAgendamentoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_AGENDAMENTOS", schema = "agd")
public class AgendamentoEntity {

    @Id
    @Column(name = "COD_AGENDAMENTO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "COD_AGENDA", nullable = false)
    private Integer codAgenda;

    @Column(name = "COD_AGENDAMENTO_PAI")
    private Integer codAgendamentoPai;

    @Column(name = "COD_SERVICO")
    private Integer codServico;

    @Column(name = "HR_HORA_INICIO", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "HR_HORA_FIM", nullable = false)
    private LocalTime horaFim;

    @Column(name = "COD_CLIENTE")
    private Integer codCliente;

    @Column(name = "IC_STATUS", nullable = false)
    private StatusAgendamentoEnum status;

    @Column(name = "IC_PRESENCA_CONFIRMADA", nullable = false)
    private Boolean presencaConfirmada;

    @CreationTimestamp
    @Column(name = "DH_INSERT", nullable = false, updatable = false)
    private LocalDateTime dhInsert;

    @UpdateTimestamp
    @Column(name = "DH_ATUALIZACAO")
    private LocalDateTime dhAtualizacao;
}
