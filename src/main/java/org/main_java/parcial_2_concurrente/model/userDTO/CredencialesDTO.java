package org.main_java.parcial_2_concurrente.model.userDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class CredencialesDTO {
    private String id;
    private String correo;
    private String password;  // Incluir solo si realmente lo necesitas
    private String usuarioId; // Relación con el usuario en forma de ID
}