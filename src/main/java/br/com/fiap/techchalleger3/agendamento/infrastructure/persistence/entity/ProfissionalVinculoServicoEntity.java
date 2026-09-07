package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity;

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

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_PROFISSIONAL_VINCULO_SERVICOS", schema = "agd")
public class ProfissionalVinculoServicoEntity {

    @Id
    @Column(name = "COD_PROFISSIONAL_VINCULO_SERVICO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "COD_PROFISSIONAL_VINCULO", nullable = false)
    private Integer codProfissionalVinculo;

    @Column(name = "COD_SERVICO", nullable = false)
    private Integer codServico;

    @Column(name = "NU_TARIFA", precision = 10, scale = 2)
    private BigDecimal tarifa;
}
