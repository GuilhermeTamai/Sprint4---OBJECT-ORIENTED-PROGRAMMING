package br.com.rodovia.servico;

import br.com.rodovia.dto.IntervencaoRequest;
import br.com.rodovia.dto.IntervencaoResponse;
import br.com.rodovia.exception.RecursoNaoEncontradoException;
import br.com.rodovia.exception.RegraNegocioException;
import br.com.rodovia.modelo.EquipeManutencao;
import br.com.rodovia.modelo.IntervencaoOperacional;
import br.com.rodovia.modelo.TipoIntervencao;
import br.com.rodovia.modelo.TrechoRodovia;
import br.com.rodovia.repository.EquipeManutencaoRepository;
import br.com.rodovia.repository.IntervencaoOperacionalRepository;
import br.com.rodovia.repository.TrechoRodoviaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IntervencaoService {

    private final IntervencaoOperacionalRepository intervencaoRepository;
    private final EquipeManutencaoRepository equipeRepository;
    private final TrechoRodoviaRepository trechoRepository;

    public IntervencaoService(IntervencaoOperacionalRepository intervencaoRepository,
                              EquipeManutencaoRepository equipeRepository,
                              TrechoRodoviaRepository trechoRepository) {
        this.intervencaoRepository = intervencaoRepository;
        this.equipeRepository = equipeRepository;
        this.trechoRepository = trechoRepository;
    }

    @Transactional(readOnly = true)
    public List<IntervencaoResponse> listar(String tipo) {
        List<IntervencaoOperacional> lista;
        if (tipo == null || tipo.isBlank()) {
            lista = intervencaoRepository.findAll();
        } else {
            lista = intervencaoRepository.findByTipoIntervencao(validarTipo(tipo).name());
        }
        return lista.stream().map(IntervencaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public IntervencaoResponse buscar(Long id) {
        return IntervencaoResponse.de(buscarEntidade(id));
    }

    /** Registra a intervencao E aplica o efeito dela no trecho */
    public IntervencaoResponse criar(IntervencaoRequest req) {
        TipoIntervencao tipo = validarTipo(req.tipoIntervencao());
        EquipeManutencao equipe = buscarEquipe(req.idEquipe());
        TrechoRodovia trecho = buscarTrecho(req.idTrecho());
        return IntervencaoResponse.de(aplicar(trecho, tipo, equipe));
    }

    /** Corrige o registro historico (equipe/trecho). O tipo nao muda: nao reaplica efeito no trecho. */
    public IntervencaoResponse atualizar(Long id, IntervencaoRequest req) {
        IntervencaoOperacional intervencao = buscarEntidade(id);
        TipoIntervencao tipo = validarTipo(req.tipoIntervencao());
        if (tipo != intervencao.getTipo()) {
            throw new RegraNegocioException("O tipo de uma intervencao registrada nao pode ser alterado ("
                    + intervencao.getTipo() + "); remova e registre novamente.");
        }
        intervencao.setEquipe(buscarEquipe(req.idEquipe()));
        intervencao.setTrecho(buscarTrecho(req.idTrecho()));
        return IntervencaoResponse.de(intervencaoRepository.save(intervencao));
    }

    public void remover(Long id) {
        buscarEntidade(id);
        intervencaoRepository.deleteById(id);
    }


    public IntervencaoOperacional aplicar(TrechoRodovia trecho, TipoIntervencao tipo, EquipeManutencao equipe) {
        IntervencaoOperacional intervencao = IntervencaoOperacional.criar(tipo, equipe, trecho);
        intervencao.executarServico(trecho);   
        trechoRepository.save(trecho);
        return intervencaoRepository.save(intervencao);
    }

    // ------------------------------------------------------------------

    private IntervencaoOperacional buscarEntidade(Long id) {
        return intervencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Intervencao " + id + " nao encontrada."));
    }

    private EquipeManutencao buscarEquipe(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipe " + id + " nao encontrada."));
    }

    private TrechoRodovia buscarTrecho(String id) {
        return trechoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trecho '" + id + "' nao encontrado."));
    }

    private TipoIntervencao validarTipo(String texto) {
        TipoIntervencao tipo = TipoIntervencao.deTexto(texto);
        if (tipo == null) {
            throw new RegraNegocioException("Tipo de intervencao invalido: '" + texto
                    + "'. Use ROCADA_MECANIZADA ou PULVERIZACAO.");
        }
        return tipo;
    }
}
