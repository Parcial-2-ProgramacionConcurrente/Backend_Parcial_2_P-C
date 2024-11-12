package org.main_java.parcial_2_concurrente.service.fabricaService.maquinaService;

import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.componente.ComponenteWorkerRepository;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.MaquinaWorker;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.ComponenteWorker;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.MaquinaRepository;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.MaquinaWorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class MaquinaWorkerService {

    @Autowired
    private ComponenteWorkerRepository componenteWorkerRepository;

    private final MaquinaWorkerRepository maquinaWorkerRepository;
    private final ComponenteWorkerService componenteWorkerService;
    private final MaquinaRepository maquinaRepository;

    public MaquinaWorkerService(MaquinaWorkerRepository maquinaWorkerRepository, ComponenteWorkerService componenteWorkerService, MaquinaRepository maquinaRepository) {
        this.maquinaWorkerRepository = maquinaWorkerRepository;
        this.componenteWorkerService = componenteWorkerService;
        this.maquinaRepository = maquinaRepository;
    }

    /**
     * Inicia el ensamblaje de una lista de máquinas, utilizando el GaltonBoard.
     *
     * @param maquinasIds Lista de IDs de las máquinas a ensamblar.
     * @param galtonBoard El GaltonBoard que define la distribución de los componentes.
     * @return Mono<Void> señal de que el trabajo de ensamblaje ha finalizado.
     */
    public Mono<Void> iniciarTrabajo(List<String> maquinasIds, GaltonBoard galtonBoard) {
        return Flux.fromIterable(maquinasIds)
                .flatMap(maquinaId -> maquinaRepository.findById(maquinaId)
                        .flatMap(maquina -> obtenerOCrearMaquinaWorker(maquina, galtonBoard)
                                .flatMap(this::ensamblarMaquina) // Aquí llamamos a ensamblarMaquina para cada MaquinaWorker creada
                        ))
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

                    // Verificar que GaltonBoard no sea null antes de continuar
                    GaltonBoard galtonBoard = maquina.getGaltonBoard();
                    if (galtonBoard == null) {
                        System.err.println("Error: GaltonBoard es null para la máquina " + maquina.getTipo());
                        return Mono.error(new RuntimeException("GaltonBoard es null para la máquina " + maquina.getTipo()));
                    }

                    // Si no hay ComponenteWorker IDs, omitir el ensamblaje
                    if (maquinaWorker.getComponenteWorkerIds().isEmpty()) {
                        System.out.println("No hay componentes para ensamblar en la máquina " + maquina.getTipo());
                        return Mono.empty();
                    }

                    // Obtiene los ComponenteWorkers usando sus IDs de forma reactiva
                    return obtenerComponenteWorkers(maquinaWorker.getComponenteWorkerIds())
                            .flatMapMany(Flux::fromIterable)
                            .flatMap(componenteWorker ->
                                    componenteWorkerService.procesarComponente(componenteWorker, galtonBoard)
                            )
                            .collectList() // Recolecta todos los ComponenteWorkers procesados en una lista
                            .flatMap(processedComponents -> {
                                maquina.setEstado("ENSAMBLADA");
                                System.out.println("Máquina " + maquina.getTipo() + " ensamblada con éxito.");

                                // Imprimir detalles de los componentes ensamblados
                                System.out.println("Componentes ensamblados de la máquina " + maquina.getTipo() + ":");
                                processedComponents.forEach(componenteWorker -> {
                                    System.out.println("- Componente ID: " + componenteWorker.getComponente().getId() +
                                            ", Tipo: " + componenteWorker.getComponente().getTipo() +
                                            ", Estado ensamblado: " + componenteWorker.isEnsamblado());
                                });

                                return Mono.just(maquinaWorker); // Pasamos el MaquinaWorker al siguiente paso
                            })
                            .flatMap(maquinaWorkerRepository::save) // Guarda el MaquinaWorker actualizado en la base de datos
                            .doOnSuccess(savedWorker -> System.out.println("Estado del MaquinaWorker guardado para máquina " + maquina.getTipo()))
                            .doOnError(e -> System.err.println("Error ensamblando máquina " + maquina.getTipo() + ": " + e.getMessage()));
                }).then();
    }


}
