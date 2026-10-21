package br.com.rodovia.controller;

import br.com.rodovia.dto.RelatorioResponse;
import br.com.rodovia.servico.GerenciadorPrioridade;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final GerenciadorPrioridade service;

    public RelatorioController(GerenciadorPrioridade service) {
        this.service = service;
    }

    /** Gera o relatorio a partir dos trechos atuais e persiste no historico. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RelatorioResponse gerar(@RequestParam(defaultValue = "false") boolean aplicarIntervencoes) {
        return service.gerarRelatorioEProcessar(aplicarIntervencoes);
    }

    @GetMapping
    public List<RelatorioResponse> historico() {
        return service.listarHistorico();
    }

    @GetMapping("/periodo")
    public List<RelatorioResponse> porPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.listarPorPeriodo(inicio, fim);
    }

    @GetMapping("/{id}")
    public RelatorioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
