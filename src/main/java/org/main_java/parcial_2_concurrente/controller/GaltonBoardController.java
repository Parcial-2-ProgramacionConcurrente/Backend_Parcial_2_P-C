package org.main_java.parcial_2_concurrente.controller;

import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.model.galtonDTO.GaltonBoardDTO;
import org.main_java.parcial_2_concurrente.service.galtonService.GaltonBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/galtonboard")
public class GaltonBoardController {

    private final GaltonBoardService galtonBoardService;

    @Autowired
    public GaltonBoardController(GaltonBoardService galtonBoardService) {
        this.galtonBoardService = galtonBoardService;
    }

    // Endpoint para obtener la cantidad de bolas por contenedor en tiempo real
    @PostMapping("/bolasPorContenedor")
    public Mono<ResponseEntity<Map<String, Integer>>> bolasPorContenedor(@RequestParam String galtonBoardId) {
        return galtonBoardService.obtenerGaltonBoardPorId(galtonBoardId)
                .flatMap(galtonBoard -> galtonBoard.getDistribucion().obtenerDistribucion())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Endpoint para mostrar la distribución final tras la simulación
    @PostMapping("/mostrarDistribucion")
    public Mono<ResponseEntity<Map<String, Integer>>> mostrarDistribucion(@RequestParam String galtonBoardId) {
        return galtonBoardService.obtenerGaltonBoardPorId(galtonBoardId)
                .flatMap(galtonBoard -> galtonBoard.getDistribucion().obtenerDistribucion())
                .doOnSuccess(galtonBoardService::mostrarDistribucion)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}