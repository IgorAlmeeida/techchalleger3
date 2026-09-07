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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_SERVICOS", schema = "agd")
public class ServicoEntity {

    @Id
    @Column(name = "COD_SERVICO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "TX_NOME", nullable = false)
    private String nome;

    @Column(name = "NU_DURACAO_MINUTOS", nullable = false)
    private Integer duracaoMinutos;

    @Column(name = "NU_PRECO", precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "IC_ATIVO", nullable = false)
    private Boolean ativo;

    @CreationTimestamp
    @Column(name = "DH_INSERT", nullable = false, updatable = false)
    private LocalDateTime dhInsert;

    @UpdateTimestamp
    @Column(name = "DH_ATUALIZACAO")
    private LocalDateTime dhAtualizacao;
}
