package br.com.rodovia.repository;

import br.com.rodovia.modelo.RelatorioPrioridade;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelatorioPrioridadeRepository extends JpaRepository<RelatorioPrioridade, Long> {

    List<RelatorioPrioridade> findAllByOrderByDataGeracaoDesc();

    List<RelatorioPrioridade> findByDataGeracaoBetweenOrderByDataGeracaoDesc(LocalDateTime inicio, LocalDateTime fim);
}
