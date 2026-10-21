package br.com.rodovia.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("1")
public class TrechoIoT extends TrechoRodovia implements MonitoravelViaIoT {

    protected TrechoIoT() {
    }

    public TrechoIoT(String identificador, double kmInicial, double kmFinal, double nivelVegetacaoInicial, boolean isTrechoUmido) {
        super(identificador, kmInicial, kmFinal, nivelVegetacaoInicial, isTrechoUmido);
    }

    @Override
    public double transmitirDadosSensor() {
        return this.nivelVegetacao;
    }
}
