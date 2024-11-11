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
    private MaquinaWorker maquinaWorker;
    private GaltonBoard galtonBoard;
    private boolean trabajoCompletado;

    /**
     * Obtiene un valor desde el GaltonBoard basado en la distribución de bolas en los contenedores y otros posibles factores.
     *
     * @param galtonBoard el GaltonBoard que contiene la distribución y configuración de la simulación.
     * @return Mono<Double> el valor obtenido de la distribución.
     */
    public Mono<Double> obtenerValorDesdeGaltonBoard(GaltonBoard galtonBoard) {
        // Acceder a la distribución desde el GaltonBoard
        Map<String, Integer> datos = galtonBoard.getDistribucion().getDatos();

        int totalBolas = datos.values().stream().mapToInt(Integer::intValue).sum();

        if (totalBolas == 0) {
            return Mono.error(new RuntimeException("La distribución no contiene bolas."));
        }

        // Valor promedio basado en la cantidad de bolas en cada contenedor
        double valorPromedio = datos.values().stream()
                .mapToDouble(cantidad -> (double) cantidad / totalBolas)
                .average()
                .orElse(0.0);

        // Escalar el valor promedio, si es necesario
        return Mono.just(valorPromedio * 100);
    }
}
