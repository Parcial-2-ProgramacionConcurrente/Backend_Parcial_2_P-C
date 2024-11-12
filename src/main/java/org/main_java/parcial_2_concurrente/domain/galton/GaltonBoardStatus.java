package org.main_java.parcial_2_concurrente.domain.galton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class GaltonBoardStatus {

    @Id
    private String id;
    private String estado = "EN_PROGRESO"; // Estado predeterminado para evitar null
    private Map<String, Integer> distribucionActual = new HashMap<>(); // Inicializar para evitar null

    public GaltonBoardStatus(String estado, Map<String, Integer> distribucionActual) {
        this.estado = estado != null ? estado : "EN_PROGRESO"; // Asigna un valor por defecto si es null
        this.distribucionActual = distribucionActual != null ? distribucionActual : new HashMap<>();
    }

    /**
     * Metodo para actualizar la distribución actual del tablero.
     * Este metodo reemplaza la distribución actual con la nueva distribución.
     *
     * @param nuevaDistribucion la nueva distribución de bolas en contenedores
     */
    public void actualizarDistribucion(Map<String, Integer> nuevaDistribucion) {
        if (nuevaDistribucion != null) {
            this.distribucionActual = nuevaDistribucion;
        } else {
            System.err.println("La nueva distribución es nula y no puede actualizarse.");
        }
    }
}
