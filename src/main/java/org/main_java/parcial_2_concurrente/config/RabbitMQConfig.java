package org.main_java.parcial_2_concurrente.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_QUEUE = "componente_notification_queue";
    public static final String PROGRESS_QUEUE = "componente_progress_queue";
    public static final String BOLAS_QUEUE = "queue_bolas";
    public static final String PRODUCCION_QUEUE = "produccion_queue";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue progressQueue() {
        return new Queue(PROGRESS_QUEUE, true);
    }

    @Bean
    public Queue bolasQueue() {
        return new Queue(BOLAS_QUEUE, true);
    }

    @Bean
    public Queue produccionQueue() {
        return new Queue(PRODUCCION_QUEUE, true);
    }

    @Bean
    public MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
