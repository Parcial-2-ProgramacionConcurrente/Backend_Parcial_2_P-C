package org.main_java.parcial_2_concurrente;

import org.main_java.parcial_2_concurrente.domain.user.Rol;
import org.main_java.parcial_2_concurrente.model.userDTO.RegisterRequestDTO;
import org.main_java.parcial_2_concurrente.repos.user.RolRepository;
import org.main_java.parcial_2_concurrente.service.fabricaService.FabricaGaussService;
import org.main_java.parcial_2_concurrente.service.userService.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableAspectJAutoProxy
public class Backend_Parcial_2 implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private FabricaGaussService fabricaGaussService;

    public static void main(String[] args) {
        SpringApplication.run(Backend_Parcial_2.class, args);
    }

    private Mono<Void> initRoles() {
        Rol adminRole = new Rol("admin");
        Rol userRole = new Rol("user");

        return Mono.zip(
                        rolRepository.findByNombre(adminRole.getNombre()).switchIfEmpty(rolRepository.save(adminRole)),
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
                .then(fabricaGaussService.iniciarProduccionCompleta()) // Llamada a iniciarProduccionCompleta
                .subscribe(
                        unused -> System.out.println("Inicialización completa y producción iniciada"),
                        error -> System.err.println("Error en la inicialización o producción: " + error.getMessage())
                );
    }
}
