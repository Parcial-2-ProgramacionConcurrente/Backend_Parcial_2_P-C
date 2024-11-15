# Sistema de Monitoreo de la Maquina de Galton - Backend (Spring WebFlux)

# LINKS

FRONTEND: 

BACKEND: 

---

# PARTICIPANTES

- **Nombre del equipo**: Galton Board Analitics Team
- **Miembros**:
  - Jaime López Díaz
  - Nicolás Jimenez
  - Marcos García Benito

---

# CUENTAS PARA TESTEO:

-USUARIO --> **Mail: [usuario@gmail.com]** || **Nombre: [Usuario]** || **Contraseña: [a12345_679]**

-ADMINISTRADOR --> **Mail: [admin@gmail.com]** || **Nombre: [Administrador]** || **Contraseña: [a12345_67]**

---

## Tabla de Contenidos

- [Introducción](#introducción)

- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
  
- [Implementación Reactiva y Concurrencia](#implementación-reactiva-y-concurrencia)
  - [Técnicas de Concurrencia](#técnicas-de-reactividad)
  - [Técnicas de Reactividad](#técnicas-de-reactividad)
  - [Técnicas de Sincronización](#técnicas-de-reactividad)
    

- [Servicios REST](#servicios-rest)
  - [Endpoints Principales](#endpoints-principales)
  - [Autenticación y Seguridad](#autenticación-y-seguridad)
    
- [Aspectos y Patrones Utilizados](#aspectos-y-patrones-utilizados)

  - [Aspectos (AOP)](#aspectos-aop)
  - [Patrones Utilizados](#patrones-utilizados)
- [Contribuciones y Licencia](#contribuciones-y-licencia)

---

# Introducción

El sistema de monitoreo de Jurassic Park es un backend avanzado desarrollado en Spring WebFlux con una base de datos reactiva en MongoDB y soporte de mensajería asíncrona con RabbitMQ. Su propósito es gestionar en tiempo real la actividad y el estado de salud de dinosaurios dentro de un parque temático, utilizando sensores de frecuencia cardíaca, movimiento y temperatura. Este sistema está diseñado para ofrecer una respuesta rápida y escalable ante eventos críticos, como cambios en la salud de los dinosaurios o interacciones entre ellos. Además, el sistema incluye una mecánica de envejecimiento en tiempo real, junto con un ecosistema reactivo en el que los dinosaurios interactúan según su tipo, alimentación, y entorno.

---

# Arquitectura del Proyecto

El proyecto está organizado en varios paquetes de acuerdo con la lógica de dominio:

1. **Dominio (domain)** - Contiene entidades como Dinosaurio, Isla, Sensor, Usuario y Rol.
2. **Servicios (service)** - Lógica de negocio (DinosaurioService, IslaService, SensorService).
3. **Controladores (controller)** - Controladores REST que exponen los servicios a través de endpoints.
4. **Repositorios (repos)** - Interfaces para MongoDB reactivo.
5. **DTOs (model)** - Objetos de Transferencia de Datos (Data Transfer Objects) para intercambiar datos entre el backend y el frontend.
6. Paquetes alternos orientados a otras tareas como **[Config, Messaging, aop, util etc...]**

# Servicios Principales

- **AuthService**:
  - **Inicio de Sesión de Usuarios**: Utiliza `Mono` para realizar la autenticación reactiva del usuario, validando sus credenciales de manera asíncrona. Si las credenciales son válidas, responde con un JWT simulado; de lo contrario, responde con un estado de error.
  - **Registro de Usuarios**: Este método usa `Mono` para verificar si el correo ya está registrado, y en caso de que no lo esté, guarda de forma reactiva al usuario con credenciales codificadas. La inserción en MongoDB se maneja de forma no bloqueante, permitiendo una experiencia de registro fluida.

- **FabricaGaussService**:
  - **Crear Fábrica**: Utiliza `Mono` para mapear y guardar una nueva entidad `FabricaGauss` en la base de datos. Esto asegura que el proceso de creación sea completamente reactivo y no bloqueante, mejorando el rendimiento y la experiencia del usuario.
  - **Encontrar Todas las Fábricas**: Utiliza `Flux` para obtener todas las entidades `FabricaGauss` de la base de datos de forma reactiva. Esto permite una recuperación de datos eficiente y concurrente.
  - **Encontrar Fábrica por ID**: Utiliza `Mono` para obtener una entidad `FabricaGauss` específica por su ID. Si la entidad no se encuentra, devuelve un error.
  - **Eliminar Fábrica por ID**: Utiliza `Mono` para eliminar una entidad `FabricaGauss` específica por su ID. Si la entidad no se encuentra, devuelve un error.
  - **Actualizar Fábrica**: Utiliza `Mono` para actualizar una entidad `FabricaGauss` existente con nuevos datos. Esto asegura que el proceso de actualización sea completamente reactivo y no bloqueante.
  - **Iniciar Producción Completa**: Inicia un proceso de producción completo en todas las fábricas utilizando `Mono` y `Flux`. Envía mensajes a RabbitMQ y procesa cada fábrica de forma reactiva, asegurando un flujo de trabajo de producción eficiente y no bloqueante.
  - **Verificar Estado de Simulación**: Utiliza `Mono` para recibir mensajes de RabbitMQ y verificar el estado de la simulación, asegurando un monitoreo continuo del proceso.
  - **Actualizar Componentes con Distribución**: Utiliza `Mono` para actualizar cada componente de una máquina con los valores de distribución del `GaltonBoard`. Este proceso está sincronizado para asegurar la consistencia de los datos.

- **GaltonBoardService**:
  - **Simular Caída de Bolas**: Utiliza `Mono` para simular la caída de bolas en un `GaltonBoard`, actualizando la distribución de forma reactiva. Esto asegura un proceso de simulación eficiente y no bloqueante.
  - **Obtener GaltonBoard por ID**: Utiliza `Mono` para obtener una entidad `GaltonBoard` específica por su ID.
  - **Esperar la Finalización de la Simulación**: Utiliza `Mono` para esperar hasta que la simulación actual de caída de bolas se complete.
  - **Guardar GaltonBoard**: Utiliza `Mono` para guardar una entidad `GaltonBoard` en la base de datos de forma reactiva.
  - **Mostrar Distribución**: Utiliza `Mono` para mostrar la distribución de bolas en cada contenedor después de la simulación.
  - **Actualizar Distribución**: Utiliza `Mono` para actualizar la distribución de un `GaltonBoard` con nuevos datos. Este proceso está sincronizado para asegurar la consistencia de los datos.
  - **Obtener GaltonBoard por ID de Fábrica**: Utiliza `Mono` para obtener un `GaltonBoard` asociado con una fábrica específica.
  - **Crear GaltonBoard para Fábrica**: Utiliza `Mono` para crear un nuevo `GaltonBoard` para una fábrica y simular la caída de bolas de forma reactiva.
  - **Enviar Notificación de Finalización de Simulación**: Utiliza `Mono` para enviar una notificación a RabbitMQ indicando la finalización de la simulación.

- **RabbitMQService**:
  - **Enviar Mensaje**: Utiliza `Mono` para enviar mensajes a las colas de RabbitMQ de forma reactiva, asegurando una comunicación no bloqueante.
  - **Recibir Mensaje**: Utiliza `Mono` para recibir mensajes de las colas de RabbitMQ de forma reactiva.
  - **Iniciar Oyente**: Inicia un oyente para recibir continuamente mensajes de las colas de RabbitMQ y procesarlos de forma reactiva.
  - **Obtener Mensajes Recibidos**: Expone un `Flux` que emite los mensajes recibidos por el oyente, permitiendo un monitoreo continuo de los mensajes entrantes.

- **ComponenteWorkerService**:
  - **Procesamiento de Componentes**: Utiliza `Mono` para procesar un componente, calculando su valor basado en la distribución de un `GaltonBoard` y registrando el valor en la base de datos de manera reactiva. El procesamiento se realiza en un hilo separado para cada componente.
  - **Cálculo de Valor**: Este método usa `Mono` para calcular el valor de un `ComponenteWorker` basado en la distribución del `GaltonBoard`, asegurando que la distribución no esté vacía antes de proceder.
  - **Registro de Valor**: Utiliza `Mono` para registrar el valor calculado en el componente y guardarlo en la base de datos de manera reactiva, asegurando que el valor se registre correctamente.

- **MaquinaWorkerService**:
  - **Inicio de Trabajo de Ensamblaje**: Utiliza `Mono` y `Flux` para iniciar el ensamblaje de una lista de máquinas, utilizando un `GaltonBoard` para definir la distribución de los componentes. El ensamblaje se realiza de manera reactiva y sincronizada.
  - **Obtención o Creación de MaquinaWorker**: Este método usa `Mono` para obtener un `MaquinaWorker` de la base de datos o crear uno nuevo si no existe, incluyendo la creación y guardado de `ComponenteWorkers` asociados.
  - **Ensamblaje de Máquina**: Utiliza `Mono` para ensamblar una máquina, procesando cada `ComponenteWorker` con el `GaltonBoard` actualizado y guardando el estado del `MaquinaWorker` de manera reactiva.

- **UsuarioService**:
  - **Obtener Todos los Usuarios**: Utiliza `Flux` para obtener todos los usuarios de la base de datos de manera reactiva, ejecutando la consulta en un pool de hilos específico.
  - **Obtener Usuario por ID**: Este método usa `Mono` para obtener un usuario por su ID de la base de datos de manera reactiva, ejecutando la consulta en un pool de hilos específico.
  - **Crear Usuario**: Utiliza `Mono` para crear un nuevo usuario a partir de un DTO, mapeando el DTO a una entidad y guardando el usuario en la base de datos de manera reactiva.
  - **Actualizar Usuario**: Este método usa `Mono` para actualizar un usuario existente a partir de un DTO, mapeando el DTO a una entidad y guardando los cambios en la base de datos de manera reactiva.
  - **Eliminar Usuario**: Utiliza `Mono` para eliminar un usuario por su ID de la base de datos de manera reactiva, ejecutando la operación en un pool de hilos específico.

# Implementación Reactiva y Concurrencia

## Técnicas de Concurrencia

- **ExecutorService**:  
  - **Descripción**: En `UsuarioService`, se utilizan múltiples instancias de `ExecutorService` para manejar tareas concurrentes, como la creación, actualización, eliminación y obtención de usuarios. Esto permite que las operaciones se ejecuten en hilos separados, mejorando el rendimiento y la capacidad de respuesta del servicio.  
  - **Ejemplo**:  
    ```java
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);
    ```

- **Schedulers**:  
  - **Descripción**: En `UsuarioService`, se utiliza `Schedulers.fromExecutor` para asignar tareas a los `ExecutorService` correspondientes. Esto asegura que las operaciones se ejecuten en los hilos adecuados.  
  - **Ejemplo**:  
    ```java
    return usuarioRepository.findAll()
            .subscribeOn(Schedulers.fromExecutor(executorService));
    ```

## Técnicas de Reactividad

- **Reactor (Mono y Flux)**:  
  - **Descripción**: En todos los servicios (`FabricaGaussService`, `GaltonBoardService`, `RabbitMQService`, `UsuarioService`), se utilizan `Mono` y `Flux` de Project Reactor para manejar flujos de datos reactivos. `Mono` se utiliza para representar un solo elemento o vacío, mientras que `Flux` se utiliza para representar múltiples elementos.  
  - **Ejemplo en FabricaGaussService**:  
    ```java
    public Mono<FabricaGaussDTO> createFabrica(FabricaGaussDTO fabricaGaussDTO) {
        FabricaGauss fabrica = mapToEntity(fabricaGaussDTO);
        return fabricaGaussRepository.save(fabrica)
                .map(this::mapToDTO)
                .doOnError(e -> System.err.println("Error creating Fabrica: " + e.getMessage()));
    }
    ```

- **Schedulers para Reactividad**:  
  - **Descripción**: En `UsuarioService`, se utiliza `Schedulers.boundedElastic` para operaciones de mapeo que pueden ser bloqueantes, asegurando que no bloqueen el hilo principal.  
  - **Ejemplo**:  
    ```java
    public Mono<UsuarioDTO> mapToDTO(Usuario usuario) {
        return Mono.fromCallable(() -> {
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(usuario.getId());
            dto.setNombre(usuario.getNombre());
            // Otros campos...
            return dto;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    ```

- **Sinks**:  
  - **Descripción**: En `RabbitMQService`, se utiliza `Sinks.many().replay().all()` para almacenar y emitir mensajes recibidos de RabbitMQ de manera reactiva.  
  - **Ejemplo**:  
    ```java
    private final Sinks.Many<String> messageSink = Sinks.many().replay().all();
    ```

- **WebClient**:  
  - **Descripción**: En `GaltonBoardService`, se utiliza `WebClient` para realizar llamadas HTTP de manera reactiva.  
  - **Ejemplo**:  
    ```java
    this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/galtonboard").build();
    ```

### Técnicas de Sincronización

- **SynchronizedExecution**:  
  - **Descripción**: En `GaltonBoardService`, se utiliza la anotación `@SynchronizedExecution` para asegurar que ciertos métodos se ejecuten de manera sincronizada, evitando problemas de concurrencia.  
  - **Ejemplo**:  
    ```java
    @SynchronizedExecution
    public Mono<Void> simularCaidaDeBolas(GaltonBoard galtonBoard, double media, double desviacionEstandar) {
        // Implementación...
    }

---

# Servicios REST

### Endpoints Principales

| Recurso        | Método | Endpoint                                   | Descripción                                           |
| -------------- | -------| ------------------------------------------ | ---------------------------------------------------- |
| **Auth**       | POST   | `/api/auth/login`                         | Permite a un usuario iniciar sesión (JWT)            |
|                | POST   | `/api/auth/register`                      | Permite registrar un nuevo usuario                   |
| **Usuarios**   | GET    | `/usuarios`                               | Listado de usuarios                                  |
|                | GET    | `/usuarios/{id}`                          | Obtiene la información de un usuario específico      |
|                | POST   | `/usuarios`                               | Crea un nuevo usuario en el sistema                  |
|                | PUT    | `/usuarios/{id}`                          | Actualiza los datos de un usuario específico         |
|                | DELETE | `/usuarios/{id}`                          | Elimina un usuario del sistema                       |
| **Fabricas**   | GET    | `/fabricas`                               | Listado de fábricas                                  |
|                | GET    | `/fabricas/{id}`                          | Obtiene información de una fábrica específica        |
|                | GET    | `/fabricas/{id}/galtonboard`              | Obtiene el GaltonBoard asociado a una fábrica        |
|                | POST   | `/fabricas/iniciar-produccion`            | Inicia la producción completa                        |
| **GaltonBoard**| POST   | `/api/galtonboard/bolasPorContenedor`     | Obtiene el número de bolas por contenedor en tiempo real |
|                | POST   | `/api/galtonboard/mostrarDistribucion`    | Muestra la distribución final después de la simulación |
| **Messages**   | GET    | `/api/messages/recibidos`                 | Obtiene todos los mensajes recibidos de RabbitMQ     |
| **Home**       | GET    | `/`                                       | Endpoint de bienvenida                               |


### Autenticación y Seguridad

- **Roles**: Existen tres roles básicos: `user`, `admin`, y `paleontólogo`, que controlan los accesos a diferentes partes del sistema.
- **Autenticación JWT**: Se usa un sistema de autenticación basado en tokens JWT para proteger los endpoints sensibles y verificar la identidad de los usuarios.

  
---

# Aspectos y Patrones Utilizados

### Aspectos (AOP)

1. **Manejo de Errores**:

   - **Aspecto de Manejo de Errores (`ErrorHandlingAspect`)**: Captura excepciones en los métodos del servicio, registrando errores para facilitar el monitoreo y diagnóstico.


2. **Monitoreo de Rendimiento**:

   - **Aspecto de Monitoreo de Rendimiento (`PerformanceMonitoringAspect`)**: Mide el tiempo de ejecución de métodos críticos, ayudando a identificar y resolver cuellos de botella.
3. **Validación de Datos**:

   - **Aspecto de Validación (`ValidationAspect`)**: Valida datos de entrada en métodos clave para asegurar que cumplan con los requisitos antes de ser procesados.
  
4. **Ejecución Sincronizada**:

- **Aspecto de Ejecución Sincronizada (`SynchronizationAspect`)**: Utiliza la anotación `@SynchronizedExecution` para asegurar que los métodos anotados se ejecuten de manera sincronizada. Este aspecto emplea un ReentrantLock para garantizar que solo un hilo pueda ejecutar el método a la vez, evitando problemas de concurrencia.

## Patrones Utilizados

1. **Dependency Inyection Patern**: Gestiona las dependencias entre objetos inyectándolas desde una entidad externa en lugar de crearlas internamente. Esto facilita la prueba y el mantenimiento del código.
2. **Observer Patern**: Define una dependencia uno-a-muchos entre objetos, de manera que cuando un objeto cambia de estado, todos sus dependientes son notificados y actualizados automáticamente. En este caso, `Mono` y `Flux` de Reactor se utilizan para manejar flujos de datos reactivos.
3. **Document Patern**: Mapea clases a documentos en una base de datos NoSQL como MongoDB. Esto permite que las clases se almacenen y recuperen fácilmente desde la base de datos.
4. **Data Transfer Object (DTO)**: Separa la capa de presentación y simplifica la transferencia de datos.
5. **Repository Pattern**: Desacopla la lógica de acceso a datos de la lógica de negocio.
6. **Service Layer**: Organiza la lógica de negocio en servicios dedicados.
7. **Patrón de Fábrica**: Centraliza la creación de objetos, permitiendo que la lógica de creación sea gestionada en un solo lugar. En este contexto, `FabricaGauss` se utiliza para crear y gestionar instancias de `Maquina`.
8. **Singleton**: Asegura una única instancia en servicios críticos.

---

# Contribuciones y Licencia

- **Contribuciones**: Abiertas a colaboradores interesados en monitoreo en tiempo real y sistemas reactivos.
- **Licencia**: Detalles de licencia especificando derechos de uso y modificaciones del código.
