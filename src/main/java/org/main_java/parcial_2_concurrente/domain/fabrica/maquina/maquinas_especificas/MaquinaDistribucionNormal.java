package org.main_java.parcial_2_concurrente.domain.fabrica.maquina.maquinas_especificas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "maquinas")
public class MaquinaDistribucionNormal extends Maquina {

    private double media;
    private double desviacionEstandar;
    private int maximoValor;

}