package org.main_java.parcial_2_concurrente.model.fabricaDTO.maquinaDTO;


import lombok.*;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MaquinaDTO {

    private String id;
    private String tipo;
    private int numeroComponentesRequeridos;
    private List<Componente> componentes;
    private Map<String, Integer> distribucion;
    private String estado;

}

