package br.com.rodovia.repository;

import br.com.rodovia.modelo.EquipeManutencao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeManutencaoRepository extends JpaRepository<EquipeManutencao, Long> {

    Optional<EquipeManutencao> findByNomeEquipe(String nomeEquipe);

    boolean existsByNomeEquipe(String nomeEquipe);
}
