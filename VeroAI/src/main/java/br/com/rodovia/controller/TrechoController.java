package br.com.rodovia.controller;

import br.com.rodovia.dto.TrechoRequest;
import br.com.rodovia.dto.TrechoResponse;
import br.com.rodovia.servico.TrechoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Camada HTTP: so recebe a requisicao, delega ao Service e devolve o status correto. */
@RestController
@RequestMapping("/api/trechos")
public class TrechoController {

    private final TrechoService service;

    public TrechoController(TrechoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TrechoResponse> listar(@RequestParam(required = false) Double nivelMinimo) {
        return service.listar(nivelMinimo);
    }

    @GetMapping("/{id}")
    public TrechoResponse buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrechoResponse criar(@Valid @RequestBody TrechoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public TrechoResponse atualizar(@PathVariable String id, @Valid @RequestBody TrechoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable String id) {
        service.remover(id);
    }

    /** Sprint 2: simula N dias de crescimento e grava o novo nivel de vegetacao. */
    @PostMapping("/{id}/crescimento")
    public TrechoResponse simularCrescimento(@PathVariable String id, @RequestParam int dias) {
        return service.simularCrescimento(id, dias);
    }
}
