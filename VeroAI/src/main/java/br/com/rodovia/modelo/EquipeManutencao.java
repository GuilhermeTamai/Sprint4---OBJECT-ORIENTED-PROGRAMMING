package br.com.rodovia.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_EQUIPE_MANUTENCAO")
public class EquipeManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipe")
    private Long idEquipe;

    @Column(name = "nome_equipe", nullable = false, unique = true, length = 100)
    private String nomeEquipe;

    protected EquipeManutencao() {
    }

    public EquipeManutencao(String nomeEquipe) {
        this.nomeEquipe = nomeEquipe;
    }

    public Long getIdEquipe() { return idEquipe; }
    public String getNomeEquipe() { return nomeEquipe; }
    public void setNomeEquipe(String nomeEquipe) { this.nomeEquipe = nomeEquipe; }
}
