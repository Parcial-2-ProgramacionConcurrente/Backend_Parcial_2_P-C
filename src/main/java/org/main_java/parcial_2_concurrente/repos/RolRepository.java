package org.main_java.parcial_2_concurrente.repos;



import org.main_java.parcial_2_concurrente.domain.Rol;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RolRepository extends ReactiveMongoRepository<Rol, String> {
    Mono<Rol> findByNombre(String nombre);
}
