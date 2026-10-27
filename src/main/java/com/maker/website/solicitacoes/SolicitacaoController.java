package com.maker.website.solicitacoes;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SolicitacaoController {
    private final SolicitacaoService service;

    @GetMapping("/api/{tipo}/admin")
    public List<SolicitacaoDTO> findAll(@PathVariable String tipo) {
        return service.findAll(normalize(tipo));
    }

    @PostMapping("/api/{tipo}")
    public ResponseEntity<SolicitacaoDTO> create(@PathVariable String tipo, @RequestBody SolicitacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, normalize(tipo)));
    }

    @PutMapping("/api/{tipo}/{id}")
    public SolicitacaoDTO update(@PathVariable String tipo, @PathVariable Long id, @RequestBody SolicitacaoRequest request) {
        return service.update(id, request, normalize(tipo));
    }

    private String normalize(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "pedidos" -> "PEDIDO";
            case "emprestimos" -> "EMPRESTIMO";
            default -> tipo.toUpperCase();
        };
    }
}
