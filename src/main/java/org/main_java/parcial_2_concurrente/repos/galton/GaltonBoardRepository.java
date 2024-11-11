package org.main_java.parcial_2_concurrente.repos.galton;

import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface GaltonBoardRepository extends ReactiveMongoRepository<GaltonBoard, String> {

    /**
     * Encuentra un GaltonBoard por el ID de la fábrica asociada.
     *
     * @param fabricaId ID de la fábrica.
     * @return Mono<GaltonBoard> el GaltonBoard asociado a la fábrica, si existe.
     */
    Mono<GaltonBoard> findByFabricaId(String fabricaId);

}
