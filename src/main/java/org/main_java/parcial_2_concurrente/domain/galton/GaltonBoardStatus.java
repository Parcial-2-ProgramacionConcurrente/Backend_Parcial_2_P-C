package org.main_java.parcial_2_concurrente.domain.galton;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class GaltonBoardStatus {

    @Id
    private String id;
    private String estado;  // Estado del tablero (por ejemplo, "EN_PROGRESO", "FINALIZADO", etc.)
    private Map<String, Integer> distribucionActual;  // Distribución de bolas en cada contenedor

    public GaltonBoardStatus(String estado, Map<String, Integer> distribucionActual) {
        this.estado = estado;
        this.distribucionActual = distribucionActual;
    }

    /**
     * Metodo para actualizar la distribución actual del tablero.
     * Este metodo reemplaza la distribución actual con la nueva distribución.
     *
     * @param nuevaDistribucion la nueva distribución de bolas en contenedores
     */
    public void actualizarDistribucion(Map<String, Integer> nuevaDistribucion) {
        this.distribucionActual = nuevaDistribucion;
    }
}

