package br.com.rodovia.repository;

import br.com.rodovia.modelo.TrechoRodovia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrechoRodoviaRepository extends JpaRepository<TrechoRodovia, String> {

    List<TrechoRodovia> findByNivelVegetacaoGreaterThanEqual(Double minimo);
}
