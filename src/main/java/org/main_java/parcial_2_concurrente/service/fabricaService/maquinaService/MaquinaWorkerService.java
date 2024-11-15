package org.main_java.parcial_2_concurrente.service.fabricaService.maquinaService;

import org.main_java.parcial_2_concurrente.aop.sync.SynchronizedExecution;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.componente.ComponenteWorkerRepository;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.MaquinaWorker;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.ComponenteWorker;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.MaquinaRepository;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.MaquinaWorkerRepository;
import org.main_java.parcial_2_concurrente.repos.galton.GaltonBoardRepository;
import org.main_java.parcial_2_concurrente.service.fabricaService.FabricaGaussService;
import org.main_java.parcial_2_concurrente.service.galtonService.GaltonBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class MaquinaWorkerService {

    @Autowired
    private ComponenteWorkerRepository componenteWorkerRepository;

    @Autowired
    private MaquinaWorkerRepository maquinaWorkerRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private GaltonBoardRepository galtonBoardRepository;

    private final ComponenteWorkerService componenteWorkerService;
    private final GaltonBoardService galtonBoardService; // Agregamos GaltonBoardService para control de sincronización
    private final FabricaGaussService fabricaGaussService; // Agregamos FabricaGaussService para actualizar los componentes con la distribución

    public MaquinaWorkerService(MaquinaWorkerRepository maquinaWorkerRepository,
                                ComponenteWorkerService componenteWorkerService,
                                MaquinaRepository maquinaRepository,
                                GaltonBoardService galtonBoardService,
                                @Lazy FabricaGaussService fabricaGaussService) {
        this.maquinaWorkerRepository = maquinaWorkerRepository;
        this.componenteWorkerService = componenteWorkerService;
        this.maquinaRepository = maquinaRepository;
        this.galtonBoardService = galtonBoardService;
        this.fabricaGaussService = fabricaGaussService;
    }

    /**
     * Inicia el ensamblaje de una lista de máquinas, utilizando el GaltonBoard.
     *
     * @param maquinasIds Lista de IDs de las máquinas a ensamblar.
     * @param galtonBoard El GaltonBoard que define la distribución de los componentes.
     * @return Mono<Void> señal de que el trabajo de ensamblaje ha finalizado.
     */
    @SynchronizedExecution
    public Mono<Void> iniciarTrabajo(List<String> maquinasIds, GaltonBoard galtonBoard) {
        // Esperar a que la actualización de distribución esté completa antes de continuar
        return galtonBoardService.esperarDistribucionActualizada()
                .thenMany(Flux.fromIterable(maquinasIds)
                        .flatMap(maquinaId ->
                                // Actualizamos los componentes con la distribución final antes de ensamblar
                                fabricaGaussService.actualizarComponentesConDistribucion(maquinaId, galtonBoard)
                                        .then(maquinaRepository.findById(maquinaId)
                                                .flatMap(maquina -> obtenerOCrearMaquinaWorker(maquina, galtonBoard)
                                                        .flatMap(this::ensamblarMaquina)
                                                )
                                        )
                        )
                )
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
            System.err.println("Error: Maquina ID es null para la máquina de tipo " + maquina.getTipo());
            return Mono.error(new IllegalArgumentException("El ID de la máquina no debe ser null"));
        }

        return maquinaWorkerRepository.findById(maquina.getId())
                .switchIfEmpty(Mono.defer(() -> {
                    // Si no existe el MaquinaWorker, crear uno nuevo
                    MaquinaWorker nuevoMaquinaWorker = new MaquinaWorker();
                    nuevoMaquinaWorker.setMaquinaId(maquina.getId());

                    // Crear y guardar ComponenteWorkers, luego recolectar sus IDs
                    List<ComponenteWorker> componenteWorkers = maquina.getComponentesIds().stream()
                            .map(componenteId -> {
                                ComponenteWorker componenteWorker = new ComponenteWorker();
                                componenteWorker.setComponente(new Componente(componenteId, "TipoComponente", 0.0, maquina.getId())); // Crear Componente dummy
                                componenteWorker.setMaquinaWorkerId(nuevoMaquinaWorker.getId());
                                componenteWorker.setGaltonBoard(galtonBoard);
                                componenteWorker.setTrabajoCompletado(false);
                                componenteWorker.setEnsamblado(false);
                                return componenteWorker;
                            })
                            .toList();

                    return componenteWorkerRepository.saveAll(componenteWorkers)
                            .map(ComponenteWorker::getId)
                            .collectList()
                            .flatMap(ids -> {
                                nuevoMaquinaWorker.setComponenteWorkerIds(ids);
                                return maquinaWorkerRepository.save(nuevoMaquinaWorker);
                            });
                }))
                .doOnSuccess(mw -> System.out.println("MaquinaWorker obtenido/creado para la máquina " + maquina.getTipo()))
                .doOnError(e -> System.err.println("Error al obtener o crear MaquinaWorker para " + maquina.getTipo() + ": " + e.getMessage()));
    }

    // Asegúrate de tener una función que obtenga los ComponenteWorkers por ID desde el repositorio.
    private Mono<List<ComponenteWorker>> obtenerComponenteWorkers(List<String> componenteWorkerIds) {
        return Flux.fromIterable(componenteWorkerIds)
                .flatMap(componenteWorkerRepository::findById)
                .collectList();
    }

    private Mono<Void> ensamblarMaquina(MaquinaWorker maquinaWorker) {
        return maquinaRepository.findById(maquinaWorker.getMaquinaId())
                .flatMap(maquina -> {
                    System.out.println("Iniciando ensamblaje de la máquina " + maquina.getTipo());

                    // Verificar si la máquina tiene un GaltonBoard asignado
                    Mono<GaltonBoard> galtonBoardMono = (maquina.getGaltonBoard() == null)
                            ? galtonBoardRepository.findAll()
                            .last() // Obtiene el último `GaltonBoard` insertado en la base de datos
                            .doOnNext(galtonBoard -> System.out.println("GaltonBoard más reciente obtenido: " + galtonBoard.getId()))
                            : Mono.just(maquina.getGaltonBoard());

                    return galtonBoardMono.flatMap(galtonBoardActualizado -> {
                        // Asignar el GaltonBoard actualizado a la máquina y guardar si es necesario
                        if (maquina.getGaltonBoard() == null) {
                            maquina.setGaltonBoard(galtonBoardActualizado);
                            return maquinaRepository.save(maquina);
                        }
                        return Mono.just(maquina);
                    }).flatMap(savedMaquina -> {
                        if (maquinaWorker.getComponenteWorkerIds().isEmpty()) {
                            System.out.println("No hay componentes para ensamblar en la máquina " + maquina.getTipo());
                            return Mono.empty();
                        }

                        // Obtener y procesar cada ComponenteWorker con el GaltonBoard actualizado
                        return obtenerComponenteWorkers(maquinaWorker.getComponenteWorkerIds())
                                .flatMapMany(Flux::fromIterable)
                                .flatMap(componenteWorker ->
                                        componenteWorkerService.procesarComponente(componenteWorker, savedMaquina.getGaltonBoard())
                                )
                                .collectList()
                                .flatMap(processedComponents -> {
                                    maquina.setEstado("ENSAMBLADA");
                                    System.out.println("Máquina " + maquina.getTipo() + " ensamblada con éxito.");

                                    // Imprimir detalles de los componentes ensamblados
                                    processedComponents.forEach(componenteWorker -> {
                                        componenteWorker.setEnsamblado(true);
                                        System.out.println("- Componente ID: " + componenteWorker.getComponente().getId() +
                                                ", Tipo: " + componenteWorker.getComponente().getTipo() +
                                                ", Estado ensamblado: " + componenteWorker.isEnsamblado());

                                    });

                                    return Mono.just(maquinaWorker);
                                })
                                .flatMap(maquinaWorkerRepository::save)
                                .doOnSuccess(savedWorker -> System.out.println("Estado del MaquinaWorker guardado para máquina " + maquina.getTipo()))
                                .doOnError(e -> System.err.println("Error ensamblando máquina " + maquina.getTipo() + ": " + e.getMessage()));
                    });
                }).then();
    }

}
