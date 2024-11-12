package org.main_java.parcial_2_concurrente.service.fabricaService;

import org.main_java.parcial_2_concurrente.aop.sync.SynchronizedExecution;
import org.main_java.parcial_2_concurrente.domain.fabrica.FabricaGauss;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.componentes.Componente;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.maquinas_especificas.MaquinaDistribucionNormal;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoardStatus;
import org.main_java.parcial_2_concurrente.model.fabricaDTO.FabricaGaussDTO;
import org.main_java.parcial_2_concurrente.repos.fabrica.FabricaGaussRepository;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.MaquinaRepository;
import org.main_java.parcial_2_concurrente.repos.fabrica.maquina.componente.ComponenteRepository;
import org.main_java.parcial_2_concurrente.service.fabricaService.maquinaService.MaquinaWorkerService;
import org.main_java.parcial_2_concurrente.service.galtonService.GaltonBoardService;
import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FabricaGaussService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private ComponenteRepository componenteRepository;

    private final FabricaGaussRepository fabricaGaussRepository;
    private final MaquinaWorkerService maquinaWorkerService;
    private final RabbitMQService rabbitMQService;
    private final GaltonBoardService galtonBoardService;

    public FabricaGaussService(FabricaGaussRepository fabricaGaussRepository,
                               @Lazy MaquinaWorkerService maquinaWorkerService,
                               RabbitMQService rabbitMQService,
                               GaltonBoardService galtonBoardService) {
        this.fabricaGaussRepository = fabricaGaussRepository;
        this.maquinaWorkerService = maquinaWorkerService;
        this.rabbitMQService = rabbitMQService;
        this.galtonBoardService = galtonBoardService;
    }

    public Mono<FabricaGaussDTO> createFabrica(FabricaGaussDTO fabricaGaussDTO) {
        FabricaGauss fabrica = mapToEntity(fabricaGaussDTO);
        return fabricaGaussRepository.save(fabrica)
                .map(this::mapToDTO)
                .doOnError(e -> System.err.println("Error creating Fabrica: " + e.getMessage()));
    }

    public Flux<FabricaGaussDTO> findAllFabricas() {
        return fabricaGaussRepository.findAll()
                .map(this::mapToDTO)
                .doOnError(e -> System.err.println("Error fetching Fabricas: " + e.getMessage()));
    }

    public Mono<FabricaGaussDTO> findFabricaById(String id) {
        return fabricaGaussRepository.findById(id)
                .map(this::mapToDTO)
                .switchIfEmpty(Mono.error(new RuntimeException("Fabrica not found")))
                .doOnError(e -> System.err.println("Error fetching Fabrica by ID: " + e.getMessage()));
    }

    public Mono<Void> deleteFabricaById(String id) {
        return fabricaGaussRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Fabrica not found")))
                .flatMap(fabricaGaussRepository::delete)
                .doOnError(e -> System.err.println("Error deleting Fabrica: " + e.getMessage()));
    }

    public Mono<FabricaGaussDTO> updateFabrica(String id, FabricaGaussDTO fabricaGaussDTO) {
        return fabricaGaussRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Fabrica not found")))
                .flatMap(existingFabrica -> {
                    existingFabrica.setNombre(fabricaGaussDTO.getNombre());
                    return fabricaGaussRepository.save(existingFabrica);
                })
                .map(this::mapToDTO)
                .doOnError(e -> System.err.println("Error updating Fabrica: " + e.getMessage()));
    }

    public Mono<Void> iniciarProduccionCompleta() {
        System.out.println("Iniciando producción completa en todas las fábricas.");

        return fabricaGaussRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        System.out.println("No se encontraron fábricas, inicializando fábricas predeterminadas.");

                        // Crear y configurar MaquinaDistribucionNormal
                        MaquinaDistribucionNormal maquinaDistribucionNormal = new MaquinaDistribucionNormal();
                        maquinaDistribucionNormal.setTipo("MaquinaDistribucionNormal");
                        maquinaDistribucionNormal.setMedia(5.0);
                        maquinaDistribucionNormal.setDesviacionEstandar(0.3);
                        maquinaDistribucionNormal.setMaximoValor(10);
                        maquinaDistribucionNormal.setGaltonBoard(null);  // Inicialmente sin GaltonBoard

                        int numeroComponentesRequeridos = 10;
                        maquinaDistribucionNormal.setNumeroComponentesRequeridos(numeroComponentesRequeridos);

                        // Crear componentes y recolectar sus IDs
                        List<Componente> componentes = IntStream.range(0, numeroComponentesRequeridos)
                                .mapToObj(i -> {
                                    Componente componente = new Componente();
                                    componente.setTipo("TipoComponente_" + i);
                                    componente.setValorCalculado(0.0);
                                    componente.setMaquinaId(maquinaDistribucionNormal.getId());
                                    return componente;
                                })
                                .collect(Collectors.toList());

                        return componenteRepository.saveAll(componentes)
                                .map(Componente::getId)
                                .collectList()
                                .flatMap(componentesIds -> {
                                    maquinaDistribucionNormal.setComponentesIds(componentesIds);
                                    return maquinaRepository.save(maquinaDistribucionNormal);
                                })
                                .flatMap(maquinaGuardada -> {
                                    if (maquinaGuardada.getId() == null) {
                                        return Mono.error(new RuntimeException("La máquina guardada no tiene un ID asignado."));
                                    }
                                    System.out.println("Máquina guardada con ID: " + maquinaGuardada.getId());

                                    FabricaGauss fabrica = new FabricaGauss(null, "Fábrica A", List.of(maquinaGuardada.getId()));
                                    return fabricaGaussRepository.save(fabrica)
                                            .doOnSuccess(f -> System.out.println("Fábrica guardada con nombre: " + f.getNombre()));
                                });
                    }
                    return Mono.empty();
                })
                .thenMany(fabricaGaussRepository.findAll())
                .flatMap(fabrica -> {
                    System.out.println("Procesando fábrica: " + fabrica.getNombre());

                    return obtenerOGenerarGaltonBoard(fabrica)
                            .flatMap(galtonBoard -> {
                                System.out.println("GaltonBoard preparado para la fábrica: " + fabrica.getNombre());

                                return maquinaRepository.findById(fabrica.getMaquinasIds().get(0))
                                        .flatMap(maquina -> {
                                            maquina.setGaltonBoard(galtonBoard);
                                            return maquinaRepository.save(maquina);
                                        })
                                        .then(galtonBoardService.actualizarDistribucion(galtonBoard, galtonBoard.getDistribucion().getDatos())) // Solo actualizamos distribución después de la simulación
                                        .then(maquinaWorkerService.iniciarTrabajo(fabrica.getMaquinasIds(), galtonBoard));
                            });
                })
                .collectList()
                .then(Mono.defer(() -> {
                    String mensaje = "Producción completa en todas las fábricas.";
                    System.out.println("Enviando mensaje de finalización a RabbitMQ: " + mensaje);
                    return rabbitMQService.enviarMensaje("produccion_queue", mensaje)
                            .doOnSuccess(v -> System.out.println("Producción completa en todas las fábricas. Notificación enviada."));
                }))
                .doOnError(e -> System.err.println("Error en iniciarProduccionCompleta: " + e.getMessage()));
    }

    /**
     * Actualiza cada componente de la máquina con los valores de la distribución en el GaltonBoard.
     *
     * @param maquinaId ID de la máquina cuyos componentes se actualizarán.
     * @param galtonBoard El GaltonBoard que contiene la distribución.
     * @return Mono<Void> que indica el final de la actualización.
     */
    @SynchronizedExecution
    public Mono<Void> actualizarComponentesConDistribucion(String maquinaId, GaltonBoard galtonBoard) {
        // Esperar que la simulación de caída de bolas esté completa antes de proceder
        return galtonBoardService.esperarSimulacionCompletada()
                .then(Mono.defer(() -> {
                    System.out.println("Actualizando componentes de la máquina " + maquinaId + " con la distribución de bolas.");
                    Map<String, Integer> distribucion = galtonBoard.getDistribucion().getDatos();

                    return maquinaRepository.findById(maquinaId)
                            .flatMapMany(maquina -> {
                                List<String> componentesIds = maquina.getComponentesIds();

                                return Flux.range(0, componentesIds.size())
                                        .flatMap(index -> {
                                            String componenteId = componentesIds.get(index);
                                            String contenedorKey = "contenedor_" + (index % distribucion.size());

                                            Integer bolasEnContenedor = distribucion.getOrDefault(contenedorKey, 0);
                                            System.out.println("Asignando " + bolasEnContenedor + " bolas al componente con ID: " + componenteId);

                                            return componenteRepository.findById(componenteId)
                                                    .flatMap(componente -> {
                                                        componente.setValorCalculado(bolasEnContenedor.doubleValue());
                                                        return componenteRepository.save(componente)
                                                                .doOnSuccess(c -> System.out.println("Componente " + c.getId() + " actualizado con " + bolasEnContenedor + " bolas."));
                                                    });
                                        });
                            })
                            .then()
                            .doOnSuccess(v -> System.out.println("Todos los componentes de la máquina " + maquinaId + " han sido actualizados con la distribución de bolas."))
                            .doOnError(e -> System.err.println("Error al actualizar componentes con la distribución: " + e.getMessage()));
                }));
    }

    private Mono<GaltonBoard> obtenerOGenerarGaltonBoard(FabricaGauss fabrica) {
        return galtonBoardService.obtenerGaltonBoardPorFabricaId(fabrica.getId())
                .switchIfEmpty(galtonBoardService.crearGaltonBoardParaFabrica(fabrica)
                        .flatMap(galtonBoard -> {
                            if (galtonBoard.getEstado() == null) {
                                galtonBoard.setStatus(new GaltonBoardStatus("EN_PROGRESO", new HashMap<>()));
                            }
                            // Explicitly save the GaltonBoard to ensure it gets an ID
                            return galtonBoardService.guardarGaltonBoard(galtonBoard);
                        })
                )
                .doOnSuccess(galtonBoard -> System.out.println("GaltonBoard obtenido/creado para la fábrica: " + fabrica.getNombre()))
                .doOnError(e -> System.err.println("Error al obtener/crear GaltonBoard para la fábrica: " + fabrica.getNombre() + ". Error: " + e.getMessage()));
    }



    private FabricaGaussDTO mapToDTO(FabricaGauss fabrica) {
        FabricaGaussDTO dto = new FabricaGaussDTO();
        dto.setId(fabrica.getId());
        dto.setNombre(fabrica.getNombre());
        return dto;
    }

    private FabricaGauss mapToEntity(FabricaGaussDTO fabricaGaussDTO) {
        FabricaGauss fabrica = new FabricaGauss();
        fabrica.setNombre(fabricaGaussDTO.getNombre());
        return fabrica;
    }
}
