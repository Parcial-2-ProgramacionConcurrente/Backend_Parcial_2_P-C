package org.main_java.parcial_2_concurrente.service.galtonService;

import org.main_java.parcial_2_concurrente.domain.fabrica.FabricaGauss;
import org.main_java.parcial_2_concurrente.domain.galton.Distribucion;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.model.galtonDTO.GaltonBoardDTO;
import org.main_java.parcial_2_concurrente.repos.galton.GaltonBoardRepository;
import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class GaltonBoardService {

    private final GaltonBoardRepository galtonBoardRepository;
    private final RabbitMQService rabbitMQService;

    private static final String RABBITMQ_QUEUE = "galtonboard_simulation_queue";


    public GaltonBoardService(GaltonBoardRepository galtonBoardRepository, RabbitMQService rabbitMQService) {
        this.galtonBoardRepository = galtonBoardRepository;
        this.rabbitMQService = rabbitMQService;
    }

    /**
     * Simula la caída de bolas en el GaltonBoard y actualiza la distribución en cada contenedor.
     *
     * @param galtonBoard El GaltonBoard en el cual simular la caída de bolas.
     * @return Mono<Void> señal de que la simulación ha finalizado.
     */
    public Mono<Void> simularCaidaDeBolas(GaltonBoard galtonBoard) {
        System.out.println("Iniciando simulación de caída de " + galtonBoard.getNumBolas() + " bolas en el tablero " + galtonBoard.getId());

        return Flux.range(0, galtonBoard.getNumBolas())
                .flatMap(i -> {
                    int contenedorId = (int) (Math.random() * galtonBoard.getNumContenedores());
                    return galtonBoard.getDistribucion().agregarBola(contenedorId)
                            .doOnSuccess(v -> System.out.println("Bola #" + i + " cayó en el contenedor " + contenedorId));
                })
                .then(Mono.fromRunnable(() -> {
                    galtonBoard.setEstado("COMPLETADO");
                    System.out.println("Simulación completada en el tablero " + galtonBoard.getId());
                    mostrarDistribucion(galtonBoard.getDistribucion().getDatos()); // Mostrar distribución completa

                    // Enviar mensaje a RabbitMQ notificando la finalización de la simulación
                    enviarNotificacionSimulacionCompleta(galtonBoard.getId(), galtonBoard.getDistribucion().getDatos())
                            .subscribe();
                }))
                .doOnError(e -> System.err.println("Error simulating ball fall: " + e.getMessage()))
                .then();
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
     * Enviar notificación de simulación completa a RabbitMQ con los datos de la distribución.
     *
     * @param galtonBoardId ID del GaltonBoard que completó la simulación.
     * @param distribucion Mapa de distribución de bolas por contenedor.
     * @return Mono<Void> señal de que el mensaje ha sido enviado.
     */
    private Mono<Void> enviarNotificacionSimulacionCompleta(String galtonBoardId, Map<String, Integer> distribucion) {
        String mensaje = "Simulación completada para GaltonBoard ID: " + galtonBoardId + " con distribución: " + distribucion.toString();
        return rabbitMQService.enviarMensaje(RABBITMQ_QUEUE, mensaje)
                .doOnSuccess(v -> System.out.println("Mensaje enviado a RabbitMQ: " + mensaje))
                .doOnError(e -> System.err.println("Error enviando mensaje a RabbitMQ: " + e.getMessage()));
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
