package org.main_java.parcial_2_concurrente.domain.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "credenciales")
public class Credenciales {

    @Id
    private String id;

    @Field(name = "username")
    private String username;

    @Field(name = "password")
    private String password;

    // Almacenamos el ID del usuario en lugar de una relación directa
    private String usuarioId;  // ID del Usuario relacionado
}
