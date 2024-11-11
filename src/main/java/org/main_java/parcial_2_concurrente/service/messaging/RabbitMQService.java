package org.main_java.parcial_2_concurrente.service.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarMensaje(String cola, String mensaje) {
        rabbitTemplate.convertAndSend(cola, mensaje);
        System.out.println("Mensaje enviado a la cola " + cola + ": " + mensaje);
    }

    public Mono<Void> aplicarBackpressureOnDrop(String cola, String mensaje) {
        return Flux.just(mensaje)
                .doOnNext(msg -> System.out.println("Intentando enviar mensaje: " + msg))
                .doOnNext(msg -> rabbitTemplate.convertAndSend(cola, msg))
                .onBackpressureDrop(droppedMsg -> System.out.println("Mensaje descartado por backpressure: " + droppedMsg))
                .then();
    }

    public Mono<Void> aplicarBackpressureOnBuffer(String cola, String mensaje) {
        return Flux.just(mensaje)
                .doOnNext(msg -> System.out.println("Intentando enviar mensaje: " + msg))
                .doOnNext(msg -> rabbitTemplate.convertAndSend(cola, msg))
                .onBackpressureBuffer() // Aplica un buffer para manejar presión de flujo
                .then();
    }
}
