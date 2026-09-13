package br.com.fiap.techchalleger3.agendamento.infrastructure.persistence.entity;

import br.com.fiap.techchalleger3.agendamento.domain.model.ProvedorCalendarioEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TB_INTEGRACOES_CALENDARIO", schema = "agd")
public class IntegracaoCalendarioEntity {

    @Id
    @Column(name = "COD_INTEGRACAO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "COD_CLIENTE")
    private Integer codCliente;

    @Column(name = "COD_PROFISSIONAL")
    private Integer codProfissional;

    @Enumerated(EnumType.STRING)
    @Column(name = "IC_PROVEDOR", nullable = false)
    private ProvedorCalendarioEnum provedor;

    @Column(name = "TX_ACCESS_TOKEN", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "TX_REFRESH_TOKEN", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "DH_EXPIRA_EM")
    private Instant expiraEm;

    @Column(name = "IC_ATIVO", nullable = false)
    private Boolean ativo;

    @CreationTimestamp
    @Column(name = "DH_INSERT", nullable = false, updatable = false)
    private LocalDateTime dhInsert;
}
