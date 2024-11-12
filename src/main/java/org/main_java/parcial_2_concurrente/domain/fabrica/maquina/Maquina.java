package org.main_java.parcial_2_concurrente.domain.fabrica.maquina;

import lombok.*;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.springframework.data.annotation.Id;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Maquina {

    @Id
    private String id;
    private String tipo;
    private int numeroComponentesRequeridos;
    private List<String> componentesIds = new ArrayList<>();
    private Map<String, Integer> distribucion;  // Distribución resultante de la operación
    private String estado;
    private GaltonBoard galtonBoard;
}
