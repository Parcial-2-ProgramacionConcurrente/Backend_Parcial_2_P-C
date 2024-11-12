package org.main_java.parcial_2_concurrente.model.fabricaDTO;

import lombok.*;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FabricaGaussDTO {

    private String id;
    private String nombre;
    private List<String> maquinasIds;

}
