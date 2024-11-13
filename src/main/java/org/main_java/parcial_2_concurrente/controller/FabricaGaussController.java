package org.main_java.parcial_2_concurrente.controller;

import org.main_java.parcial_2_concurrente.model.fabricaDTO.FabricaGaussDTO;
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
    private GaltonBoardService galtonBoardService;

    @PostMapping
    public Mono<FabricaGaussDTO> createFabrica(@RequestBody FabricaGaussDTO fabricaGaussDTO) {
        return fabricaGaussService.createFabrica(fabricaGaussDTO);
    }

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

    @PutMapping("/{id}")
    public Mono<ResponseEntity<FabricaGaussDTO>> updateFabrica(@PathVariable String id, @RequestBody FabricaGaussDTO fabricaGaussDTO) {
        return fabricaGaussService.updateFabrica(id, fabricaGaussDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteFabricaById(@PathVariable String id) {
        return fabricaGaussService.deleteFabricaById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/iniciar-produccion")
    public Mono<Void> iniciarProduccionCompleta() {
        return fabricaGaussService.iniciarProduccionCompleta();
    }

    @PostMapping("/detenerProduccion")
    public Mono<Void> detenerProduccion() {
        return fabricaGaussService.detenerSimulacion();
    }

    @PostMapping("/simularCaidaBolas")
    public Mono<Void> simularCaidaDeBolas(@RequestParam Integer numBolas) {
        return galtonBoardService.simularCaidaDeBolas(numBolas);
    }

    @PostMapping("/ensamblarMaquina")
    public Mono<Void> ensamblarMaquina(@RequestParam String maquinaId) {
        return fabricaGaussService.ensamblarMaquina(maquinaId);
    }

    @GetMapping("/calcularDistribucion")
    public Mono<Void> calcularDistribucion(@RequestParam String maquinaId) {
        return fabricaGaussService.calcularDistribucion(maquinaId);
    }

    @GetMapping("/estadoDistribucion")
    public Mono<Void> obtenerEstadoDistribucion(@RequestParam String galtonBoardId) {
        return galtonBoardService.mostrarDistribucion(galtonBoardId);
    }

    @GetMapping("/estadoMaquina")
    public Mono<Void> verEstadoMaquina(@RequestParam String maquinaId) {
        return fabricaGaussService.verEstadoMaquina(maquinaId);
    }

    @PostMapping("/registrarComponente")
    public Mono<Void> registrarComponente(@RequestParam String componenteId, @RequestParam double valor) {
        return fabricaGaussService.registrarComponente(componenteId, valor);
    }

    @PostMapping("/gestionarErrores")
    public Mono<Void> gestionarErrores() {
        return fabricaGaussService.gestionarErrores();
    }

    @GetMapping("/monitorearRendimiento")
    public Mono<Void> monitorearRendimiento() {
        return fabricaGaussService.monitorearRendimiento();
    }
}


/* POSIBLES ADICIONES:

@PostMapping("/iniciarProduccion")
    public Mono<Void> iniciarProduccionCompleta() {
        return fabricaGaussService.iniciarProduccionCompleta();
    }

    @PostMapping("/detenerProduccion")
    public Mono<Void> detenerProduccion() {
        return fabricaGaussService.detenerSimulacion();
    }

    @PostMapping("/simularCaidaBolas")
    public Mono<Void> simularCaidaDeBolas(@RequestParam Integer numBolas) {
        return galtonBoardService.simularCaidaDeBolas(numBolas);
    }

    @PostMapping("/ensamblarMaquina")
    public Mono<Void> ensamblarMaquina(@RequestParam String maquinaId) {
        return fabricaGaussService.ensamblarMaquina(maquinaId);
    }

    @GetMapping("/calcularDistribucion")
    public Mono<Void> calcularDistribucion(@RequestParam String maquinaId) {
        return fabricaGaussService.calcularDistribucion(maquinaId);
    }

    @GetMapping("/estadoDistribucion")
    public Mono<Void> obtenerEstadoDistribucion(@RequestParam String galtonBoardId) {
        return galtonBoardService.mostrarDistribucion(galtonBoardId);
    }

    @GetMapping("/estadoMaquina")
    public Mono<Void> verEstadoMaquina(@RequestParam String maquinaId) {
        return fabricaGaussService.verEstadoMaquina(maquinaId);
    }

    @PostMapping("/registrarComponente")
    public Mono<Void> registrarComponente(@RequestParam String componenteId, @RequestParam double valor) {
        return fabricaGaussService.registrarComponente(componenteId, valor);
    }

    @PostMapping("/gestionarErrores")
    public Mono<Void> gestionarErrores() {
        return fabricaGaussService.gestionarErrores();
    }

    @GetMapping("/monitorearRendimiento")
    public Mono<Void> monitorearRendimiento() {
        return fabricaGaussService.monitorearRendimiento();
    }
 */