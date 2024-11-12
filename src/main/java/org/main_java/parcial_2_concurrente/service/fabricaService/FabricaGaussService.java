package org.main_java.parcial_2_concurrente.service.fabricaService;

import org.main_java.parcial_2_concurrente.domain.fabrica.FabricaGauss;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.Maquina;
import org.main_java.parcial_2_concurrente.domain.fabrica.maquina.maquinas_especificas.MaquinaDistribucionNormal;
import org.main_java.parcial_2_concurrente.domain.galton.GaltonBoard;
import org.main_java.parcial_2_concurrente.model.fabricaDTO.FabricaGaussDTO;
import org.main_java.parcial_2_concurrente.repos.fabrica.FabricaGaussRepository;
import org.main_java.parcial_2_concurrente.service.fabricaService.maquinaService.MaquinaWorkerService;
import org.main_java.parcial_2_concurrente.service.galtonService.GaltonBoardService;
import org.main_java.parcial_2_concurrente.service.messaging.RabbitMQService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class FabricaGaussService {

    private final FabricaGaussRepository fabricaGaussRepository;
    private final MaquinaWorkerService maquinaWorkerService;
    private final RabbitMQService rabbitMQService;
    private final GaltonBoardService galtonBoardService;

    public FabricaGaussService(FabricaGaussRepository fabricaGaussRepository,
                               MaquinaWorkerService maquinaWorkerService,
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

    /**
     * Inicia la producción completa en todas las fábricas.
     *
     * @return Mono<Void>
     */
    public Mono<Void> iniciarProduccionCompleta() {
        System.out.println("Iniciando producción completa en todas las fábricas.");

        return fabricaGaussRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        System.out.println("No se encontraron fábricas, inicializando fábricas predeterminadas.");

                        // Crear una instancia de MaquinaDistribucionNormal con valores predeterminados
                        MaquinaDistribucionNormal maquinaDistribucionNormal = new MaquinaDistribucionNormal();
                        maquinaDistribucionNormal.setTipo("MaquinaDistribucionNormal");
                        maquinaDistribucionNormal.setMedia(0.0);
                        maquinaDistribucionNormal.setDesviacionEstandar(1.5);
                        maquinaDistribucionNormal.setMaximoValor(10);

                        // Crear fábricas predeterminadas con la instancia de MaquinaDistribucionNormal
                        List<FabricaGauss> fabricasPredeterminadas = List.of(
                                new FabricaGauss(null, "Fábrica A", List.of(maquinaDistribucionNormal))
                        );

                        return fabricaGaussRepository.saveAll(fabricasPredeterminadas).then();
                    }
                    return Mono.empty();
                })
                .thenMany(fabricaGaussRepository.findAll())
                .flatMap(fabrica -> {
                    System.out.println("Procesando fábrica: " + fabrica.getNombre());

                    return obtenerOGenerarGaltonBoard(fabrica)
                            .flatMap(galtonBoard -> {
                                System.out.println("GaltonBoard preparado para la fábrica: " + fabrica.getNombre());

                                return galtonBoardService.simularCaidaDeBolas(galtonBoard)
                                        .then(galtonBoardService.actualizarDistribucion(galtonBoard, galtonBoard.getDistribucion().getDatos()))
                                        .then(maquinaWorkerService.iniciarTrabajo(fabrica.getMaquinas(), galtonBoard));
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
     * Obtiene el GaltonBoard asociado a la fábrica o crea uno nuevo si es necesario.
     *
     * @param fabrica La fábrica para la cual se requiere un GaltonBoard.
     * @return Mono<GaltonBoard> el GaltonBoard existente o generado.
     */
    private Mono<GaltonBoard> obtenerOGenerarGaltonBoard(FabricaGauss fabrica) {
        return galtonBoardService.obtenerGaltonBoardPorFabricaId(fabrica.getId())
                .switchIfEmpty(galtonBoardService.crearGaltonBoardParaFabrica(fabrica))
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
