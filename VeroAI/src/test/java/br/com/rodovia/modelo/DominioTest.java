package br.com.rodovia.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

/**
 * Substitui os "testes unitarios manuais" (metodos main) das Sprints 2 e 3:
 * agora sao testes JUnit de verdade, sem banco e sem Spring.
 */
class DominioTest {

    @Test
    void intervencaoOperacionalContinuaAbstrata() {
        assertTrue(Modifier.isAbstract(IntervencaoOperacional.class.getModifiers()));
    }

    @Test
    void mockIoTCapturaDadosDoSensor() {
        MonitoravelViaIoT droneSimuladoMock = () -> 45.5;
        assertEquals(45.5, droneSimuladoMock.transmitirDadosSensor());
    }

    @Test
    void trechoIoTTransmiteNivelDeVegetacao() {
        TrechoIoT trecho = new TrechoIoT("IoT-1", 0, 10, 33.0, true);
        assertTrue(trecho instanceof MonitoravelViaIoT);
        assertEquals(33.0, trecho.transmitirDadosSensor());
    }

    @Test
    void crescimentoUmidoE3VezesMaisRapidoQueSeco() {
        TrechoRodovia umido = new TrechoRodovia("U", 0, 10, 0, true);
        TrechoRodovia seco = new TrechoRodovia("S", 0, 10, 0, false);
        umido.simularCrescimento(10);
        seco.simularCrescimento(10);
        assertEquals(15.0, umido.getNivelVegetacao());
        assertEquals(5.0, seco.getNivelVegetacao());
    }

    @Test
    void estaCriticoNoLimiteDe30cm() {
        assertFalse(new TrechoRodovia("A", 0, 10, 29.9, false).estaCritico());
        assertTrue(new TrechoRodovia("B", 0, 10, 30.0, false).estaCritico());
    }

    @Test
    void rocadaZeraEPulverizacaoReduzPara5() {
        TrechoRodovia t1 = new TrechoRodovia("R", 0, 10, 50, false);
        new RocadaMecanizada(null, t1).executarServico(t1);
        assertEquals(0.0, t1.getNivelVegetacao());

        TrechoRodovia t2 = new TrechoRodovia("P", 0, 10, 35, false);
        new Pulverizacao(null, t2).executarServico(t2);
        assertEquals(5.0, t2.getNivelVegetacao());
    }

    @Test
    void quilometragemInvalidaLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> new TrechoRodovia("X", -1, 10, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new TrechoRodovia("Y", 10, 10, 0, false));
    }
}
