package br.com.rodovia.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_INTERVENCAO_OPERACIONAL")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_intervencao", discriminatorType = DiscriminatorType.STRING, length = 50)
public abstract class IntervencaoOperacional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intervencao")
    private Long idIntervencao;

    /** Espelho somente-leitura do discriminador: permite derived queries como findByTipoIntervencao. */
    @Column(name = "tipo_intervencao", insertable = false, updatable = false)
    private String tipoIntervencao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_equipe", nullable = false)
    private EquipeManutencao equipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_trecho", nullable = false)
    private TrechoRodovia trecho;

    @Column(name = "data_registro")
    private LocalDateTime dataRegistro;

    /** Exigido pelo JPA. */
    protected IntervencaoOperacional() {
    }

    protected IntervencaoOperacional(EquipeManutencao equipe, TrechoRodovia trecho) {
        this.equipe = equipe;
        this.trecho = trecho;
    }

    /** Cada tipo de intervencao altera o trecho do seu jeito (polimorfismo). */
    public abstract void executarServico(TrechoRodovia trecho);

    public abstract TipoIntervencao getTipo();

    /** Fabrica: devolve a subclasse concreta correspondente ao tipo. */
    public static IntervencaoOperacional criar(TipoIntervencao tipo, EquipeManutencao equipe, TrechoRodovia trecho) {
        return switch (tipo) {
            case ROCADA_MECANIZADA -> new RocadaMecanizada(equipe, trecho);
            case PULVERIZACAO -> new Pulverizacao(equipe, trecho);
        };
    }

    @PrePersist
    void definirDataRegistro() {
        if (dataRegistro == null) {
            dataRegistro = LocalDateTime.now();
        }
    }

    public Long getIdIntervencao() { return idIntervencao; }
    public EquipeManutencao getEquipe() { return equipe; }
    public TrechoRodovia getTrecho() { return trecho; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }

    public void setEquipe(EquipeManutencao equipe) { this.equipe = equipe; }
    public void setTrecho(TrechoRodovia trecho) { this.trecho = trecho; }
}
