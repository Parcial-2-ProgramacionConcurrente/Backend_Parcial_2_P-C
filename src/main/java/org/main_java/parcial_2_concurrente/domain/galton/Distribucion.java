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

    private Map<String, Integer> datos = new HashMap<>();
    private int numBolas;
    private int numContenedores;

    public Distribucion(int numContenedores) {
        this.numContenedores = numContenedores;
        for (int i = 0; i < numContenedores; i++) {
            datos.put("contenedor_" + i, 0);
        }
    }

    public Mono<String> agregarBola(int contenedorId) {
        if (contenedorId < 0 || contenedorId >= numContenedores) {
            return Mono.error(new IllegalArgumentException("Contenedor ID fuera de rango: " + contenedorId));
        }
        String key = "contenedor_" + contenedorId;
        datos.merge(key, 1, Integer::sum);
        return Mono.just("Bola agregada a " + key + ". Total en este contenedor: " + datos.get(key));
    }

    public Mono<Map<String, Integer>> obtenerDistribucion() {
        return Mono.just(datos);
    }
}
