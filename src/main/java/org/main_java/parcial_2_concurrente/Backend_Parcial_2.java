package org.main_java.parcial_2_concurrente;

import org.main_java.parcial_2_concurrente.domain.Rol;
import org.main_java.parcial_2_concurrente.model.RegisterRequestDTO;
import org.main_java.parcial_2_concurrente.repos.RolRepository;
import org.main_java.parcial_2_concurrente.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;

import java.util.*;

@SpringBootApplication
public class Backend_Parcial_2 implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private AuthService authService;


    public static void main(String[] args) {
        SpringApplication.run(Backend_Parcial_2.class, args);
    }

    private Mono<Void> initRoles() {
        Rol adminRole = new Rol("admin");
        Rol researcherRole = new Rol("paleontologist");
        Rol userRole = new Rol("user");

        return Mono.zip(
                        rolRepository.findByNombre(adminRole.getNombre()).switchIfEmpty(rolRepository.save(adminRole)),
                        rolRepository.findByNombre(researcherRole.getNombre()).switchIfEmpty(rolRepository.save(researcherRole)),
                        rolRepository.findByNombre(userRole.getNombre()).switchIfEmpty(rolRepository.save(userRole))
                )
                .doOnSuccess(result -> System.out.println("Roles initialized"))
                .doOnError(error -> System.err.println("Error initializing roles: " + error.getMessage()))
                .then();
    }

    private Mono<Void> initUsers() {
        return Mono.when(
                        registrarNuevoUsuario(
                                authService,
                                "Paleontologo", "ApellidoAA", "ApellidoBB", "paleontologist@gmail.com", 987654321,
                                "Calle Secundaria 456", "a12345_678", "paleontologist"
                        ).doOnError(error -> System.err.println("Error registrando Paleontologo: " + error.getMessage())),

                        registrarNuevoUsuario(
                                authService,
                                "Administrador", "ApellidoA", "ApellidoB", "admin@gmail.com", 123456789,
                                "Calle Principal 123", "a12345_67", "admin"
                        ).doOnError(error -> System.err.println("Error registrando Administrador: " + error.getMessage())),

                        registrarNuevoUsuario(
                                authService,
                                "Usuario", "ApellidoCC", "ApellidoDD", "usuario@gmail.com", 555666777,
                                "Avenida Tercera 789", "a12345_679", "user"
                        ).doOnError(error -> System.err.println("Error registrando Usuario: " + error.getMessage()))
                )
                .then()
                .doOnSuccess(unused -> System.out.println("Todos los usuarios han sido registrados"));
    }


    private Mono<Void> registrarNuevoUsuario(AuthService authService, String nombre, String apellido1, String apellido2,
                                             String correo, int telefono, String direccion, String contrasena, String rolNombre) {

        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setNombre(nombre);
        registerRequest.setApellido1(apellido1);
        registerRequest.setApellido2(apellido2);
        registerRequest.setCorreo(correo);
        registerRequest.setTelefono(telefono);
        registerRequest.setDireccion(direccion);
        registerRequest.setPassword(contrasena);
        registerRequest.setRolNombre(rolNombre);

        return authService.register(registerRequest)
                .doOnSuccess(response -> System.out.println("Usuario registrado con nombre: " + nombre + " y rol: " + rolNombre))
                .then();
    }




    @Override
    public void run(String... args) throws Exception {

        initRoles()
                .then(initUsers())
                .subscribe(
                        unused -> System.out.println("Inicialización completa"),
                        error -> System.err.println("Error en la inicialización: " + error.getMessage())
                );
    }
}
