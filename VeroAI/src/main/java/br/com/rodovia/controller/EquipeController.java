package br.com.rodovia.controller;

import br.com.rodovia.dto.EquipeRequest;
import br.com.rodovia.dto.EquipeResponse;
import br.com.rodovia.servico.EquipeService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeService service;

    public EquipeController(EquipeService service) {
        this.service = service;
    }

    @GetMapping
    public List<EquipeResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EquipeResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipeResponse criar(@Valid @RequestBody EquipeRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public EquipeResponse atualizar(@PathVariable Long id, @Valid @RequestBody EquipeRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
