package br.com.rodovia.repository;

import br.com.rodovia.modelo.IntervencaoOperacional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntervencaoOperacionalRepository extends JpaRepository<IntervencaoOperacional, Long> {

    List<IntervencaoOperacional> findByTipoIntervencao(String tipoIntervencao);

    List<IntervencaoOperacional> findByTrechoIdentificador(String identificador);

    boolean existsByTrechoIdentificador(String identificador);

    boolean existsByEquipeIdEquipe(Long idEquipe);
}
