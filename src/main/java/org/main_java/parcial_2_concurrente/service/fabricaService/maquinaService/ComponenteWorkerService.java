package org.main_java.parcial_2_concurrente.service.fabricaService.maquinaService;

import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.ComponenteWorker;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.componente.ComponenteRepository;
import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ComponenteWorkerService {

    private final ComponenteRepository componenteRepository;
    private final RabbitMQService rabbitMQService;

    // Crea un pool de hilos para procesar cada componente en un hilo separado
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public ComponenteWorkerService(ComponenteRepository componenteRepository, RabbitMQService rabbitMQService) {
        this.componenteRepository = componenteRepository;
        this.rabbitMQService = rabbitMQService;
    }

    /**
     * Procesa un único componente: calcula el valor y lo registra.
     *
     * @param componenteWorker El ComponenteWorker a procesar.
     * @param galtonBoard      El GaltonBoard que provee la distribución de valores.
     * @return Mono<Void> que indica el final del procesamiento del componente.
     */
    public Mono<ComponenteWorker> procesarComponente(ComponenteWorker componenteWorker, GaltonBoard galtonBoard) {
        if (componenteWorker.getComponente() == null) {
            System.err.println("El componente asociado al ComponenteWorker es null, omitiendo procesamiento.");
            return Mono.just(componenteWorker); // Devolver el componente sin procesar
        }

        return calcularValor(componenteWorker, galtonBoard)
                .flatMap(valor -> registrarValor(componenteWorker.getComponente(), valor))
                .doOnSuccess(v -> System.out.println("Componente procesado con valor registrado: " + componenteWorker.getComponente().getId()))
                .doOnError(e -> System.err.println("Error procesando componente " + componenteWorker.getComponente().getId() + ": " + e.getMessage()))
                .doFinally(signal -> System.out.println("Proceso finalizado para el componente: " + componenteWorker.getComponente().getId()))
                .thenReturn(componenteWorker); // Devolver el componente procesado
    }





    /**
     * Calcula el valor del ComponenteWorker basado en la distribución del GaltonBoard.
     *
     * @param componenteWorker El ComponenteWorker para el cual se calculará el valor.
     * @param galtonBoard El GaltonBoard que contiene la distribución de la simulación.
     * @return Mono<Double> el valor calculado.
     */
    public Mono<Double> calcularValor(ComponenteWorker componenteWorker, GaltonBoard galtonBoard) {
        // Obtener la distribución desde el GaltonBoard
        return componenteWorker.obtenerValorDesdeGaltonBoard(galtonBoard)
                .doOnSuccess(valor -> System.out.println("Valor calculado para el componente " + componenteWorker.getComponente().getTipo() + ": " + valor))
                .doOnError(e -> System.err.println("Error calculando el valor: " + e.getMessage()));
    }

    /**
     * Registra el valor calculado en el componente y guarda en la base de datos.
     *
     * @param componente El componente en el que se registrará el valor.
     * @param valor El valor a registrar.
     * @return Mono<Void> señal de que el registro ha sido exitoso.
     */
    public Mono<Void> registrarValor(Componente componente, double valor) {
        return componente.registrarValor(valor)
                .then(componenteRepository.save(componente))
                .doOnSuccess(c -> System.out.println("Valor registrado en el componente: " + c.getValorCalculado()))
                .doOnError(e -> System.err.println("Error registrando el valor: " + e.getMessage()))
                .then();
    }

    /**
     * Envía una notificación de completado a la cola de RabbitMQ.
     *
     * @return Mono<Void> señal de que la notificación ha sido enviada.
     */
    private Mono<Void> enviarNotificacionDeCompletado() {
        return rabbitMQService.enviarMensaje("NOTIFICACION_COLA", "Todos los componentes procesados")
                .doOnSuccess(v -> System.out.println("Notificación de completado enviada a RabbitMQ"))
                .doOnError(e -> System.err.println("Error enviando notificación de completado: " + e.getMessage()));
    }
}
