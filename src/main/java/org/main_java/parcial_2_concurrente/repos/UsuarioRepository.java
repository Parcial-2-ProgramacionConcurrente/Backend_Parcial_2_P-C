package org.main_java.parcial_2_concurrente.repos;

import org.main_java.parcial_2_concurrente.domain.Usuario;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UsuarioRepository extends ReactiveMongoRepository<Usuario, String> {
    // Definimos el metodo para buscar un usuario por su correo electrónico.
    Mono<Usuario> findByCorreo(String correo);
}
