package sobracero.backend.servicesImpl.donante;

import sobracero.backend.services.donante.DonanteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sobracero.backend.dtos.donante.DonacionRequestDto;
import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.models.Donacion;
import sobracero.backend.models.EstadoDonacion;
import sobracero.backend.models.Usuario;
import sobracero.backend.models.Reserva;
import sobracero.backend.models.EstadoReserva;
import sobracero.backend.models.HistorialEstado;
import sobracero.backend.repositories.DonacionRepository;
import sobracero.backend.repositories.UsuarioRepository;
import sobracero.backend.repositories.ReservaRepository;
import sobracero.backend.repositories.HistorialEstadoRepository;
import sobracero.backend.mappers.DonacionMapper;
import sobracero.backend.services.FileService;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonanteServiceImpl implements DonanteService {

    @Autowired
    private DonacionRepository donacionRepository;

    @Autowired
    private DonacionMapper donacionMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HistorialEstadoRepository historialEstadoRepository;

    @Autowired
    private FileService fileService;

    @Transactional
    @Override
    public DonacionResponseDto crearDonacion(DonacionRequestDto request, Long donanteId) {
        Usuario donante = usuarioRepository.findById(donanteId)
                .orElseThrow(() -> new RuntimeException("Donante no encontrado"));

        Donacion donacion = new Donacion();
        donacion.setNombre(request.getTipoAlimento());
        donacion.setCategoria(request.getTipoAlimento());
        donacion.setDescripcion(request.getTipoAlimento());
        donacion.setCantidadPorciones(request.getPorciones());
        donacion.setHoraLimiteRetiro(request.getHoraLimite());

        // Lógica actualizada: Prioridad a la ubicación del formulario
        if (request.getUbicacion() != null && !request.getUbicacion().trim().isEmpty()) {
            donacion.setUbicacion(request.getUbicacion());
        } else if (donante.getDireccion() != null && !donante.getDireccion().isEmpty()) {
            donacion.setUbicacion(donante.getDireccion());
        }
        // Última opción: Si no hay ninguna de las dos
        else {
            donacion.setUbicacion("Ubicación no especificada");
        }

        // NUEVO: Lógica para subir la imagen a MinIO
        MultipartFile imagen = request.getImagen();
        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = fileService.uploadImage(imagen);
            donacion.setImagenUrl(nombreArchivo); // Guardamos el nombre generado en la BD
        }

        donacion.setEstado(EstadoDonacion.PUBLICADO);
        donacion.setDonante(donante);

        donacion = donacionRepository.save(donacion);

        return donacionMapper.mapToDonacionResponseDto(donacion);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DonacionResponseDto> obtenerMisDonaciones(Long donanteId) {
        return donacionRepository.findByDonanteIdOrderByFechaPublicacionDesc(donanteId)
                .stream()
                .filter(d -> d.getEstado() != EstadoDonacion.CANCELADO)
                .map(donacion -> {
                    // 1. Mapeo inicial básico
                    DonacionResponseDto dto = donacionMapper.mapToDonacionResponseDto(donacion);

                    // 2. Si ya fue entregada, le inyectamos los datos detallados de la reserva
                    if (donacion.getEstado() == EstadoDonacion.ENTREGADO) {
                        reservaRepository.findAll().stream()
                                .filter(r -> r.getDonacion().getId().equals(donacion.getId())
                                        && r.getEstado() == EstadoReserva.COMPLETADA)
                                .findFirst()
                                .ifPresent(reserva -> {
                                    dto.setCodigoRetiro(reserva.getCodigoRetiro());
                                    dto.setFechaEntrega(reserva.getFechaEntrega());
                                    // AQUÍ ESTABA EL ERROR: Cambié getUsuario() por getOng()
                                    if (reserva.getOng() != null) {
                                        dto.setRazonSocialOng(reserva.getOng().getRazonSocial());
                                        dto.setDireccionOng(reserva.getOng().getDireccion());
                                    }
                                });
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void eliminarDonacion(Long donacionId, Long donanteId) {
        Donacion donacion = donacionRepository.findById(donacionId)
                .orElseThrow(() -> new RuntimeException("Donación no encontrada"));
        
        if (!donacion.getDonante().getId().equals(donanteId)) {
            throw new RuntimeException("No puedes eliminar una donación de otro local");
        }

        if (donacion.getEstado() == EstadoDonacion.ENTREGADO) {
            throw new RuntimeException("No puedes eliminar una donación que ya fue entregada");
        }

        EstadoDonacion estadoViejo = donacion.getEstado();
        donacion.setEstado(EstadoDonacion.CANCELADO);
        donacionRepository.save(donacion);
        
        reservaRepository.deleteByDonacionId(donacionId);
        
        HistorialEstado historial = new HistorialEstado();
        historial.setDonacion(donacion);
        historial.setEstadoAnterior(estadoViejo);
        historial.setEstadoNuevo(EstadoDonacion.CANCELADO);
        historial.setUsuarioQueCambio(donacion.getDonante());
        historialEstadoRepository.save(historial);
    }

    @Transactional
    @Override
    public DonacionResponseDto modificarDonacion(Long donacionId, DonacionRequestDto request, Long donanteId) {
        Donacion donacion = donacionRepository.findById(donacionId)
                .orElseThrow(() -> new RuntimeException("Donación no encontrada"));
        
        if (!donacion.getDonante().getId().equals(donanteId)) {
            throw new RuntimeException("No puedes modificar una donación de otro local");
        }

        if (donacion.getEstado() == EstadoDonacion.ENTREGADO || donacion.getEstado() == EstadoDonacion.CANCELADO) {
            throw new RuntimeException("No se puede modificar una donación en estado finalizado");
        }

        donacion.setNombre(request.getTipoAlimento());
        donacion.setCategoria(request.getTipoAlimento());
        donacion.setDescripcion(request.getTipoAlimento());
        donacion.setCantidadPorciones(request.getPorciones());
        donacion.setHoraLimiteRetiro(request.getHoraLimite());

        if (request.getUbicacion() != null && !request.getUbicacion().trim().isEmpty()) {
            donacion.setUbicacion(request.getUbicacion());
        }

        MultipartFile imagen = request.getImagen();
        if (imagen != null && !imagen.isEmpty()) {
            String nombreArchivo = fileService.uploadImage(imagen);
            donacion.setImagenUrl(nombreArchivo);
        }

        donacion = donacionRepository.save(donacion);

        return donacionMapper.mapToDonacionResponseDto(donacion);
    }

    @Transactional
    @Override
    public String validarRetiro(String codigoIngresado, Long donanteId) {
        if (codigoIngresado == null || codigoIngresado.trim().isEmpty()) {
            throw new RuntimeException("Debes ingresar un código válido.");
        }

        // 1. Limpieza inicial
        String codigoTemp = codigoIngresado.trim().toUpperCase();

        // 2. Creamos la variable final para que la expresión Lambda no marque error
        final String codigoLimpio = codigoTemp.startsWith("DON-") ? codigoTemp : "DON-" + codigoTemp;

        // 3. Búsqueda en la BD
        Reserva reserva = reservaRepository.findByCodigoRetiro(codigoLimpio)
                .orElseThrow(() -> new RuntimeException("El código " + codigoLimpio + " no se encontró en el sistema."));

        Donacion donacion = reserva.getDonacion();

        // 4. Validaciones de seguridad
        if (!donacion.getDonante().getId().equals(donanteId)) {
            throw new RuntimeException("Este código pertenece a una donación de otro local.");
        }

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new RuntimeException("Esta reserva no está activa (ya fue entregada o cancelada).");
        }

        // 5. Actualizamos la reserva
        reserva.setEstado(EstadoReserva.COMPLETADA);
        reserva.setFechaEntrega(LocalDateTime.now());
        reservaRepository.save(reserva);

        // 6. Actualizamos la donación
        donacion.setEstado(EstadoDonacion.ENTREGADO);
        donacionRepository.save(donacion);

        // 7. Guardamos el historial
        HistorialEstado historial = new HistorialEstado();
        historial.setDonacion(donacion);
        historial.setEstadoAnterior(EstadoDonacion.RESERVADO);
        historial.setEstadoNuevo(EstadoDonacion.ENTREGADO);
        Usuario donante = usuarioRepository.findById(donanteId).orElseThrow();
        historial.setUsuarioQueCambio(donante);
        historialEstadoRepository.save(historial);

        return "Retiro validado y completado exitosamente.";
    }
}