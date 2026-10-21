package br.com.rodovia;

import br.com.rodovia.principal.SistemaPrincipal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.rodovia.modelo.EquipeManutencao;
import br.com.rodovia.repository.EquipeManutencaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** CRUD de Intervencao (heranca RocadaMecanizada/Pulverizacao) e regras de integridade. */
@SpringBootTest(classes = SistemaPrincipal.class)
@Transactional
class IntervencaoApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EquipeManutencaoRepository equipeRepository;

    private MockMvc mockMvc;
    private Long idEquipe;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        idEquipe = equipeRepository.save(new EquipeManutencao("Equipe-Teste")).getIdEquipe();
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"TR-1\",\"kmInicial\":0,\"kmFinal\":10,"
                        + "\"nivelVegetacao\":50,\"umido\":true,\"iot\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iot").value(true));
    }

    private String corpo(String tipo) {
        return "{\"tipoIntervencao\":\"" + tipo + "\",\"idEquipe\":" + idEquipe + ",\"idTrecho\":\"TR-1\"}";
    }

    @Test
    void registrarRocada_retorna201_eZeraVegetacaoDoTrecho() throws Exception {
        mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ROCADA_MECANIZADA")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoIntervencao").value("ROCADA_MECANIZADA"))
                .andExpect(jsonPath("$.nomeEquipe").value("Equipe-Teste"));

        mockMvc.perform(get("/api/trechos/TR-1"))
                .andExpect(jsonPath("$.nivelVegetacao").value(0.0));
    }

    @Test
    void tipoInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("QUEIMADA")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void equipeOuTrechoInexistente_retorna404() throws Exception {
        mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoIntervencao\":\"PULVERIZACAO\",\"idEquipe\":999999,\"idTrecho\":\"TR-1\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoIntervencao\":\"PULVERIZACAO\",\"idEquipe\":" + idEquipe
                                + ",\"idTrecho\":\"NAO-EXISTE\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtrarPorTipo_usaDerivedQuery() throws Exception {
        mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("PULVERIZACAO"))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROCADA_MECANIZADA"))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/intervencoes").param("tipo", "PULVERIZACAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipoIntervencao").value("PULVERIZACAO"));
    }

    @Test
    void trechoComIntervencao_naoPodeSerRemovido_retorna409() throws Exception {
        mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("PULVERIZACAO"))).andExpect(status().isCreated());
        mockMvc.perform(delete("/api/trechos/TR-1")).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/equipes/" + idEquipe)).andExpect(status().isConflict());
    }

    @Test
    void atualizarComTipoDiferente_retorna400_eRemoverRetorna204() throws Exception {
        String resposta = mockMvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("PULVERIZACAO")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resposta.replaceAll(".*\"idIntervencao\":(\\d+).*", "$1");

        mockMvc.perform(put("/api/intervencoes/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("ROCADA_MECANIZADA"))).andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/intervencoes/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("PULVERIZACAO"))).andExpect(status().isOk());
        mockMvc.perform(delete("/api/intervencoes/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/intervencoes/" + id)).andExpect(status().isNotFound());
    }
}
