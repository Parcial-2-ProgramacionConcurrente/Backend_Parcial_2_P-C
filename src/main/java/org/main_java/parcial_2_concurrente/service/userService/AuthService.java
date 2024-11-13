// src/main/java/org/main_java/parcial_2_concurrente/service/userService/AuthService.java
package org.main_java.parcial_2_concurrente.service.userService;

import org.main_java.parcial_2_concurrente.domain.user.Credenciales;
import org.main_java.parcial_2_concurrente.domain.user.Usuario;
import org.main_java.parcial_2_concurrente.model.userDTO.AuthResponseDTO;
import org.main_java.parcial_2_concurrente.model.userDTO.LoginRequestDTO;
import org.main_java.parcial_2_concurrente.model.userDTO.RegisterRequestDTO;
import org.main_java.parcial_2_concurrente.repos.user.CredencialesRepository;
import org.main_java.parcial_2_concurrente.repos.user.RolRepository;
import org.main_java.parcial_2_concurrente.repos.user.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CredencialesRepository credencialesRepository;
    @Autowired
    private RolRepository rolRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Mono<ResponseEntity<AuthResponseDTO>> login(LoginRequestDTO loginRequest) {
        System.out.println("Intentando iniciar sesión con correo: " + loginRequest.getCorreo());

        return usuarioRepository.findByCorreo(loginRequest.getCorreo())
                .flatMap(usuario -> {
                    System.out.println("Usuario encontrado: " + usuario.getCorreo());
                    return credencialesRepository.findById(usuario.getCredencialesId())
                            .flatMap(credenciales -> {
                                if (passwordEncoder.matches(loginRequest.getPassword(), credenciales.getPassword())) {
                                    System.out.println("Contraseña correcta para el usuario: " + usuario.getCorreo());
                                    return rolRepository.findById(usuario.getRolId())
                                            .flatMap(rol -> {
                                                // Cambiamos para devolver el nombre del rol en lugar del ID
                                                return Mono.just(ResponseEntity.ok(new AuthResponseDTO(
                                                        "Login exitoso", "FAKE_JWT_TOKEN", rol.getNombre())));
                                            });
                                } else {
                                    System.out.println("Contraseña incorrecta para el usuario: " + usuario.getCorreo());
                                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                            .body(new AuthResponseDTO("Credenciales incorrectas", null, null)));
                                }
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new AuthResponseDTO("Usuario no encontrado", null, null))));
    }


    public Mono<ResponseEntity<AuthResponseDTO>> register(RegisterRequestDTO registerRequest) {
        System.out.println("Intentando registrar nuevo usuario con correo: " + registerRequest.getCorreo());

        return usuarioRepository.findByCorreo(registerRequest.getCorreo())
                .flatMap(existingUser -> {
                    System.out.println("El usuario ya existe con el correo: " + registerRequest.getCorreo());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new AuthResponseDTO("El usuario ya existe", null, null)));
                })
                .switchIfEmpty(
                        rolRepository.findByNombre(registerRequest.getRolNombre())
                                .flatMap(rol -> {
                                    System.out.println("Rol encontrado para el registro: " + rol.getNombre());
                                    Credenciales credenciales = new Credenciales();
                                    credenciales.setUsername(registerRequest.getCorreo());
                                    credenciales.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

                                    return credencialesRepository.save(credenciales)
                                            .flatMap(savedCredenciales -> {
                                                Usuario nuevoUsuario = new Usuario();
                                                nuevoUsuario.setNombre(registerRequest.getNombre());
                                                nuevoUsuario.setApellido1(registerRequest.getApellido1());
                                                nuevoUsuario.setApellido2(registerRequest.getApellido2());
                                                nuevoUsuario.setCorreo(registerRequest.getCorreo());
                                                nuevoUsuario.setTelefono(registerRequest.getTelefono());
                                                nuevoUsuario.setDireccion(registerRequest.getDireccion());
                                                nuevoUsuario.setCredencialesId(savedCredenciales.getId());
                                                nuevoUsuario.setRolId(rol.getId());

                                                System.out.println("Usuario guardado con correo: " + registerRequest.getCorreo());
                                                return usuarioRepository.save(nuevoUsuario)
                                                        .map(savedUsuario -> {
                                                            System.out.println("Registro exitoso para el usuario: " + savedUsuario.getCorreo());
                                                            return ResponseEntity.ok(
                                                                    new AuthResponseDTO("Usuario registrado con éxito", "FAKE_JWT_TOKEN", rol.getNombre()));
                                                        });
                                            });
                                })
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Rol no válido.")))
                );
    }
}
