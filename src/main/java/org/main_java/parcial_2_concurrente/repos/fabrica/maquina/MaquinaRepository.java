package org.main_java.parcial_2_concurrente.repos.fabrica.maquina;

import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MaquinaRepository extends ReactiveMongoRepository<Maquina, String> {
}
