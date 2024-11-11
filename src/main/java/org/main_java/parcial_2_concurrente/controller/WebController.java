package org.main_java.parcial_2_concurrente.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class WebController {

    @GetMapping
    public Mono<String> index() {
        return Mono.just("Welcome to the main page of the Jurassic Monitoring System!");
    }
}


