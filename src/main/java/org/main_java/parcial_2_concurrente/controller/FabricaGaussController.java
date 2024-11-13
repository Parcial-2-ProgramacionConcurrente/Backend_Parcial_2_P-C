package org.main_java.parcial_2_concurrente.controller;

import org.main_java.parcial_2_concurrente.model.fabricaDTO.FabricaGaussDTO;
import org.main_java.parcial_2_concurrente.service.fabricaService.FabricaGaussService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fabricas")
public class FabricaGaussController {

    @Autowired
    private FabricaGaussService fabricaGaussService;

    @GetMapping
    public Flux<FabricaGaussDTO> findAllFabricas() {
        return fabricaGaussService.findAllFabricas();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<FabricaGaussDTO>> findFabricaById(@PathVariable String id) {
        return fabricaGaussService.findFabricaById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/iniciar-produccion")
    public Mono<Void> iniciarProduccionCompleta() {
        return fabricaGaussService.iniciarProduccionCompleta();
    }
}
