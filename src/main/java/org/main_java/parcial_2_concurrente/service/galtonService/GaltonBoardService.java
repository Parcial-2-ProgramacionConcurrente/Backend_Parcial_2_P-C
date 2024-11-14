package org.main_java.parcial_2_concurrente.service.galtonService;

import org.main_java.parcial_2_concurrente.aop.sync.SynchronizedExecution;
import org.main_java.parcial_2_concurrente.domain.fabrica.FabricaGauss;
import org.main_java.parcial_2_concurrente.domain.galton.Distribucion;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoardStatus;
import org.main_java.parcial_2_concurrente.model.galtonDTO.GaltonBoardDTO;
import org.main_java.parcial_2_concurrente.repos.galton.GaltonBoardRepository;
import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GaltonBoardService {

    private final WebClient webClient;
    private final GaltonBoardRepository galtonBoardRepository;
    private final RabbitMQService rabbitMQService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final AtomicBoolean procesandoSimulacion = new AtomicBoolean(false);
    private final AtomicBoolean distribucionActualizando = new AtomicBoolean(false); // Indicador de actualización


    public GaltonBoardService(GaltonBoardRepository galtonBoardRepository, RabbitMQService rabbitMQService, WebClient.Builder webClientBuilder) {
        this.galtonBoardRepository = galtonBoardRepository;
        this.rabbitMQService = rabbitMQService;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/galtonboard").build();
    }

    @SynchronizedExecution
    public Mono<Void> simularCaidaDeBolas(GaltonBoard galtonBoard, double media, double desviacionEstandar) {
        if (procesandoSimulacion.get()) {
            System.out.println("Esperando a que se complete la simulación en curso...");
            return Mono.empty().delayElement(Duration.ofMillis(500))
                    .then(simularCaidaDeBolas(galtonBoard, media, desviacionEstandar));
        }

        procesandoSimulacion.set(true);
        System.out.println("Iniciando simulación de caída de " + galtonBoard.getNumBolas() + " bolas en el tablero " + galtonBoard.getId());

        int numContenedores = galtonBoard.getNumContenedores();
        int n = numContenedores - 1; // Número de niveles

        // Calculamos el discriminante
        double discriminante = 1 - (4 * desviacionEstandar * desviacionEstandar) / n;

        // Verificamos que el discriminante sea no negativo
        if (discriminante < 0) {
            System.err.println("No es posible obtener una desviación estándar de " + desviacionEstandar + " con n = " + n);
            // Manejar el error adecuadamente
            return Mono.error(new IllegalArgumentException("Parámetros incompatibles."));
        }

        // Calculamos las posibles soluciones para p
        double sqrtDiscriminante = Math.sqrt(discriminante);
        double p1 = (1 + sqrtDiscriminante) / 2;
        double p2 = (1 - sqrtDiscriminante) / 2;

        // Elegimos el valor de p que está entre 0 y 1
        double p;
        if (p1 >= 0 && p1 <= 1) {
            p = p1;
        } else if (p2 >= 0 && p2 <= 1) {
            p = p2;
        } else {
            System.err.println("No hay solución válida para p.");
            // Manejar el error adecuadamente
            return Mono.error(new IllegalArgumentException("No hay solución válida para p."));
        }

        // Calculamos la media resultante
        double mediaResultante = n * p;
        System.out.println("Media resultante: " + mediaResultante);

        double toleranciaAceptable = 0.1; // Diferencia máxima aceptable de 0.1

        // Informamos si la media resultante difiere de la media deseada
        if (Math.abs(mediaResultante - media) > toleranciaAceptable) {
            System.err.println("La media resultante (" + mediaResultante + ") difiere de la media deseada (" + media + ").");
            // Puedes decidir cómo manejar esta discrepancia
        }

        // Continuamos con la simulación utilizando este p
        Random random = new Random();
        int numBolas = galtonBoard.getNumBolas();
        AtomicInteger numBolasProcesadas = new AtomicInteger(0);

        return Flux.range(0, numBolas)
                .flatMap(i -> Mono.fromCallable(() -> {
                    // Simulamos el recorrido de la bola
                    int posicion = 0;
                    for (int nivel = 0; nivel < n; nivel++) {
                        if (random.nextDouble() < p) {
                            posicion++;
                        }
                    }
                    int contenedorId = posicion;
                    contenedorId = Math.max(0, Math.min(numContenedores - 1, contenedorId));

                    // Imprimir el hilo actual para verificar el uso del ExecutorService
                    System.out.println("    ----> Hilo actual: [" + Thread.currentThread().getName() + "] - Bola #" + i + " cayó en el contenedor " + contenedorId);
                    int finalContenedorId = contenedorId;
                    galtonBoard.getDistribucion().agregarBola(contenedorId).subscribe(msg -> {
                        System.out.println(msg);
                        String mensaje = String.format("Bola #%d agregada al contenedor %d", i, finalContenedorId);
                        rabbitMQService.enviarMensaje("queue_bolas", mensaje)
                                .doOnSuccess(v -> System.out.println("Notificación enviada a RabbitMQ: " + mensaje))
                                .doOnError(e -> System.err.println("Error enviando notificación a RabbitMQ: " + e.getMessage()))
                                .subscribe();
                    });

                    return i;
                }).subscribeOn(Schedulers.fromExecutor(executorService))) // Asignamos cada tarea al executorService
                .doOnNext(i -> {
                    numBolasProcesadas.incrementAndGet();
                    if (numBolasProcesadas.get() % 100 == 0 || numBolasProcesadas.get() == numBolas) {
                        System.out.println("Procesadas " + numBolasProcesadas + " bolas.");
                        this.webClient.post()
                                .uri("/bolasPorContenedor?galtonBoardId=" + galtonBoard.getId())
                                .retrieve()
                                .bodyToMono(Map.class)
                                .subscribe();
                    }
                })
                .doOnComplete(() -> {
                    galtonBoard.setEstado("COMPLETADO");
                    System.out.println("Simulación completada en el tablero " + galtonBoard.getId());

                    galtonBoard.getDistribucion().obtenerDistribucion()
                            .doOnSuccess(this::mostrarDistribucion)
                            .doOnError(e -> System.err.println("Error mostrando distribución final: " + e.getMessage()))
                            .subscribe();

                    galtonBoardRepository.save(galtonBoard)
                            .doOnSuccess(savedGaltonBoard -> System.out.println("GaltonBoard actualizado en la base de datos para el tablero " + savedGaltonBoard.getId()))
                            .doOnError(e -> System.err.println("Error guardando el GaltonBoard actualizado: " + e.getMessage()))
                            .subscribe();

                    enviarNotificacionSimulacionCompleta(galtonBoard.getId(), galtonBoard.getDistribucion().getDatos())
                            .doOnSuccess(v -> System.out.println("Notificación de simulación completada enviada para GaltonBoard ID: " + galtonBoard.getId()))
                            .doOnError(e -> System.err.println("Error enviando notificación: " + e.getMessage()))
                            .subscribe();

                    this.webClient.post()
                            .uri("/mostrarDistribucion?galtonBoardId=" + galtonBoard.getId())
                            .retrieve()
                            .bodyToMono(Map.class)
                            .subscribe();

                    executorService.shutdown(); // Cerramos el ExecutorService al finalizar
                })
                .doOnError(e -> System.err.println("Error simulando la caída de bolas: " + e.getMessage()))
                .doFinally(signal -> procesandoSimulacion.set(false)) // Liberamos el bloqueo al finalizar
                .then();
    }


    public Mono<GaltonBoard> obtenerGaltonBoardPorId(String id) {
        return galtonBoardRepository.findById(id);
    }


    public Mono<Void> esperarSimulacionCompletada() {
        return Mono.defer(() -> {
            if (procesandoSimulacion.get()) {
                System.out.println("Esperando a que termine la simulación de caída de bolas...");
                return Mono.delay(Duration.ofMillis(100)).then(esperarSimulacionCompletada());
            }
            return Mono.empty();
        });
    }


    public Mono<GaltonBoard> guardarGaltonBoard(GaltonBoard galtonBoard) {
        return galtonBoardRepository.save(galtonBoard)
                .doOnSuccess(savedGaltonBoard -> System.out.println("GaltonBoard saved with ID: " + savedGaltonBoard.getId()));
    }

    /**
     * Muestra la distribución de bolas en cada contenedor al finalizar la simulación.
     *
     * @param distribucion Mapa que representa la cantidad de bolas en cada contenedor.
     */
    /**
     * Muestra la distribución de bolas en cada contenedor en orden al finalizar la simulación.
     *
     * @param distribucion Mapa que representa la cantidad de bolas en cada contenedor.
     */
    public Mono<Map<String, Integer>> mostrarDistribucion(Map<String, Integer> distribucion) {
        System.out.println("\nDistribución de bolas en los contenedores:");
        int maxBolas = distribucion.values().stream().max(Integer::compareTo).orElse(1);

        // Ordenamos el mapa por las claves
        distribucion.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((a, b) -> {
                    int numA = Integer.parseInt(a.replace("contenedor_", ""));
                    int numB = Integer.parseInt(b.replace("contenedor_", ""));
                    return Integer.compare(numA, numB);
                }))
                .forEach(entry -> {
                    String contenedor = entry.getKey();
                    int cantidad = entry.getValue();
                    int longitudBarra = (int) ((cantidad * 50) / (double) maxBolas);
                    String barra = "*".repeat(longitudBarra);
                    System.out.printf("%s: %s (%d bolas)%n", contenedor, barra, cantidad);
                });

        return Mono.just(distribucion);
    }

    /**
     * Actualiza la distribución del GaltonBoard con una nueva distribución.
     *
     * @param galtonBoard       El GaltonBoard cuya distribución será actualizada.
     * @param nuevaDistribucion Mapa con la nueva distribución.
     * @return Mono<Void> señal de que la actualización ha finalizado.
     */
    @SynchronizedExecution
    public Mono<Void> actualizarDistribucion(GaltonBoard galtonBoard, Map<String, Integer> nuevaDistribucion) {
        if (distribucionActualizando.get()) {
            System.out.println("Esperando a que se complete la actualización de la distribución antes de continuar...");
            return esperarDistribucionActualizada()
                    .then(Mono.defer(() -> actualizarDistribucion(galtonBoard, nuevaDistribucion))); // Reintentar después de esperar
        }

        distribucionActualizando.set(true); // Indicamos que se está actualizando la distribución

        return esperarSimulacionCompletada()
                .then(Mono.defer(() -> {
                    galtonBoard.getDistribucion().setDatos(nuevaDistribucion);
                    return galtonBoardRepository.save(galtonBoard)
                            .doOnSuccess(v -> System.out.println("Distribución actualizada en el tablero " + galtonBoard.getId()))
                            .doOnError(e -> System.err.println("Error actualizando distribución: " + e.getMessage()))
                            .doFinally(signal -> distribucionActualizando.set(false)); // Liberamos el bloqueo al finalizar
                })).then();
    }
    /**
     * Metodo para esperar hasta que la actualización de la distribución esté completa.
     */
    public Mono<Void> esperarDistribucionActualizada() {
        return Mono.defer(() -> {
            if (distribucionActualizando.get()) {
                System.out.println("Esperando a que se complete la actualización de la distribución...");
                return Mono.delay(Duration.ofMillis(100)).then(esperarDistribucionActualizada());
            }
            return Mono.empty();
        });
    }

    /**
     * Obtiene un GaltonBoard asociado a una fábrica, si existe.
     *
     * @param fabricaId ID de la fábrica.
     * @return Mono<GaltonBoard> el GaltonBoard asociado, si existe.
     */
    public Mono<GaltonBoard> obtenerGaltonBoardPorFabricaId(String fabricaId) {
        return galtonBoardRepository.findByFabricaId(fabricaId);
    }

    /**
     * Crea un nuevo GaltonBoard para una fábrica si no existe uno.
     *
     * @param fabrica La fábrica para la cual se creará el GaltonBoard.
     * @return Mono<GaltonBoard> el nuevo GaltonBoard creado.
     */
    public Mono<GaltonBoard> crearGaltonBoardParaFabrica(FabricaGauss fabrica, double media, double desviacionEstandar) {
        GaltonBoard nuevoGaltonBoard = new GaltonBoard();
        nuevoGaltonBoard.setNumBolas(100);  // Número de bolas a simular
        nuevoGaltonBoard.setNumContenedores(12);  // Número de contenedores
        nuevoGaltonBoard.setEstado("INICIADO");

        // Crear la distribución con el mismo número de contenedores que el GaltonBoard
        Distribucion distribucion = new Distribucion(nuevoGaltonBoard.getNumContenedores());
        nuevoGaltonBoard.setDistribucion(distribucion);
        nuevoGaltonBoard.setFabricaId(fabrica.getId());  // Asignar el ID de la fábrica al GaltonBoard

        // Guardar el GaltonBoard inicial en la base de datos
        return galtonBoardRepository.save(nuevoGaltonBoard)
                .flatMap(galtonBoardCreado -> {
                    System.out.println("GaltonBoard creado para la fábrica: " + fabrica.getNombre());

                    // Simular la caída de bolas y actualizar la distribución antes de continuar
                    return simularCaidaDeBolas(galtonBoardCreado, media, desviacionEstandar)
                            .then(Mono.defer(() -> {
                                // Guardar el GaltonBoard con la distribución actualizada
                                return galtonBoardRepository.save(galtonBoardCreado)
                                        .doOnSuccess(gb -> System.out.println("GaltonBoard actualizado con la distribución de bolas para la fábrica: " + fabrica.getNombre()));
                            }));
                })
                .doOnError(e -> System.err.println("Error al crear o actualizar GaltonBoard para la fábrica: " + fabrica.getNombre() + " - " + e.getMessage()));
    }



    /**
     * Envía una notificación a RabbitMQ informando la finalización de la simulación.
     *
     * @param galtonBoardId ID del GaltonBoard.
     * @param distribucion  Distribución final de las bolas en los contenedores.
     * @return Mono<Void> señal de que el mensaje fue enviado.
     */
    private Mono<Void> enviarNotificacionSimulacionCompleta(String galtonBoardId, Map<String, Integer> distribucion) {
        String mensaje = "Simulación completada para GaltonBoard " + galtonBoardId;
        return rabbitMQService.enviarMensaje("simulacion_queue", mensaje)
                .doOnSuccess(v -> System.out.println("Notificación de completado enviada a RabbitMQ"))
                .doOnError(e -> System.err.println("Error enviando notificación de completado: " + e.getMessage()));
    }

    private GaltonBoardDTO mapToDTO(GaltonBoard galtonBoard) {
        GaltonBoardDTO dto = new GaltonBoardDTO();
        dto.setId(galtonBoard.getId());
        dto.setNumBolas(galtonBoard.getNumBolas());
        dto.setNumContenedores(galtonBoard.getNumContenedores());
        dto.setEstado(galtonBoard.getEstado());
        dto.setDistribucion(galtonBoard.getDistribucion());
        dto.setFabricaId(galtonBoard.getFabricaId());
        return dto;
    }

    private GaltonBoard mapToEntity(GaltonBoardDTO galtonBoardDTO) {
        GaltonBoard galtonBoard = new GaltonBoard();
        galtonBoard.setNumBolas(galtonBoardDTO.getNumBolas());
        galtonBoard.setNumContenedores(galtonBoardDTO.getNumContenedores());
        galtonBoard.setEstado(galtonBoardDTO.getEstado());
        galtonBoard.setDistribucion(galtonBoardDTO.getDistribucion());
        galtonBoard.setFabricaId(galtonBoardDTO.getFabricaId());
        return galtonBoard;
    }
}
