package org.main_java.parcial_2_concurrente.service.fabricaService.maquinaService;

import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.MaquinaWorker;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.ComponenteWorker;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.MaquinaWorkerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class MaquinaWorkerService {

    private final MaquinaWorkerRepository maquinaWorkerRepository;
    private final ComponenteWorkerService componenteWorkerService;

    public MaquinaWorkerService(MaquinaWorkerRepository maquinaWorkerRepository, ComponenteWorkerService componenteWorkerService) {
        this.maquinaWorkerRepository = maquinaWorkerRepository;
        this.componenteWorkerService = componenteWorkerService;
    }

    /**
     * Inicia el ensamblaje de una lista de máquinas, utilizando el GaltonBoard.
     *
     * @param maquinas    Lista de máquinas a ensamblar.
     * @param galtonBoard El GaltonBoard que define la distribución de los componentes.
     * @return Mono<Void> señal de que el trabajo de ensamblaje ha finalizado.
     */
    public Mono<Void> iniciarTrabajo(List<Maquina> maquinas, GaltonBoard galtonBoard) {
        return Flux.fromIterable(maquinas)
                .flatMap(maquina -> obtenerOCrearMaquinaWorker(maquina, galtonBoard)
                        .flatMap(this::ensamblarMaquina))
                .then()
                .doOnSuccess(v -> System.out.println("Ensamblaje completo para todas las máquinas."))
                .doOnError(e -> System.err.println("Error en iniciarTrabajo: " + e.getMessage()));
    }

    /**
     * Obtiene un MaquinaWorker de la base de datos o lo crea si no existe.
     *
     * @param maquina     La máquina para la cual se necesita un MaquinaWorker.
     * @param galtonBoard El GaltonBoard utilizado para el ensamblaje.
     * @return Mono<MaquinaWorker> el MaquinaWorker existente o creado.
     */
    private Mono<MaquinaWorker> obtenerOCrearMaquinaWorker(Maquina maquina, GaltonBoard galtonBoard) {
        if (maquina.getId() == null) {
            // Log an error and return a Mono error if the Maquina ID is null
            System.err.println("Error: Maquina ID is null for maquina of type " + maquina.getTipo());
            return Mono.error(new IllegalArgumentException("Maquina ID must not be null"));
        }

        return maquinaWorkerRepository.findById(maquina.getId())
                .switchIfEmpty(Mono.defer(() -> {
                    // If no MaquinaWorker exists, create a new one
                    MaquinaWorker nuevoMaquinaWorker = new MaquinaWorker();
                    nuevoMaquinaWorker.setMaquina(maquina);
                    nuevoMaquinaWorker.setComponenteWorkers(maquina.getComponentes().stream()
                            .map(componente -> new ComponenteWorker(null, componente, nuevoMaquinaWorker, galtonBoard, false))
                            .toList());
                    return maquinaWorkerRepository.save(nuevoMaquinaWorker);
                }))
                .doOnSuccess(mw -> System.out.println("MaquinaWorker obtenido/creado para la máquina " + maquina.getTipo()))
                .doOnError(e -> System.err.println("Error al obtener o crear MaquinaWorker para " + maquina.getTipo() + ": " + e.getMessage()));
    }


    /**
     * Ensambla una máquina ejecutando cada ComponenteWorker asociado y registrando valores en función del GaltonBoard.
     *
     * @param maquinaWorker El MaquinaWorker que gestiona el ensamblaje de la máquina.
     * @return Mono<Void> señal de que el ensamblaje de la máquina ha finalizado.
     */
    private Mono<Void> ensamblarMaquina(MaquinaWorker maquinaWorker) {
        System.out.println("Iniciando ensamblaje de la máquina " + maquinaWorker.getMaquina().getTipo());

        // Procesa cada ComponenteWorker a través de ComponenteWorkerService y utiliza la distribución del GaltonBoard
        return componenteWorkerService.procesarComponentes(
                        Flux.fromIterable(maquinaWorker.getComponenteWorkers()),
                        maquinaWorker.getMaquina().getGaltonBoard() // Proporcionamos el GaltonBoard para el cálculo
                )
                .then(Mono.fromRunnable(() -> {
                    maquinaWorker.getMaquina().setEstado("ENSAMBLADA");
                    System.out.println("Máquina " + maquinaWorker.getMaquina().getTipo() + " ensamblada con éxito.");
                }))
                .then(maquinaWorkerRepository.save(maquinaWorker))
                .doOnSuccess(savedWorker -> System.out.println("Estado del MaquinaWorker guardado para máquina " + savedWorker.getMaquina().getTipo()))
                .doOnError(e -> System.err.println("Error ensamblando máquina " + maquinaWorker.getMaquina().getTipo() + ": " + e.getMessage())).then();
    }
}
