package org.main_java.parcial_2_concurrente.repos.galton;

import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface GaltonBoardRepository extends ReactiveMongoRepository<GaltonBoard, String> {
}
