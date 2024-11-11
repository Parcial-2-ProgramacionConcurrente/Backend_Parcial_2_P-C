package org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes;

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
public class Componente {

    @Id
    private String id;
    private String tipo;
    private double valorCalculado;

    public Mono<Void> registrarValor(double valor) {
        this.valorCalculado = valor;
        return Mono.empty();
    }

    // Getters y Setters
}

