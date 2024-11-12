package org.main_java.parcial_2_concurrente.service.messaging;

import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;
    private final MessageListenerContainer messageListenerContainer;
    private final ExecutorService executorService;

    public RabbitMQService(RabbitTemplate rabbitTemplate, MessageListenerContainer messageListenerContainer) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageListenerContainer = messageListenerContainer;
        this.executorService = Executors.newFixedThreadPool(10); // Cambia el tamaño según tus necesidades
    }

    public Mono<Void> enviarMensaje(String queue, String mensaje) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(queue, mensaje);
            System.out.println("Mensaje enviado a la cola " + queue + ": " + mensaje);
        }).doOnError(e -> System.err.println("Error enviando mensaje a RabbitMQ: " + e.getMessage())).then();
    }

    public Mono<String> recibirMensaje(String queue) {
        return Mono.fromCallable(() -> {
            Object message = rabbitTemplate.receiveAndConvert(queue);
            return message != null ? message.toString() : null;
        }).doOnError(e -> System.err.println("Error recibiendo mensaje de RabbitMQ: " + e.getMessage()));
    }

    public void iniciarListener(String queueName, java.util.function.Consumer<String> callback) {
        SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) messageListenerContainer;
        container.addQueueNames(queueName);
        container.setMessageListener(message -> {
            executorService.submit(() -> { // Procesa el mensaje en un hilo separado
                String mensaje = new String(message.getBody());
                callback.accept(mensaje);
                System.out.println("Mensaje recibido de la cola " + queueName + ": " + mensaje);
            });
        });
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("Cerrando ExecutorService...");
        executorService.shutdown();
    }
}
