package org.main_java.parcial_2_concurrente.repos;

import org.main_java.parcial_2_concurrente.domain.Credenciales;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CredencialesRepository extends ReactiveMongoRepository<Credenciales, String> {
}
