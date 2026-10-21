package br.com.rodovia;

import br.com.rodovia.principal.SistemaPrincipal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/** Testes de API (CRUD de Trecho + codigos HTTP). Usam H2 em memoria, nao o Oracle. */
@SpringBootTest(classes = SistemaPrincipal.class)
@Transactional // cada teste faz rollback: banco sempre limpo
class TrechoApiTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    static String json(String id, double kmIni, double kmFim, double nivel) {
        return """
                {"identificador":"%s","kmInicial":%s,"kmFinal":%s,"nivelVegetacao":%s,"umido":true,"iot":false}
                """.formatted(id, kmIni, kmFim, nivel);
    }

    @Test
    void criarTrecho_retorna201ComCorpo() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("T-1", 0, 10, 12.5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificador").value("T-1"))
                .andExpect(jsonPath("$.critico").value(false));
    }

    @Test
    void criarTrecho_kmFinalMenorQueInicial_retorna400() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("T-2", 20, 10, 5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void criarTrecho_nivelNegativo_retorna400PeloBeanValidation() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("T-3", 0, 10, -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes").isNotEmpty());
    }

    @Test
    void criarTrecho_identificadorDuplicado_retorna409() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(json("T-4", 0, 10, 5))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("T-4", 0, 10, 5)))
                .andExpect(status().isConflict());
    }

    @Test
    void buscarTrecho_existenteRetorna200_inexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(json("T-5", 0, 10, 5))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/trechos/T-5")).andExpect(status().isOk());
        mockMvc.perform(get("/api/trechos/NAO-EXISTE")).andExpect(status().isNotFound());
    }

    @Test
    void atualizarTrecho_retorna200_e404SeNaoExiste() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(json("T-6", 0, 10, 5))).andExpect(status().isCreated());

        mockMvc.perform(put("/api/trechos/T-6").contentType(MediaType.APPLICATION_JSON)
                        .content(json("T-6", 0, 10, 40)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelVegetacao").value(40.0))
                .andExpect(jsonPath("$.critico").value(true));

        mockMvc.perform(put("/api/trechos/FANTASMA").contentType(MediaType.APPLICATION_JSON)
                        .content(json("FANTASMA", 0, 10, 5)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerTrecho_retorna204_edepoisRetorna404() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(json("T-7", 0, 10, 5))).andExpect(status().isCreated());

        mockMvc.perform(delete("/api/trechos/T-7")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/trechos/T-7")).andExpect(status().isNotFound());
    }

    @Test
    void listarComFiltroNivelMinimo_usaDerivedQuery() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(json("BAIXO", 0, 10, 5))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(json("ALTO", 10, 20, 50))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/trechos").param("nivelMinimo", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].identificador").value("ALTO"));
    }

    @Test
    void simularCrescimento_trechoUmido15Dias_somaTaxaDe1ponto5() throws Exception {
        mockMvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                .content(json("T-8", 0, 10, 10))).andExpect(status().isCreated());

        // umido: 1.5 cm/dia * 10 dias = +15 -> 25
        mockMvc.perform(post("/api/trechos/T-8/crescimento").param("dias", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelVegetacao").value(25.0));

        mockMvc.perform(post("/api/trechos/T-8/crescimento").param("dias", "-1"))
                .andExpect(status().isBadRequest());
    }
}
