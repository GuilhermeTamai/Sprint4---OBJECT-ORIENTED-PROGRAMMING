package br.com.rodovia.controller;

import br.com.rodovia.dto.IntervencaoRequest;
import br.com.rodovia.dto.IntervencaoResponse;
import br.com.rodovia.servico.IntervencaoService;
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

@RestController
@RequestMapping("/api/intervencoes")
public class IntervencaoController {

    private final IntervencaoService service;

    public IntervencaoController(IntervencaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<IntervencaoResponse> listar(@RequestParam(required = false) String tipo) {
        return service.listar(tipo);
    }

    @GetMapping("/{id}")
    public IntervencaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IntervencaoResponse criar(@Valid @RequestBody IntervencaoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public IntervencaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody IntervencaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
