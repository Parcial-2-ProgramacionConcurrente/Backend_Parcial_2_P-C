package org.main_java.parcial_2_concurrente.domain.galton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Distribucion {

    @Id
    private String id;
    private Map<String, Integer> datos = new HashMap<>();
    private int numBolas;
    private int numContenedores;

    public Mono<Void> agregarBola(int contenedorId) {
        String key = "contenedor_" + contenedorId;
        datos.put(key, datos.getOrDefault(key, 0) + 1);
        return Mono.empty();
    }

    public Mono<Map<String, Integer>> obtenerDistribucion() {
        return Mono.just(datos);
    }
}
