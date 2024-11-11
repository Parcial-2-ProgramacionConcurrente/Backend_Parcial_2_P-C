package org.main_java.parcial_2_concurrente.repos.user;

import org.main_java.parcial_2_concurrente.domain.user.Credenciales;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CredencialesRepository extends ReactiveMongoRepository<Credenciales, String> {
}
