package org.main_java.parcial_2_concurrente.repos.fabrica.maquina;

import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.MaquinaWorker;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MaquinaWorkerRepository  extends ReactiveMongoRepository <MaquinaWorker, String> {
}
