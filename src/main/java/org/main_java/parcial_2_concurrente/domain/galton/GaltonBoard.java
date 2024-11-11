package org.main_java.parcial_2_concurrente.domain.galton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class GaltonBoard {

    @Id
    private String id;
    private int numBolas;
    private int numContenedores;
    private String estado;
    private Distribucion distribucion;

    public Mono<Void> simularCaidaDeBolas() {
        // Simulación de la caída de bolas
        for (int i = 0; i < numBolas; i++) {
            int contenedorId = (int) (Math.random() * numContenedores);
            distribucion.agregarBola(contenedorId).block();
        }
        return Mono.empty();
    }

    // Getters y Setters
}
