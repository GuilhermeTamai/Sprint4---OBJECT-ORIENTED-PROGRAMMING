package br.com.rodovia.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;


@Entity
@Table(name = "TB_TRECHO_RODOVIA")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "is_iot", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
public class TrechoRodovia {

    public static final double LIMITE_CRITICO = 30.0;

    @Id
    @Column(name = "identificador", length = 50)
    protected String identificador;

    @Column(name = "km_inicial", nullable = false)
    protected double quilometroInicial;

    @Column(name = "km_final", nullable = false)
    protected double quilometroFinal;

    @Column(name = "nivel_vegetacao", nullable = false)
    protected double nivelVegetacao;

    @Convert(converter = BooleanNumericConverter.class)
    @Column(name = "is_umido", nullable = false)
    protected Boolean isTrechoUmido;

    /** Exigido pelo JPA. */
    protected TrechoRodovia() {
    }

    public TrechoRodovia(String identificador, double kmInicial, double kmFinal, double nivelVegetacaoInicial, boolean isTrechoUmido) {
        this.identificador = identificador;
        setQuilometros(kmInicial, kmFinal);
        this.nivelVegetacao = Math.max(0, nivelVegetacaoInicial);
        this.isTrechoUmido = isTrechoUmido;
    }

    public void setQuilometros(double inicial, double fim) {
        if (inicial < 0 || fim < 0) {
            throw new IllegalArgumentException("[ERRO] Quilometragem não pode ser negativa (Informado: " + inicial + " a " + fim + ")");
        }
        if (fim <= inicial) {
            throw new IllegalArgumentException("[ERRO] O KM final deve ser maior que o inicial (Informado: " + inicial + " a " + fim + ")");
        }
        this.quilometroInicial = inicial;
        this.quilometroFinal = fim;
    }

    // Motor de crescimento inteligente (Sprint 2)
    public void simularCrescimento(int dias) {
        // Trechos úmidos crescem mais rápido (ex: 1.5cm/dia). Secos crescem menos (0.5cm/dia)
        double taxaDiaria = isTrechoUmido() ? 1.5 : 0.5;
        this.nivelVegetacao += (taxaDiaria * dias);
    }

    public boolean estaCritico() {
        return this.nivelVegetacao >= LIMITE_CRITICO;
    }

    /** Atualiza os dados editáveis (a validação das regras de negócio fica no Service). */
    public void atualizarDados(double kmInicial, double kmFinal, double nivelVegetacao, boolean isTrechoUmido) {
        setQuilometros(kmInicial, kmFinal);
        setNivelVegetacao(nivelVegetacao);
        this.isTrechoUmido = isTrechoUmido;
    }

    public String getIdentificador() { return identificador; }
    public double getQuilometroInicial() { return quilometroInicial; }
    public double getQuilometroFinal() { return quilometroFinal; }
    public double getNivelVegetacao() { return nivelVegetacao; }
    public boolean isTrechoUmido() { return Boolean.TRUE.equals(isTrechoUmido); }

    public void setNivelVegetacao(double nivelVegetacao) {
        this.nivelVegetacao = Math.max(0, nivelVegetacao);
    }

    @Override
    public String toString() {
        double extensao = quilometroFinal - quilometroInicial;
        String status = estaCritico() ? " [CRÍTICO]" : " [OK]";
        String clima = isTrechoUmido() ? "Úmido" : "Seco";

        return String.format(
            "%s (KM %.1f ao %.1f | Ext: %.1fkm | Clima: %s) | Vegetação: %.2f/%.0f cm%s",
            identificador, quilometroInicial, quilometroFinal, extensao, clima, nivelVegetacao, LIMITE_CRITICO, status
        );
    }
}
