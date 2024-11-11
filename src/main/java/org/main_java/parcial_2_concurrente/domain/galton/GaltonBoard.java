package org.main_java.parcial_2_concurrente.domain.galton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class GaltonBoard {

    @Id
    private String id;
    private int numBolas;           // Número de bolas a simular
    private int numContenedores;    // Número de contenedores
    private String estado;          // Estado del tablero (ej., "EN_SIMULACION", "COMPLETADO")
    private Distribucion distribucion;  // Distribución de bolas en los contenedores
    private String fabricaId;

    /**
     * Simula la caída de las bolas en el tablero de Galton de manera reactiva.
     * @return Mono<Void> que indica que la simulación se ha completado.
     */
    public Mono<Void> simularCaidaDeBolas() {
        this.estado = "EN_SIMULACION";
        System.out.println("Iniciando simulación de caída de bolas en el tablero de Galton con " + numBolas + " bolas y " + numContenedores + " contenedores.");

        return Flux.range(0, numBolas)
                .flatMap(i -> {
                    // Generar un ID de contenedor aleatorio para cada bola
                    int contenedorId = (int) (Math.random() * numContenedores);
                    return distribucion.agregarBola(contenedorId)
                            .doOnSuccess(v -> System.out.println("Bola #" + (i + 1) + " cayó en el contenedor " + contenedorId));
                })
                .then(Mono.fromRunnable(() -> {
                    this.estado = "COMPLETADO";
                    System.out.println("Simulación de caída de bolas completada. Estado del tablero: " + estado);
                }))
                .doOnError(e -> System.err.println("Error en la simulación de caída de bolas: " + e.getMessage())).then();
    }
}
