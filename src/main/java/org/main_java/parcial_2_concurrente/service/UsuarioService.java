package org.main_java.parcial_2_concurrente.service;

import org.main_java.parcial_2_concurrente.model.UsuarioDTO;
import org.main_java.parcial_2_concurrente.repos.UsuarioRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UsuarioService {

    // Inyectamos el repositorio de usuarios
    private final UsuarioRepository usuarioRepository;
    // Creamos pools de hilos para tareas específicas
    private final ExecutorService executorService;
    private final ExecutorService executorServiceDelete;
    private final ExecutorService executorServiceCreate;
    private final ExecutorService executorServiceUpdate;
    private final ExecutorService executorServiceGetById;

    // Inyectamos el repositorio de usuarios y creamos los pools de hilos
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.executorService = Executors.newFixedThreadPool(50);
        this.executorServiceDelete = Executors.newFixedThreadPool(50);
        this.executorServiceCreate = Executors.newFixedThreadPool(50);
        this.executorServiceUpdate = Executors.newFixedThreadPool(50);
        this.executorServiceGetById = Executors.newFixedThreadPool(50);
    }

    // Metodo para obtener todos los usuarios
    public Flux<Usuario> getAll() {
        return usuarioRepository.findAll()
                .subscribeOn(Schedulers.fromExecutor(executorService));
    }

    // Metodo para obtener un usuario por su id
    public Mono<Usuario> getById(String id) {
        return usuarioRepository.findById(id)
                .subscribeOn(Schedulers.fromExecutor(executorServiceGetById));
    }

    // Metodo para crear un usuario a partir de un DTO
    public Mono<UsuarioDTO> create(UsuarioDTO usuarioDTO) {
        return mapToEntity(usuarioDTO)
                .flatMap(usuario -> usuarioRepository.save(usuario))
                .subscribeOn(Schedulers.fromExecutor(executorServiceCreate))
                .flatMap(this::mapToDTO);
    }

    // Metodo para actualizar un usuario a partir de un DTO
    public Mono<UsuarioDTO> update(String id, UsuarioDTO usuarioActualizadoDTO) {
        return usuarioRepository.findById(id)
                .flatMap(usuarioExistente -> mapToEntity(usuarioActualizadoDTO)
                        .map(usuarioActualizado -> {
                            // Actualizar los campos necesarios
                            usuarioExistente.setNombre(usuarioActualizado.getNombre());
                            usuarioExistente.setApellido1(usuarioActualizado.getApellido1());
                            usuarioExistente.setApellido2(usuarioActualizado.getApellido2());
                            usuarioExistente.setCorreo(usuarioActualizado.getCorreo());
                            usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
                            usuarioExistente.setDireccion(usuarioActualizado.getDireccion());
                            usuarioExistente.setRolId(usuarioActualizado.getRolId());
                            usuarioExistente.setCredencialesId(usuarioActualizado.getCredencialesId());
                            return usuarioExistente;
                        })
                )
                .flatMap(usuario -> usuarioRepository.save(usuario))
                .subscribeOn(Schedulers.fromExecutor(executorServiceUpdate))
                .flatMap(this::mapToDTO);
    }

    // Metodo para eliminar un usuario
    public Mono<Void> delete(String id) {
        return Mono.fromRunnable(() -> usuarioRepository.deleteById(id))
                .subscribeOn(Schedulers.fromExecutor(executorServiceDelete))
                .then();
    }

    // Metodo para mapear una entidad a un DTO
    public Mono<UsuarioDTO> mapToDTO(Usuario usuario) {
        return Mono.fromCallable(() -> {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(usuario.getId());
            dto.setNombre(usuario.getNombre());
            dto.setApellido1(usuario.getApellido1());
            dto.setApellido2(usuario.getApellido2());
            dto.setCorreo(usuario.getCorreo());
            dto.setTelefono(usuario.getTelefono());
            dto.setDireccion(usuario.getDireccion());
            dto.setRolId(usuario.getRolId());
            dto.setCredencialesId(usuario.getCredencialesId());
            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Metodo para mapear un DTO a una entidad
    public Mono<Usuario> mapToEntity(UsuarioDTO dto) {
        return Mono.fromCallable(() -> {
            Usuario usuario = new Usuario();
            usuario.setId(dto.getId());
            usuario.setNombre(dto.getNombre());
            usuario.setApellido1(dto.getApellido1());
            usuario.setApellido2(dto.getApellido2());
            usuario.setCorreo(dto.getCorreo());
            usuario.setTelefono(dto.getTelefono());
            usuario.setDireccion(dto.getDireccion());
            usuario.setRolId(dto.getRolId());
            usuario.setCredencialesId(dto.getCredencialesId());
            return usuario;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
