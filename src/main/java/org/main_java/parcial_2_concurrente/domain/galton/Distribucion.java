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

    /**
     * Agrega una bola al contenedor especificado de manera concurrente.
     *
     * @param contenedorId El ID del contenedor donde se agrega la bola.
     * @return Mono<String> mensaje indicando el estado del contenedor después de agregar la bola.
     */
    public Mono<String> agregarBola(int contenedorId) {
        if (contenedorId < 0 || contenedorId >= numContenedores) {
            return Mono.error(new IllegalArgumentException("Contenedor ID fuera de rango: " + contenedorId));
        }

        // Identificador de contenedor en el mapa
        String key = "contenedor_" + contenedorId;

        // Actualización concurrente del contador de bolas en el contenedor
        datos.merge(key, 1, Integer::sum);

        // Devolución de mensaje de estado para depuración o notificación
        String mensaje = "Bola agregada a " + key + ". Total en este contenedor: " + datos.get(key);
        return Mono.just(mensaje);
    }

    public Mono<Map<String, Integer>> obtenerDistribucion() {
        return Mono.just(datos);
    }
}
