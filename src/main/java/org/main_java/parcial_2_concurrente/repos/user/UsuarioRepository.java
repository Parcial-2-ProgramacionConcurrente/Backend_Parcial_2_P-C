package org.main_java.parcial_2_concurrente.repos.user;

import org.main_java.parcial_2_concurrente.domain.user.Usuario;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UsuarioRepository extends ReactiveMongoRepository<Usuario, String> {
    // Definimos el metodo para buscar un usuario por su correo electrónico.
    Mono<Usuario> findByCorreo(String correo);
}
