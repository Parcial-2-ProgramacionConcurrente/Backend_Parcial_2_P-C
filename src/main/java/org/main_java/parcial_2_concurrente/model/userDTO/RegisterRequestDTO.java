package org.main_java.parcial_2_concurrente.model.userDTO;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequestDTO {
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String correo;
    private int telefono;
    private String direccion;
    private String password;
    private String rolNombre; // Cambiado a rolNombre
}