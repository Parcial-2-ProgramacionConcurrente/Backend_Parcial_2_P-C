package org.main_java.parcial_2_concurrente.repos.rabbitMQ;

import org.main_java.parcial_2_concurrente.domain.rabbitMQ.MensajeRabbit;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRabbitRepository extends ReactiveCrudRepository<MensajeRabbit, String> {
}
