package br.com.rodovia.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Tabela TB_RELATORIO_PRIORIDADE: historico gerado pelo motor de prioridade. */
@Entity
@Table(name = "TB_RELATORIO_PRIORIDADE")
public class RelatorioPrioridade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatorio")
    private Long idRelatorio;

    @Column(name = "qt_urgente", nullable = false)
    private Integer qtUrgente;

    @Column(name = "qt_critico", nullable = false)
    private Integer qtCritico;

    @Column(name = "qt_atencao", nullable = false)
    private Integer qtAtencao;

    @Column(name = "qt_normal", nullable = false)
    private Integer qtNormal;

    @Column(name = "resumo", nullable = false, length = 500)
    private String resumo;

    @Column(name = "data_geracao")
    private LocalDateTime dataGeracao;

    protected RelatorioPrioridade() {
    }

    public RelatorioPrioridade(int qtUrgente, int qtCritico, int qtAtencao, int qtNormal, String resumo) {
        this.qtUrgente = qtUrgente;
        this.qtCritico = qtCritico;
        this.qtAtencao = qtAtencao;
        this.qtNormal = qtNormal;
        this.resumo = resumo;
    }

    @PrePersist
    void definirDataGeracao() {
        if (dataGeracao == null) {
            dataGeracao = LocalDateTime.now();
        }
    }

    public Long getIdRelatorio() { return idRelatorio; }
    public int getQtUrgente() { return qtUrgente; }
    public int getQtCritico() { return qtCritico; }
    public int getQtAtencao() { return qtAtencao; }
    public int getQtNormal() { return qtNormal; }
    public String getResumo() { return resumo; }
    public LocalDateTime getDataGeracao() { return dataGeracao; }
}
