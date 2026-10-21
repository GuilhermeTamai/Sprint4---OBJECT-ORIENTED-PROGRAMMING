package br.com.rodovia.servico;

import br.com.rodovia.dto.TrechoRequest;
import br.com.rodovia.dto.TrechoResponse;
import br.com.rodovia.exception.ConflitoException;
import br.com.rodovia.exception.RecursoNaoEncontradoException;
import br.com.rodovia.exception.RegraNegocioException;
import br.com.rodovia.modelo.MonitoravelViaIoT;
import br.com.rodovia.modelo.TrechoIoT;
import br.com.rodovia.modelo.TrechoRodovia;
import br.com.rodovia.repository.IntervencaoOperacionalRepository;
import br.com.rodovia.repository.TrechoRodoviaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de negocio de Trecho (validacoes da Sprint 1 + simulacao de crescimento da Sprint 2). */
@Service
@Transactional
public class TrechoService {

    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoOperacionalRepository intervencaoRepository;

    public TrechoService(TrechoRodoviaRepository trechoRepository,
                         IntervencaoOperacionalRepository intervencaoRepository) {
        this.trechoRepository = trechoRepository;
        this.intervencaoRepository = intervencaoRepository;
    }

    @Transactional(readOnly = true)
    public List<TrechoResponse> listar(Double nivelMinimo) {
        List<TrechoRodovia> trechos = (nivelMinimo == null)
                ? trechoRepository.findAll()
                : trechoRepository.findByNivelVegetacaoGreaterThanEqual(nivelMinimo);
        return trechos.stream().map(TrechoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TrechoResponse buscar(String id) {
        return TrechoResponse.de(buscarEntidade(id));
    }

    public TrechoResponse criar(TrechoRequest req) {
        if (req.identificador() == null || req.identificador().isBlank()) {
            throw new RegraNegocioException("O identificador do trecho e obrigatorio.");
        }
        String id = req.identificador().trim();
        validarRegras(req.kmInicial(), req.kmFinal(), req.nivelVegetacao());

        if (trechoRepository.existsById(id)) {
            throw new ConflitoException("Ja existe um trecho com o identificador '" + id + "'.");
        }
        double kmIni = req.kmInicial();
        double kmFim = req.kmFinal();
        double nivel = req.nivelVegetacao();
        boolean umido = Boolean.TRUE.equals(req.umido());
        // Polimorfismo da Sprint 2: trecho com sensor vira TrechoIoT
        TrechoRodovia trecho = Boolean.TRUE.equals(req.iot())
                ? new TrechoIoT(id, kmIni, kmFim, nivel, umido)
                : new TrechoRodovia(id, kmIni, kmFim, nivel, umido);
        return TrechoResponse.de(trechoRepository.save(trecho));
    }

    public TrechoResponse atualizar(String id, TrechoRequest req) {
        TrechoRodovia trecho = buscarEntidade(id);
        validarRegras(req.kmInicial(), req.kmFinal(), req.nivelVegetacao());
        boolean iotAtual = trecho instanceof MonitoravelViaIoT;
        if (req.iot() != null && req.iot() != iotAtual) {
            throw new RegraNegocioException("Nao e possivel alterar o tipo IoT de um trecho existente; "
                    + "remova e recrie o trecho.");
        }
        trecho.atualizarDados(req.kmInicial(), req.kmFinal(), req.nivelVegetacao(),
                Boolean.TRUE.equals(req.umido()));
        return TrechoResponse.de(trechoRepository.save(trecho));
    }

    public void remover(String id) {
        buscarEntidade(id);
        if (intervencaoRepository.existsByTrechoIdentificador(id)) {
            throw new ConflitoException("O trecho '" + id + "' possui intervencoes registradas e nao pode ser removido.");
        }
        trechoRepository.deleteById(id);
    }

    /** Sprint 2: simula o crescimento da vegetacao (umido 1.5 cm/dia, seco 0.5 cm/dia) e persiste. */
    public TrechoResponse simularCrescimento(String id, int dias) {
        if (dias < 0) {
            throw new RegraNegocioException("O numero de dias da simulacao nao pode ser negativo.");
        }
        TrechoRodovia trecho = buscarEntidade(id);
        trecho.simularCrescimento(dias);
        return TrechoResponse.de(trechoRepository.save(trecho));
    }

    // ------------------------------------------------------------------

    private TrechoRodovia buscarEntidade(String id) {
        return trechoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trecho '" + id + "' nao encontrado."));
    }

    /** Validacoes da Sprint 1: ficam AQUI (e nao no Controller) para valerem para qualquer porta de entrada. */
    private void validarRegras(Double kmInicial, Double kmFinal, Double nivelVegetacao) {
        if (kmInicial == null || kmFinal == null || nivelVegetacao == null) {
            throw new RegraNegocioException("kmInicial, kmFinal e nivelVegetacao sao obrigatorios.");
        }
        if (kmInicial < 0 || kmFinal < 0) {
            throw new RegraNegocioException("Quilometragem nao pode ser negativa (informado: "
                    + kmInicial + " a " + kmFinal + ").");
        }
        if (kmFinal <= kmInicial) {
            throw new RegraNegocioException("O KM final deve ser maior que o inicial (informado: "
                    + kmInicial + " a " + kmFinal + ").");
        }
        if (nivelVegetacao < 0) {
            throw new RegraNegocioException("O nivel de vegetacao nao pode ser negativo (informado: "
                    + nivelVegetacao + ").");
        }
    }
}
