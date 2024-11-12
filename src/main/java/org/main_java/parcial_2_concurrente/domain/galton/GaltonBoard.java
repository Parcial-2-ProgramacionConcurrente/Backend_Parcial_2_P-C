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
    private GaltonBoardStatus status;
}
