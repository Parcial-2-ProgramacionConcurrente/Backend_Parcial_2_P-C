package org.main_java.parcial_2_concurrente;

import org.main_java.parcial_2_concurrente.domain.Rol;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.Posicion;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.carnivoro.CarnivoroAcuatico;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.carnivoro.CarnivoroTerrestre;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.carnivoro.CarnivoroVolador;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.herbivoro.HerbivoroAcuatico;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.herbivoro.HerbivoroTerrestre;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.herbivoro.HerbivoroVolador;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.omnivoro.OmnivoroAcuatico;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.omnivoro.OmnivoroTerrestre;
import org.main_java.parcial_2_concurrente.domain.dinosaurios.omnivoro.OmnivoroVolador;
import org.main_java.parcial_2_concurrente.domain.islas.Isla;
import org.main_java.parcial_2_concurrente.domain.islas.IslaAcuatica;
import org.main_java.parcial_2_concurrente.domain.islas.criaderos.CriaderoAcuatico;
import org.main_java.parcial_2_concurrente.domain.islas.criaderos.CriaderoTerrestre;
import org.main_java.parcial_2_concurrente.domain.islas.criaderos.CriaderoVoladores;
import org.main_java.parcial_2_concurrente.domain.sensores.Sensor;
import org.main_java.parcial_2_concurrente.domain.sensores.SensorFrecuenciaCardiaca;
import org.main_java.parcial_2_concurrente.domain.sensores.SensorMovimiento;
import org.main_java.parcial_2_concurrente.domain.sensores.SensorTemperatura;
import org.main_java.parcial_2_concurrente.model.RegisterRequestDTO;
import org.main_java.parcial_2_concurrente.model.dinosauriosDTO.DinosaurioDTO;
import org.main_java.parcial_2_concurrente.model.dinosauriosDTO.omnivoro.OmnivoroAcuaticoDTO;
import org.main_java.parcial_2_concurrente.model.islasDTO.criaderos.CriaderoAcuaticoDTO;
import org.main_java.parcial_2_concurrente.model.islasDTO.criaderos.CriaderoTerrestreDTO;
import org.main_java.parcial_2_concurrente.model.islasDTO.criaderos.CriaderoVoladoresDTO;
import org.main_java.parcial_2_concurrente.repos.RolRepository;
import org.main_java.parcial_2_concurrente.service.AuthService;
import org.main_java.parcial_2_concurrente.service.DinosaurioService;
import org.main_java.parcial_2_concurrente.service.IslaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;

import java.util.*;

@SpringBootApplication
public class Backend_Parcial_2 implements CommandLineRunner {

    @Autowired
    private IslaService islaService;
    @Autowired
    private DinosaurioService dinosaurioService;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private AuthService authService;


