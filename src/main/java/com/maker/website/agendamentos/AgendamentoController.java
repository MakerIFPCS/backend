package com.maker.website.agendamentos;

import com.maker.website.agendamentos.dtos.AgendamentoRequestDTO;
import com.maker.website.agendamentos.dtos.AgendamentoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;

    @GetMapping
    public List<AgendamentoResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AgendamentoResponseDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> create(
            @RequestBody AgendamentoRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, currentUserId(authentication)));
    }

    @PutMapping("/{id}")
    public AgendamentoResponseDTO update(
            @PathVariable Long id,
            @RequestBody AgendamentoRequestDTO request,
            Authentication authentication
    ) {
        return service.update(id, request, currentUserId(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
