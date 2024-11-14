package org.main_java.parcial_2_concurrente.controller;

import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.model.fabricaDTO.FabricaGaussDTO;
import org.main_java.parcial_2_concurrente.model.galtonDTO.GaltonBoardDTO;
import org.main_java.parcial_2_concurrente.service.fabricaService.FabricaGaussService;
import org.main_java.parcial_2_concurrente.service.galtonService.GaltonBoardService;
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

    @Autowired
    private GaltonBoardService galtonBoardService; // Asegúrate de tener acceso al servicio


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

    private GaltonBoardDTO mapToDTO(GaltonBoard galtonBoard) {
        GaltonBoardDTO dto = new GaltonBoardDTO();
        dto.setId(galtonBoard.getId());
        dto.setNumBolas(galtonBoard.getNumBolas());
        dto.setNumContenedores(galtonBoard.getNumContenedores());
        dto.setEstado(galtonBoard.getEstado());
        dto.setDistribucion(galtonBoard.getDistribucion());
        dto.setFabricaId(galtonBoard.getFabricaId());
        return dto;
    }

    @GetMapping("/{id}/galtonboard")
    public Mono<ResponseEntity<GaltonBoardDTO>> obtenerGaltonBoardPorFabricaId(@PathVariable String id) {
        return galtonBoardService.obtenerGaltonBoardPorFabricaId(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    @PostMapping("/iniciar-produccion")
    public Mono<Void> iniciarProduccionCompleta() {
        return fabricaGaussService.iniciarProduccionCompleta();
    }
}

