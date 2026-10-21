package br.com.rodovia.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ROCADA_MECANIZADA")
public class RocadaMecanizada extends IntervencaoOperacional {

    protected RocadaMecanizada() {
    }

    public RocadaMecanizada(EquipeManutencao equipe, TrechoRodovia trecho) {
        super(equipe, trecho);
    }

    @Override
    public void executarServico(TrechoRodovia trecho) {
        trecho.setNivelVegetacao(0); 
    }

    @Override
    public TipoIntervencao getTipo() {
        return TipoIntervencao.ROCADA_MECANIZADA;
    }
}