    public static void main(String[] args) {
        SpringApplication.run(Backend_Sistema_Monitoreo_Jurassic_main.class, args);
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

        // Crear y registrar cada tipo de isla y criadero
        // Isla Acuática
        IslaAcuaticaDTO islaAcuaticaDTO = new IslaAcuaticaDTO(
                "1", "Isla Acuática", 50, 100, new int[10][10], new ArrayList<>()
        );
        islaAcuaticaDTO.setPermiteAcuaticos(true);
        islaService.create(islaAcuaticaDTO).subscribe();

        // Isla Terrestre Aérea
        IslaTerrestreAereaDTO islaTerrestreAereaDTO = new IslaTerrestreAereaDTO(
                "2", "Isla Terrestre Aérea", 50, 225, new int[15][15], new ArrayList<>()
        );
        islaTerrestreAereaDTO.setPermiteTerrestres(true);
        islaTerrestreAereaDTO.setPermiteVoladores(true);
        islaService.create(islaTerrestreAereaDTO).subscribe();

        // Enfermería
        EnfermeriaDTO enfermeriaDTO = new EnfermeriaDTO(
                "3", "Enfermería", 1, 100, new int[10][10], new ArrayList<>()
        );
        islaService.create(enfermeriaDTO).subscribe();

        // Criadero Acuático
        CriaderoAcuaticoDTO criaderoAcuaticoDTO = new CriaderoAcuaticoDTO(
                "4", "Criadero Acuático", 50, 64, new int[8][8], new ArrayList<>()
        );
        islaService.create(criaderoAcuaticoDTO).subscribe();

        // Criadero Terrestre
        CriaderoTerrestreDTO criaderoTerrestreDTO = new CriaderoTerrestreDTO(
                "5", "Criadero Terrestre", 50, 64, new int[8][8], new ArrayList<>()
        );
        islaService.create(criaderoTerrestreDTO).subscribe();

        // Criadero Voladores
        CriaderoVoladoresDTO criaderoVoladoresDTO = new CriaderoVoladoresDTO(
                "6", "Criadero Voladores", 50, 64, new int[8][8], new ArrayList<>()
        );
        islaService.create(criaderoVoladoresDTO).subscribe();

        // Confirmación de creación
        System.out.println("Todas las islas y criaderos han sido creados y registrados.");

        //------------------------------------------

        CriaderoAcuatico criaderoAcuatico = (CriaderoAcuatico) islaService.mapToEntity(criaderoAcuaticoDTO).block();
        assert criaderoAcuatico != null;
        System.out.println("Isla obtenida: " + criaderoAcuatico.getNombre());

        Random random = new Random();

        // Crear y agregar un dinosaurio carnívoro acuático
        String id = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        String id5 = UUID.randomUUID().toString();
        String id6 = UUID.randomUUID().toString();
        String id7 = UUID.randomUUID().toString();
        String id8 = UUID.randomUUID().toString();
        int edad = 0;

        // Creación de sensores
        SensorFrecuenciaCardiaca sensorFrecuencia = new SensorFrecuenciaCardiaca(
                UUID.randomUUID().toString(), "FrecuenciaCardiaca", 50.0, 100.0, 75.0
        );

        SensorMovimiento sensorMovimiento = new SensorMovimiento(
                UUID.randomUUID().toString(), "Movimiento", 0.0, 1.0, 0.0
        );

        SensorTemperatura sensorTemperatura = new SensorTemperatura(
                UUID.randomUUID().toString(), "Temperatura", 30.0, 40.0, 35.0
        );

        // Crear una lista de sensores y añadir los sensores creados
        List<Sensor> sensores = new ArrayList<>();
        sensores.add(sensorFrecuencia);
        sensores.add(sensorMovimiento);
        sensores.add(sensorTemperatura);

        // Creación del dinosaurio con los sensores
        CarnivoroAcuatico carnivoroAcuatico = new CarnivoroAcuatico(
                id,
                "carnivoro",
                edad,
                "acuatico",
                sensores,
                new Posicion(random.nextInt(8), random.nextInt(8), "acuatico"),
                criaderoAcuatico.getId()
        );

        // Mapear el dinosaurio a DTO y agregarlo a la isla
        dinosaurioService.mapToDTO(carnivoroAcuatico)
                .flatMap(carnivoroAcuaticoDTO -> islaService.agregarDinosaurioIsla(criaderoAcuatico, carnivoroAcuaticoDTO, carnivoroAcuatico.getPosicion()))
                .doOnSuccess(unused -> System.out.println("Dinosaurio agregado exitosamente."))
                .block(); // Usar block() para esperar la finalización

        System.out.println("Todos los dinosaurios han sido creados y agregados a las islas.");
        System.out.println("EL tablero del criadero acuático es: ");
        System.out.println(Arrays.deepToString(criaderoAcuatico.getTablero()));

        //------------------------------------------

        CriaderoTerrestre criaderoTerrestre = (CriaderoTerrestre) islaService.mapToEntity(criaderoTerrestreDTO).block();
        assert criaderoTerrestre != null;
        System.out.println("Isla obtenida: " + criaderoTerrestre.getNombre());

        // Creación de sensores
        SensorFrecuenciaCardiaca sensorFrecuencia2 = new SensorFrecuenciaCardiaca(
                UUID.randomUUID().toString(), "FrecuenciaCardiaca", 50.0, 100.0, 75.0
        );

        SensorMovimiento sensorMovimiento2 = new SensorMovimiento(
                UUID.randomUUID().toString(), "Movimiento", 0.0, 1.0, 0.0
        );

        SensorTemperatura sensorTemperatura2 = new SensorTemperatura(
                UUID.randomUUID().toString(), "Temperatura", 30.0, 40.0, 35.0
        );

        // Crear una lista de sensores y añadir los sensores creados
        List<Sensor> sensores2 = new ArrayList<>();
        sensores2.add(sensorFrecuencia2);
        sensores2.add(sensorMovimiento2);
        sensores2.add(sensorTemperatura2);

        // Creación del dinosaurio con los sensores
        HerbivoroTerrestre herbivoroTerrestre = new HerbivoroTerrestre(
                id2,
                "herbivoro",
                edad,
                "terrestre",
                sensores2,
                new Posicion(random.nextInt(8), random.nextInt(8), "criadero-terrestre"),
                criaderoTerrestre.getId()
        );

        // Mapear el dinosaurio a DTO y agregarlo a la isla
        dinosaurioService.mapToDTO(herbivoroTerrestre)
                .flatMap(herbivoroTerrestreDTO -> islaService.agregarDinosaurioIsla(criaderoTerrestre, herbivoroTerrestreDTO, herbivoroTerrestre.getPosicion()))
                .doOnSuccess(unused -> System.out.println("Dinosaurio agregado exitosamente."))
                .block(); // Usar block() para esperar la finalización

        System.out.println("Todos los dinosaurios han sido creados y agregados a las islas.");
        System.out.println("EL tablero del criadero terrestre es: ");
        System.out.println(Arrays.deepToString(criaderoTerrestre.getTablero()));

        //------------------------------------------

        CriaderoTerrestre criaderoTerrestre2 = (CriaderoTerrestre) islaService.mapToEntity(criaderoTerrestreDTO).block();
        assert criaderoTerrestre2 != null;
        System.out.println("Isla obtenida: " + criaderoTerrestre2.getNombre());

        // Creación de sensores
        SensorFrecuenciaCardiaca sensorFrecuencia3 = new SensorFrecuenciaCardiaca(
                UUID.randomUUID().toString(), "FrecuenciaCardiaca", 50.0, 100.0, 75.0
        );

        SensorMovimiento sensorMovimiento3 = new SensorMovimiento(
                UUID.randomUUID().toString(), "Movimiento", 0.0, 1.0, 0.0
        );

        SensorTemperatura sensorTemperatura3 = new SensorTemperatura(
                UUID.randomUUID().toString(), "Temperatura", 30.0, 40.0, 35.0
        );

        // Crear una lista de sensores y añadir los sensores creados
        List<Sensor> sensores3 = new ArrayList<>();
        sensores3.add(sensorFrecuencia3);
        sensores3.add(sensorMovimiento3);
        sensores3.add(sensorTemperatura3);

        // Creación del dinosaurio con los sensores
        OmnivoroTerrestre omnivoroTerrestre = new OmnivoroTerrestre(
                id3,
                "omnivoro",
                edad,
                "terrestre",
                sensores3,
                new Posicion(random.nextInt(8), random.nextInt(8), "criadero-terrestre"),
                criaderoTerrestre2.getId()
        );

        // Mapear el dinosaurio a DTO y agregarlo a la isla
        dinosaurioService.mapToDTO(omnivoroTerrestre)
                .flatMap(omnivoroTerrestreDTO -> islaService.agregarDinosaurioIsla(criaderoTerrestre2, omnivoroTerrestreDTO, omnivoroTerrestre.getPosicion()))
                .doOnSuccess(unused -> System.out.println("Dinosaurio agregado exitosamente."))
                .block(); // Usar block() para esperar la finalización

        System.out.println("Todos los dinosaurios han sido creados y agregados a las islas.");
        System.out.println("EL tablero del criadero terrestre es: ");
        System.out.println(Arrays.deepToString(criaderoTerrestre2.getTablero()));

        //------------------------------------------

        CriaderoVoladores criaderoVolador = (CriaderoVoladores) islaService.mapToEntity(criaderoVoladoresDTO).block();
        assert criaderoVolador != null;
        System.out.println("Isla obtenida: " + criaderoVolador.getNombre());

        // Creación de sensores
        SensorFrecuenciaCardiaca sensorFrecuencia4 = new SensorFrecuenciaCardiaca(
                UUID.randomUUID().toString(), "FrecuenciaCardiaca", 50.0, 100.0, 75.0
        );

        SensorMovimiento sensorMovimiento4 = new SensorMovimiento(
                UUID.randomUUID().toString(), "Movimiento", 0.0, 1.0, 0.0
        );

        SensorTemperatura sensorTemperatura4 = new SensorTemperatura(
                UUID.randomUUID().toString(), "Temperatura", 30.0, 40.0, 35.0
        );

        // Crear una lista de sensores y añadir los sensores creados
        List<Sensor> sensores4 = new ArrayList<>();
        sensores4.add(sensorFrecuencia4);
        sensores4.add(sensorMovimiento4);
        sensores4.add(sensorTemperatura4);

        // Creación del dinosaurio con los sensores
        OmnivoroVolador omnivoroVolador = new OmnivoroVolador(
                id4,
                "omnivoro",
                edad,
                "volador",
                sensores4,
                new Posicion(random.nextInt(8), random.nextInt(8), "criadero-volador"),
                criaderoVolador.getId()
        );

        // Mapear el dinosaurio a DTO y agregarlo a la isla
        dinosaurioService.mapToDTO(omnivoroVolador)
                .flatMap(omnivoroVoladorDTO -> islaService.agregarDinosaurioIsla(criaderoVolador, omnivoroVoladorDTO, omnivoroVolador.getPosicion()))
                .doOnSuccess(unused -> System.out.println("Dinosaurio agregado exitosamente."))
                .block(); // Usar block() para esperar la finalización

        System.out.println("Todos los dinosaurios han sido creados y agregados a las islas.");
        System.out.println("EL tablero del criadero de voladores es: ");
        System.out.println(Arrays.deepToString(criaderoVolador.getTablero()));

        //------------------------------------------

        CriaderoAcuatico criaderoAcuatico1 = (CriaderoAcuatico) islaService.mapToEntity(criaderoAcuaticoDTO).block();
        assert criaderoAcuatico1 != null;
        System.out.println("Isla obtenida: " + criaderoAcuatico1.getNombre());

        // Creación de sensores
        SensorFrecuenciaCardiaca sensorFrecuencia5 = new SensorFrecuenciaCardiaca(
                UUID.randomUUID().toString(), "FrecuenciaCardiaca", 50.0, 100.0, 75.0
        );

        SensorMovimiento sensorMovimiento5 = new SensorMovimiento(
                UUID.randomUUID().toString(), "Movimiento", 0.0, 1.0, 0.0
        );

        SensorTemperatura sensorTemperatura5 = new SensorTemperatura(
                UUID.randomUUID().toString(), "Temperatura", 30.0, 40.0, 35.0
        );

        // Crear una lista de sensores y añadir los sensores creados
        List<Sensor> sensores5 = new ArrayList<>();
        sensores5.add(sensorFrecuencia5);
        sensores5.add(sensorMovimiento5);
        sensores5.add(sensorTemperatura5);

        // Creación del dinosaurio con los sensores
        HerbivoroAcuatico  herbivoroAcuatico = new HerbivoroAcuatico(
                id5,
                "herbivoro",
                edad,
                "acuatico",
                sensores5,
                new Posicion(random.nextInt(8), random.nextInt(8), "criadero-acuatico"),
                criaderoAcuatico1.getId()
        );

        // Mapear el dinosaurio a DTO y agregarlo a la isla
        dinosaurioService.mapToDTO(herbivoroAcuatico)
                .flatMap(herbivoroAcuaticoDTO -> islaService.agregarDinosaurioIsla(criaderoAcuatico1, herbivoroAcuaticoDTO, herbivoroAcuatico.getPosicion()))
                .doOnSuccess(unused -> System.out.println("Dinosaurio agregado exitosamente."))
                .block(); // Usar block() para esperar la finalización

        System.out.println("Todos los dinosaurios han sido creados y agregados a las islas.");
        System.out.println("EL tablero del criadero acuatico es: ");
        System.out.println(Arrays.deepToString(criaderoAcuatico1.getTablero()));

        //------------------------------------------

        CriaderoVoladores criaderoVolador1 = (CriaderoVoladores) islaService.mapToEntity(criaderoVoladoresDTO).block();
        assert criaderoVolador1 != null;
        System.out.println("Isla obtenida: " + criaderoVolador1.getNombre());

        // Creación de sensores
        SensorFrecuenciaCardiaca sensorFrecuencia6 = new SensorFrecuenciaCardiaca(
                UUID.randomUUID().toString(), "FrecuenciaCardiaca", 50.0, 100.0, 75.0
        );

        SensorMovimiento sensorMovimiento6 = new SensorMovimiento(
                UUID.randomUUID().toString(), "Movimiento", 0.0, 1.0, 0.0
        );

        SensorTemperatura sensorTemperatura6 = new SensorTemperatura(
                UUID.randomUUID().toString(), "Temperatura", 30.0, 40.0, 35.0
        );

        // Crear una lista de sensores y añadir los sensores creados
        List<Sensor> sensores6 = new ArrayList<>();
        sensores6.add(sensorFrecuencia6);
        sensores6.add(sensorMovimiento6);
        sensores6.add(sensorTemperatura6);

        // Creación del dinosaurio con los sensores
        HerbivoroVolador herbivoroVolador = new HerbivoroVolador(
                id6,
                "herbivoro",
                edad,
                "volador",
                sensores6,
                new Posicion(random.nextInt(8), random.nextInt(8), "criadero-volador"),
                criaderoVolador1.getId()
        );

        // Mapear el dinosaurio a DTO y agregarlo a la isla
        dinosaurioService.mapToDTO(herbivoroVolador)
                .flatMap(herbivoroVoladorDTO -> islaService.agregarDinosaurioIsla(criaderoVolador1, herbivoroVoladorDTO, herbivoroVolador.getPosicion()))
                .doOnSuccess(unused -> System.out.println("Dinosaurio agregado exitosamente."))
                .block(); // Usar block() para esperar la finalización

        System.out.println("Todos los dinosaurios han sido creados y agregados a las islas.");
        System.out.println("EL tablero del criadero de voladores es: ");
        System.out.println(Arrays.deepToString(criaderoVolador1.getTablero()));
    }
}
