package br.com.rodovia;

import br.com.rodovia.principal.SistemaPrincipal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.rodovia.modelo.EquipeManutencao;
import br.com.rodovia.repository.EquipeManutencaoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** Testes do motor de prioridade exposto como endpoint REST. */
@SpringBootTest(classes = SistemaPrincipal.class)
@Transactional
class RelatorioApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EquipeManutencaoRepository equipeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        criarTrecho("URGENTE-1", 50);   // >= 45
        criarTrecho("CRITICO-1", 35);   // >= 30
        criarTrecho("ATENCAO-1", 20);   // >= 15
        criarTrecho("NORMAL-1", 5);     // < 15
    }

    private void criarTrecho(String id, double nivel) throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(TrechoApiTest.json(id, 0, 10, nivel))).andExpect(status().isCreated());
    }

    @Test
    void gerarRelatorio_classificaTrechosEPersiste() throws Exception {
        mockMvc.perform(post("/api/relatorios"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.qtUrgente").value(1))
                .andExpect(jsonPath("$.qtCritico").value(1))
                .andExpect(jsonPath("$.qtAtencao").value(1))
                .andExpect(jsonPath("$.qtNormal").value(1))
                .andExpect(jsonPath("$.idRelatorio").isNumber());

        mockMvc.perform(get("/api/relatorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void gerarRelatorio_comIntervencoes_rocaZeraUrgenteEPulverizaCritico() throws Exception {
        equipeRepository.save(new EquipeManutencao("Tracto-Alpha"));
        equipeRepository.save(new EquipeManutencao("Química-Beta"));

        mockMvc.perform(post("/api/relatorios").param("aplicarIntervencoes", "true"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/trechos/URGENTE-1"))
                .andExpect(jsonPath("$.nivelVegetacao").value(0.0));   // rocada
        mockMvc.perform(get("/api/trechos/CRITICO-1"))
                .andExpect(jsonPath("$.nivelVegetacao").value(5.0));   // pulverizacao
        mockMvc.perform(get("/api/intervencoes"))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/intervencoes").param("tipo", "PULVERIZACAO"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void consultarPorPeriodo_retornaRelatorioDeHoje() throws Exception {
        mockMvc.perform(post("/api/relatorios")).andExpect(status().isCreated());
        String hoje = LocalDate.now().toString();

        mockMvc.perform(get("/api/relatorios/periodo").param("inicio", hoje).param("fim", hoje))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/relatorios/periodo")
                        .param("inicio", "2000-01-01").param("fim", "2000-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void consultarPorPeriodo_inicioDepoisDoFim_retorna400() throws Exception {
        mockMvc.perform(get("/api/relatorios/periodo")
                        .param("inicio", "2026-12-31").param("fim", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarPorPeriodo_dataComFormatoInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/api/relatorios/periodo")
                        .param("inicio", "ontem").param("fim", "hoje"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarRelatorioInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/relatorios/999999")).andExpect(status().isNotFound());
    }
}
