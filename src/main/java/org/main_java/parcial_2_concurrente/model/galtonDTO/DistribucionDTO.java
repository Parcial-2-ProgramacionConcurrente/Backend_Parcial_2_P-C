package org.main_java.parcial_2_concurrente.model.galtonDTO;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DistribucionDTO {

    private Map<String, Integer> datos = new HashMap<>();
    private int numBolas;
    private Map<String, Integer> contenedores;
}


