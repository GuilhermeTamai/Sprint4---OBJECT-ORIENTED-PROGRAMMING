package br.com.rodovia;

import br.com.rodovia.principal.SistemaPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SistemaPrincipal.class)
class SistemaPrincipalTests {

    @Test
    void contextLoads() {
        // Garante que entidades, repositories (inclusive as derived queries) e beans sobem sem erro.
    }
}
