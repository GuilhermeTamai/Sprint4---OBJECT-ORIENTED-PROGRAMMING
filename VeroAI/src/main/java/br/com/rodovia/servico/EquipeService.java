package br.com.rodovia.servico;

import br.com.rodovia.dto.EquipeRequest;
import br.com.rodovia.dto.EquipeResponse;
import br.com.rodovia.exception.ConflitoException;
import br.com.rodovia.exception.RecursoNaoEncontradoException;
import br.com.rodovia.exception.RegraNegocioException;
import br.com.rodovia.modelo.EquipeManutencao;
import br.com.rodovia.repository.EquipeManutencaoRepository;
import br.com.rodovia.repository.IntervencaoOperacionalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EquipeService {

    private final EquipeManutencaoRepository equipeRepository;
    private final IntervencaoOperacionalRepository intervencaoRepository;

    public EquipeService(EquipeManutencaoRepository equipeRepository,
                         IntervencaoOperacionalRepository intervencaoRepository) {
        this.equipeRepository = equipeRepository;
        this.intervencaoRepository = intervencaoRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipeResponse> listar() {
        return equipeRepository.findAll().stream().map(EquipeResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EquipeResponse buscar(Long id) {
        return EquipeResponse.de(buscarEntidade(id));
    }

    public EquipeResponse criar(EquipeRequest req) {
        String nome = normalizarNome(req.nomeEquipe());
        if (equipeRepository.existsByNomeEquipe(nome)) {
            throw new ConflitoException("Ja existe uma equipe chamada '" + nome + "'.");
        }
        return EquipeResponse.de(equipeRepository.save(new EquipeManutencao(nome)));
    }

    public EquipeResponse atualizar(Long id, EquipeRequest req) {
        EquipeManutencao equipe = buscarEntidade(id);
        String nome = normalizarNome(req.nomeEquipe());
        boolean nomeEmUsoPorOutra = equipeRepository.findByNomeEquipe(nome)
                .map(outra -> !outra.getIdEquipe().equals(id))
                .orElse(false);
        if (nomeEmUsoPorOutra) {
            throw new ConflitoException("Ja existe uma equipe chamada '" + nome + "'.");
        }
        equipe.setNomeEquipe(nome);
        return EquipeResponse.de(equipeRepository.save(equipe));
    }

    public void remover(Long id) {
        buscarEntidade(id);
        if (intervencaoRepository.existsByEquipeIdEquipe(id)) {
            throw new ConflitoException("A equipe " + id + " possui intervencoes registradas e nao pode ser removida.");
        }
        equipeRepository.deleteById(id);
    }

    private EquipeManutencao buscarEntidade(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipe " + id + " nao encontrada."));
    }

    private String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da equipe e obrigatorio.");
        }
        return nome.trim();
    }
}
