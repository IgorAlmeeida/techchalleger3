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

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_AGENDA_ITENS", schema = "agd")
public class AgendaItemEntity {

    @Id
    @Column(name = "COD_AGENDA_ITEM")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "COD_AGENDA", nullable = false)
    private Integer codAgenda;

    @Column(name = "COD_SERVICO", nullable = false)
    private Integer codServico;

    @CreationTimestamp
    @Column(name = "DH_INSERT", nullable = false, updatable = false)
    private LocalDateTime dhInsert;

    @UpdateTimestamp
    @Column(name = "DH_ATUALIZACAO")
    private LocalDateTime dhAtualizacao;
}
