package br.com.rodovia.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PULVERIZACAO")
public class Pulverizacao extends IntervencaoOperacional {

    protected Pulverizacao() {
    }

    public Pulverizacao(EquipeManutencao equipe, TrechoRodovia trecho) {
        super(equipe, trecho);
    }

    @Override
    public void executarServico(TrechoRodovia trecho) {
        trecho.setNivelVegetacao(5.0); 
    }

    @Override
    public TipoIntervencao getTipo() {
        return TipoIntervencao.PULVERIZACAO;
    }
}
