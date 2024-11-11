package org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.MaquinaWorker;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class ComponenteWorker {

    @Id
    private String id;
    private Componente componente;
    private MaquinaWorker maquinaWorker;
    private GaltonBoard galtonBoard;
    private boolean trabajoCompletado;

    public Mono<Void> run() {
        return calcularValor().flatMap(valor -> {
            componente.registrarValor(valor);
            trabajoCompletado = true;
            return Mono.empty();
        });
    }

    public Mono<Double> calcularValor() {
        // Simulación del cálculo de valor
        return Mono.just(Math.random() * 100);
    }

    // Getters y Setters
}
