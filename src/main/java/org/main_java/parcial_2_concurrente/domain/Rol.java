package org.main_java.parcial_2_concurrente.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Document(collection = "roles")
public class Rol {

    @Id
    private String id;

    @Field(name = "nombre")
    private String nombre;

    private Set<String> usuarios;

    // Constructor adicional para nombre solamente, inicializa usuarios como un conjunto vacío
    public Rol(String nombre) {
        this.nombre = nombre;
        this.usuarios = new HashSet<>();
    }
}