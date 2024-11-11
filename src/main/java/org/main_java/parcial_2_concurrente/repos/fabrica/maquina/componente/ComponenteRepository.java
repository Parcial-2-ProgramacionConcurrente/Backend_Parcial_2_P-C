package org.main_java.parcial_2_concurrente.repos.fabrica.maquina.componente;

import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ComponenteRepository extends ReactiveMongoRepository <Componente, String> {

}
