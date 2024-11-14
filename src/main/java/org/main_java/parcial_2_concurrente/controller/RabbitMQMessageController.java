package org.main_java.parcial_2_concurrente.controller;

import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/messages")
public class RabbitMQMessageController {

    private final RabbitMQService rabbitMQService;

    @Autowired
    public RabbitMQMessageController(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

    // Endpoint para obtener todos los mensajes recibidos de RabbitMQ
    @GetMapping(value = "/recibidos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getReceivedMessages() {
        return rabbitMQService.obtenerMensajesRecibidos();
    }
}
