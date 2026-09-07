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

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_AVALIACOES", schema = "agd")
public class AvaliacaoEntity {

    @Id
    @Column(name = "COD_AVALIACAO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codAvaliacao;

    @Column(name = "COD_AGENDAMENTO", nullable = false)
    private Integer codAgendamento;

    @Column(name = "COD_CLIENTE", nullable = false)
    private Integer codCliente;

    @Column(name = "COD_ESTABELECIMENTO", nullable = false)
    private Integer codEstabelecimento;

    @Column(name = "COD_PROFISSIONAL_VINCULO", nullable = false)
    private Integer codProfissionalVinculo;

    @Column(name = "NU_NOTA", nullable = false)
    private Integer nota;

    @Column(name = "TX_COMENTARIO", columnDefinition = "TEXT")
    private String comentario;

    @CreationTimestamp
    @Column(name = "DH_INSERT", nullable = false, updatable = false)
    private LocalDateTime dhInsert;
}
