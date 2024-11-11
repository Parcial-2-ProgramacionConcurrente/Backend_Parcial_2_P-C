package org.main_java.parcial_2_concurrente.controller;

import org.main_java.parcial_2_concurrente.domain.Usuario;
import org.main_java.parcial_2_concurrente.model.UsuarioDTO;
import org.main_java.parcial_2_concurrente.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public Flux<Usuario> getAll() {
        return usuarioService.getAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Usuario>> getById(@PathVariable String id) {
        return usuarioService.getById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<UsuarioDTO> create(@RequestBody UsuarioDTO usuarioDTO) {
        return usuarioService.create(usuarioDTO);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UsuarioDTO>> update(@PathVariable String id, @RequestBody UsuarioDTO usuarioActualizadoDTO) {
        return usuarioService.update(id, usuarioActualizadoDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return usuarioService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
