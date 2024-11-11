package org.main_java.parcial_2_concurrente.domain.fabrica.maquina;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.ComponenteWorker;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class MaquinaWorker {

    @Id
    private String id;
    private List<ComponenteWorker> componenteWorkers;
    private Maquina maquina;
}
