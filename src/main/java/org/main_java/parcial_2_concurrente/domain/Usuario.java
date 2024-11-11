package org.main_java.parcial_2_concurrente.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    @Field(name = "nombre")
    private String nombre;

    @Field(name = "apellido1")
    private String apellido1;

    @Field(name = "apellido2")
    private String apellido2;

    @Field(name = "correo")
    private String correo;

    @Field(name = "telefono")
    private Integer telefono;

    @Field(name = "direccion")
    private String direccion;

    // Almacenamos el ID de rol en lugar de una relación directa
    private String rolId;  // ID del Rol relacionado

    // Almacenamos el ID de credenciales en lugar de una relación directa
    private String credencialesId;  // ID de las Credenciales relacionadas
}