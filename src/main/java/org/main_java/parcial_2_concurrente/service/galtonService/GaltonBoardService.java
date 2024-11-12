package org.main_java.parcial_2_concurrente.service.galtonService;

import org.main_java.parcial_2_concurrente.domain.fabrica.FabricaGauss;
import org.main_java.parcial_2_concurrente.domain.galton.Distribucion;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoardStatus;
import org.main_java.parcial_2_concurrente.model.galtonDTO.GaltonBoardDTO;
import org.main_java.parcial_2_concurrente.repos.galton.GaltonBoardRepository;
import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GaltonBoardService {

    private final GaltonBoardRepository galtonBoardRepository;
    private final RabbitMQService rabbitMQService;
    GaltonBoardStatus status;
    // Creamos un pool de hilos para manejar la simulación concurrente
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);



    private static final String RABBITMQ_QUEUE = "galtonboard_simulation_queue";


    public GaltonBoardService(GaltonBoardRepository galtonBoardRepository, RabbitMQService rabbitMQService) {
        this.galtonBoardRepository = galtonBoardRepository;
        this.rabbitMQService = rabbitMQService;
    }

    public Mono<Void> simularCaidaDeBolas(GaltonBoard galtonBoard) {
        System.out.println("Iniciando simulación de caída de " + galtonBoard.getNumBolas() + " bolas en el tablero " + galtonBoard.getId());

        int numBolas = galtonBoard.getNumBolas();
        AtomicInteger numBolasProcesadas = new AtomicInteger(0);
        AtomicInteger[] contenedores = new AtomicInteger[galtonBoard.getNumContenedores()];

        // Inicializar contenedores como AtomicInteger para hilos seguros
        for (int i = 0; i < galtonBoard.getNumContenedores(); i++) {
            contenedores[i] = new AtomicInteger(0);
        }

        return Flux.range(0, numBolas)
                .flatMap(i -> Mono.fromFuture(CompletableFuture.runAsync(() -> {
                    int contenedorId = (int) (Math.random() * galtonBoard.getNumContenedores());
                    contenedores[contenedorId].incrementAndGet(); // Incrementar el contenedor en el que cae la bola
                    System.out.println("Bola #" + i + " cayó en el contenedor " + contenedorId);
                })))
                .doOnNext(i -> {
                    numBolasProcesadas.incrementAndGet();
                    if (numBolasProcesadas.get() % 100 == 0 || numBolasProcesadas.get() == numBolas) {
                        System.out.println("Procesadas " + numBolasProcesadas + " bolas.");
                        Map<String, Integer> datosDistribucion = new HashMap<>();
                        for (int j = 0; j < contenedores.length; j++) {
                            datosDistribucion.put("contenedor_" + j, contenedores[j].get());
                        }
                        mostrarDistribucion(datosDistribucion); // Muestra distribución actual en cada lote
                    }
                })
                .doOnComplete(() -> {
                    galtonBoard.setEstado("COMPLETADO");
                    System.out.println("Simulación completada en el tablero " + galtonBoard.getId());

                    Map<String, Integer> datosFinales = new HashMap<>();
                    for (int j = 0; j < contenedores.length; j++) {
                        datosFinales.put("contenedor_" + j, contenedores[j].get());
                    }
                    mostrarDistribucion(datosFinales); // Mostrar distribución completa al final

                    // Notificar finalización de la simulación
                    enviarNotificacionSimulacionCompleta(galtonBoard.getId(), datosFinales)
                            .doOnSuccess(v -> System.out.println("Notificación de simulación completada enviada para GaltonBoard ID: " + galtonBoard.getId()))
                            .doOnError(e -> System.err.println("Error enviando notificación: " + e.getMessage()))
                            .subscribe();
                })
                .doOnError(e -> System.err.println("Error simulando la caída de bolas: " + e.getMessage()))
                .then();
    }





    /**
     * Muestra la distribución de bolas en cada contenedor al finalizar la simulación.
     *
     * @param distribucion Mapa que representa la cantidad de bolas en cada contenedor.
     */
    private void mostrarDistribucion(Map<String, Integer> distribucion) {
        System.out.println("\nDistribución de bolas en los contenedores:");
        int maxBolas = distribucion.values().stream().max(Integer::compareTo).orElse(1);

        distribucion.forEach((contenedor, cantidad) -> {
            int longitudBarra = (int) ((cantidad * 50) / (double) maxBolas); // Normalizamos a una barra de longitud máxima 50
            String barra = "*".repeat(longitudBarra);
            System.out.printf("%s: %s (%d bolas)%n", contenedor, barra, cantidad);
        });
    }


    /**
     * Actualiza la distribución del GaltonBoard con una nueva distribución.
     *
     * @param galtonBoard       El GaltonBoard cuya distribución será actualizada.
     * @param nuevaDistribucion Mapa con la nueva distribución.
     * @return Mono<Void> señal de que la actualización ha finalizado.
     */
    public Mono<Void> actualizarDistribucion(GaltonBoard galtonBoard, Map<String, Integer> nuevaDistribucion) {
        galtonBoard.getDistribucion().setDatos(nuevaDistribucion);
        status.actualizarDistribucion(nuevaDistribucion);

        return galtonBoardRepository.save(galtonBoard)
                .doOnSuccess(v -> System.out.println("Distribución actualizada en el tablero " + galtonBoard.getId()))
                .doOnError(e -> System.err.println("Error updating distribution: " + e.getMessage())).then();
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
    public Mono<GaltonBoard> crearGaltonBoardParaFabrica(FabricaGauss fabrica) {
        GaltonBoard nuevoGaltonBoard = new GaltonBoard();
        nuevoGaltonBoard.setNumBolas(100);  // Valor ejemplo
        nuevoGaltonBoard.setNumContenedores(10);  // Valor ejemplo
        nuevoGaltonBoard.setEstado("INICIADO");
        nuevoGaltonBoard.setDistribucion(new Distribucion()); // Crear una nueva Distribucion
        return galtonBoardRepository.save(nuevoGaltonBoard)
                .doOnSuccess(galtonBoard -> System.out.println("GaltonBoard creado para la fábrica: " + fabrica.getNombre()))
                .doOnError(e -> System.err.println("Error al crear GaltonBoard para la fábrica: " + e.getMessage()));
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