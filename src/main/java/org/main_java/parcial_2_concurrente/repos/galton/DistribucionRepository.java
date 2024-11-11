package org.main_java.parcial_2_concurrente.repos.galton;

import org.main_java.parcial_2_concurrente.domain.galton.Distribucion;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DistribucionRepository extends ReactiveMongoRepository<Distribucion, String> {
}
