package org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.MaquinaWorker;
import org.main_java.parcial_2_concurrente.domain.galton.Distribucion;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class ComponenteWorker {

    @Id
    private String id;
    private Componente componente;
    private String maquinaWorkerId;
    private GaltonBoard galtonBoard;
    private boolean trabajoCompletado;

    private boolean ensamblado;

    public ComponenteWorker(String id, Componente componente, String maquinaWorkerId, GaltonBoard galtonBoard, boolean trabajoCompletado) {
        this.id = id;
        this.trabajoCompletado = trabajoCompletado;
        this.maquinaWorkerId = maquinaWorkerId;
        this.galtonBoard = galtonBoard;
        this.componente = componente;
    }

    /**
     * Obtiene un valor desde el GaltonBoard basado en la distribución de bolas en los contenedores.
     *
     * @param galtonBoard el GaltonBoard que contiene la distribución y configuración de la simulación.
     * @param contenedorIndex el índice del contenedor específico para obtener un valor único para cada componente.
     * @return Mono<Double> el valor obtenido de la distribución.
     */
    public Mono<Double> obtenerValorDesdeGaltonBoard(GaltonBoard galtonBoard, int contenedorIndex) {
        // Acceder a la distribución desde el GaltonBoard
        Map<String, Integer> datos = galtonBoard.getDistribucion().getDatos();

        int totalBolas = datos.values().stream().mapToInt(Integer::intValue).sum();

        if (totalBolas == 0) {
            System.err.println("Advertencia: La distribución no contiene bolas.");
            return Mono.just(0.0); // O cualquier otro valor predeterminado.
        }

        // Obtener el valor específico de un contenedor por índice
        String contenedorKey = "contenedor_" + contenedorIndex % datos.size();
        Integer bolasEnContenedor = datos.getOrDefault(contenedorKey, 0);

        // Escalar o manipular el valor de las bolas en el contenedor si es necesario
        double valorEscalado = bolasEnContenedor * 10.0; // Factor de escala
        return Mono.just(valorEscalado);
    }

}
