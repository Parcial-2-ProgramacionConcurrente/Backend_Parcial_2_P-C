package org.main_java.parcial_2_concurrente.domain.rabbitMQ;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "mensajes_rabbit")
public class MensajeRabbit {

    @Id
    private String id;
    private String contenido;
    private String fechaHora;

    public MensajeRabbit() {}

    public MensajeRabbit(String contenido, String fechaHora) {
        this.contenido = contenido;
        this.fechaHora = fechaHora;
    }
}

