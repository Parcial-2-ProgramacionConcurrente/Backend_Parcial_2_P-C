package org.main_java.parcial_2_concurrente.domain.fabrica.maquina;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


public abstract class Maquina {

        @Id
        private String id;
        private String tipo;
        private int numeroComponentesRequeridos;
        private List<Componente> componentes;
        private Map<String, Integer> distribucion;
        private String estado;
        private GaltonBoard galtonBoard;

        public Mono<Void> ensamblarMaquina() {
            // Lógica de ensamblaje de la máquina, sincronizado con los componentes
            return Mono.when(componentes.stream()
                    .map(componente -> componente.registrarValor(Math.random() * 100))
                    .toList()
            );
        }

        public Mono<Void> detenerMaquina() {
            // Lógica para detener la máquina
            this.estado = "DETENIDA";
            return Mono.empty();
        }
    }
