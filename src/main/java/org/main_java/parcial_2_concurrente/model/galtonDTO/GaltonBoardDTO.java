package org.main_java.parcial_2_concurrente.model.galtonDTO;

import lombok.*;
import org.main_java.parcial_2_concurrente.domain.galton.Distribucion;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GaltonBoardDTO {

    private String id;
    private int numBolas;
    private int numContenedores;
    private String estado;
    private Distribucion distribucion;
    private String fabricaId;
}

