package org.main_java.parcial_2_concurrente.domain.fabrica;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class FabricaGauss {

    @Id
    private String id;
    private String nombre;
    private OffsetDateTime dateCreated;
    private List<Maquina> maquinas;

    public Mono<Void> iniciarProduccion() {
        // Lógica para iniciar la producción en cada máquina
        return Mono.when(maquinas.stream()
                .map(Maquina::ensamblarMaquina)
                .toList()
        );
    }

    public Mono<Void> detenerSimulacion() {
        // Lógica para detener todas las máquinas y detener la simulación
        return Mono.when(maquinas.stream()
                .map(Maquina::detenerMaquina)
                .toList()
        );
    }
}
