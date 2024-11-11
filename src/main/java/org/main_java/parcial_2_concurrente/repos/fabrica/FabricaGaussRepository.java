package org.main_java.parcial_2_concurrente.repos.fabrica;

import org.main_java.parcial_2_concurrente.domain.fabrica.FabricaGauss;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FabricaGaussRepository extends ReactiveMongoRepository<FabricaGauss, String> {
}
