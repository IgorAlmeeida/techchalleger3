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
@Table(name = "TB_ESTABELECIMENTOS", schema = "agd")
public class EstabelecimentoEntity {

    @Id
    @Column(name = "COD_ESTABELECIMENTO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "TX_NOME", nullable = false)
    private String nome;

    @Column(name = "TX_CNPJ", nullable = false, unique = true)
    private String cnpj;

    @Column(name = "TX_ENDERECO")
    private String endereco;

    @Column(name = "TX_TELEFONE")
    private String telefone;

    @Column(name = "TX_RESPONSAVEL_NOME")
    private String responsavelNome;

    @Column(name = "TX_RESPONSAVEL_CPF")
    private String responsavelCpf;

    @Column(name = "TX_FOTOS_URLS", columnDefinition = "TEXT")
    private String fotosUrls;

    @Column(name = "IC_ATIVO", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "DH_INSERT", nullable = false, updatable = false)
    private LocalDateTime dhInsert;

    @UpdateTimestamp
    @Column(name = "DH_ATUALIZACAO")
    private LocalDateTime dhAtualizacao;
}
