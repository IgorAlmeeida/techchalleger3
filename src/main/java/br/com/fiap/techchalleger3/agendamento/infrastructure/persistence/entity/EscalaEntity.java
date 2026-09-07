package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity;

import br.com.fiap.techchalleger3.agendamento.domain.model.DiaSemanaEnum;
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
@Table(name = "TB_ESCALAS", schema = "agd")
public class EscalaEntity {

    @Id
    @Column(name = "COD_ESCALA")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "COD_PROFISSIONAL_VINCULO", nullable = false)
    private Integer codProfissionalVinculo;

    @Column(name = "COD_ESTABELECIMENTO", nullable = false)
    private Integer codEstabelecimento;

    @Column(name = "IC_DIA_SEMANA", nullable = false)
    private DiaSemanaEnum diaSemana;

    @Column(name = "HR_HORA_INICIO", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "HR_HORA_FIM", nullable = false)
    private LocalTime horaFim;

    @CreationTimestamp
    @Column(name = "DH_INSERT", nullable = false, updatable = false)
    private LocalDateTime dhInsert;

    @UpdateTimestamp
    @Column(name = "DH_ATUALIZACAO")
    private LocalDateTime dhAtualizacao;
}
