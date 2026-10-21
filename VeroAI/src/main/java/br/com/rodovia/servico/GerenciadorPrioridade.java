package br.com.rodovia.servico;

import br.com.rodovia.dto.RelatorioResponse;
import br.com.rodovia.exception.RecursoNaoEncontradoException;
import br.com.rodovia.exception.RegraNegocioException;
import br.com.rodovia.modelo.EquipeManutencao;
import br.com.rodovia.modelo.MonitoravelViaIoT;
import br.com.rodovia.modelo.RelatorioPrioridade;
import br.com.rodovia.modelo.TipoIntervencao;
import br.com.rodovia.modelo.TrechoRodovia;
import br.com.rodovia.repository.EquipeManutencaoRepository;
import br.com.rodovia.repository.RelatorioPrioridadeRepository;
import br.com.rodovia.repository.TrechoRodoviaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GerenciadorPrioridade {

    private static final Logger log = LoggerFactory.getLogger(GerenciadorPrioridade.class);

    // Limiares da Sprint 2 (cm)
    public static final double LIMITE_URGENTE = 45.0;
    public static final double LIMITE_CRITICO = TrechoRodovia.LIMITE_CRITICO; // 30.0
    public static final double LIMITE_ATENCAO = 15.0;

    // Equipes padrao usadas na intervencao automatica 
    public static final String EQUIPE_ROCADA = "Tracto-Alpha";
    public static final String EQUIPE_PULVERIZACAO = "Química-Beta";

    private final RelatorioPrioridadeRepository relatorioRepository;
    private final TrechoRodoviaRepository trechoRepository;
    private final EquipeManutencaoRepository equipeRepository;
    private final IntervencaoService intervencaoService;

    public GerenciadorPrioridade(RelatorioPrioridadeRepository relatorioRepository,
                            TrechoRodoviaRepository trechoRepository,
                            EquipeManutencaoRepository equipeRepository,
                            IntervencaoService intervencaoService) {
        this.relatorioRepository = relatorioRepository;
        this.trechoRepository = trechoRepository;
        this.equipeRepository = equipeRepository;
        this.intervencaoService = intervencaoService;
    }


    public RelatorioResponse gerarRelatorioEProcessar(boolean aplicarIntervencoes) {
        List<TrechoRodovia> trechos = trechoRepository.findAll();

        int qtUrgente = 0;
        int qtCritico = 0;
        int qtAtencao = 0;
        int qtNormal = 0;
        int intervencoesFeitas = 0;

        for (TrechoRodovia trecho : trechos) {
            double leitura = (trecho instanceof MonitoravelViaIoT sensor)
                    ? sensor.transmitirDadosSensor()
                    : trecho.getNivelVegetacao();

            if (leitura >= LIMITE_URGENTE) {
                qtUrgente++;
            } else if (leitura >= LIMITE_CRITICO) {
                qtCritico++;
            } else if (leitura >= LIMITE_ATENCAO) {
                qtAtencao++;
            } else {
                qtNormal++;
            }

            if (aplicarIntervencoes && trecho.estaCritico()) {
                TipoIntervencao tipo = leitura >= LIMITE_URGENTE
                        ? TipoIntervencao.ROCADA_MECANIZADA
                        : TipoIntervencao.PULVERIZACAO;
                String nomeEquipe = (tipo == TipoIntervencao.ROCADA_MECANIZADA) ? EQUIPE_ROCADA : EQUIPE_PULVERIZACAO;

                EquipeManutencao equipe = equipeRepository.findByNomeEquipe(nomeEquipe).orElse(null);
                if (equipe == null) {
                    log.warn("Equipe '{}' nao cadastrada: intervencao no trecho {} nao executada.",
                            nomeEquipe, trecho.getIdentificador());
                    continue;
                }
                intervencaoService.aplicar(trecho, tipo, equipe);
                intervencoesFeitas++;
            }
        }

        String resumo = "Analise concluida em " + trechos.size() + " trechos. "
                + (aplicarIntervencoes
                    ? "Intervencoes executadas: " + intervencoesFeitas + "."
                    : "Modo somente analise: nenhuma intervencao executada.");

        RelatorioPrioridade salvo = relatorioRepository.save(
                new RelatorioPrioridade(qtUrgente, qtCritico, qtAtencao, qtNormal, resumo));
        log.info("Relatorio {} gerado: U={} C={} A={} N={}",
                salvo.getIdRelatorio(), qtUrgente, qtCritico, qtAtencao, qtNormal);
        return RelatorioResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponse> listarHistorico() {
        return relatorioRepository.findAllByOrderByDataGeracaoDesc().stream()
                .map(RelatorioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public RelatorioResponse buscar(Long id) {
        return relatorioRepository.findById(id)
                .map(RelatorioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Relatorio " + id + " nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponse> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("Informe os parametros 'inicio' e 'fim' (formato AAAA-MM-DD).");
        }
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data de inicio nao pode ser posterior a data de fim.");
        }
        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fim.atTime(23, 59, 59);
        return relatorioRepository.findByDataGeracaoBetweenOrderByDataGeracaoDesc(de, ate).stream()
                .map(RelatorioResponse::de).toList();
    }
}
