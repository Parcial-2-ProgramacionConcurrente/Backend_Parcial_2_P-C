package org.main_java.parcial_2_concurrente.model.userDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class UsuarioDTO {

    private String id;

    private String nombre;

    private String apellido1;

    private String apellido2;

    private String correo;

    private Integer telefono;

    private String direccion;

    // Almacenamos el ID de rol en lugar de una relación directa
    private String rolId;  // ID del Rol relacionado

    // Almacenamos el ID de credenciales en lugar de una relación directa
    private String credencialesId;  // ID de las Credenciales relacionadas
}