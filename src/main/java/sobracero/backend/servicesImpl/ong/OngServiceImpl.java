package sobracero.backend.servicesImpl.ong;

import sobracero.backend.services.ong.OngService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sobracero.backend.dtos.ong.*;
import sobracero.backend.models.*;
import sobracero.backend.repositories.*;
import sobracero.backend.services.email.EmailService;
import sobracero.backend.mappers.DonacionMapperToOng;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OngServiceImpl implements OngService {

    @Autowired
    private DonacionRepository donacionRepository;

    @Autowired
    private DonacionMapperToOng donacionMapperToOng;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HistorialEstadoRepository historialEstadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Transactional(readOnly = true)
    @Override
    public List<DonacionResponseDto> obtenerCatalogo() {
        return donacionRepository.findByEstado(EstadoDonacion.PUBLICADO)
                .stream()
                .map(donacionMapperToOng::mapToDonacionResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public DonacionResponseDto obtenerDonacion(Long id) {
        Donacion donacion = donacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donación no encontrada"));
        return donacionMapperToOng.mapToDonacionResponseDto(donacion);
    }

    @Transactional
    @Override
    public ReservaResponseDto crearReserva(ReservaRequestDto requestDto, Long ongId) {
        // Utilizamos la consulta con bloqueo para evitar que otra ONG lea la donación simultáneamente
        Donacion donacion = donacionRepository.findByIdForUpdate(requestDto.getDonacionId())
                .orElseThrow(() -> new RuntimeException("Donación no encontrada"));

        if (donacion.getEstado() != EstadoDonacion.PUBLICADO) {
            throw new RuntimeException("La donación ya no está disponible");
        }

        Usuario ong = usuarioRepository.findById(ongId)
                .orElseThrow(() -> new RuntimeException("ONG no encontrada"));

        donacion.setEstado(EstadoDonacion.RESERVADO);
        donacion.setOng(ong);
        donacionRepository.save(donacion);

        Reserva reserva = new Reserva();
        reserva.setDonacion(donacion);
        reserva.setOng(ong);
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setCodigoRetiro(generarCodigoUnico());
        reserva = reservaRepository.save(reserva);

        HistorialEstado historial = new HistorialEstado();
        historial.setDonacion(donacion);
        historial.setEstadoAnterior(EstadoDonacion.PUBLICADO);
        historial.setEstadoNuevo(EstadoDonacion.RESERVADO);
        historial.setUsuarioQueCambio(ong);
        historialEstadoRepository.save(historial);

        // Enviar correos al Donante y a la ONG con el código
        emailService.enviarCodigoReserva(
                ong.getEmail(),
                donacion.getDonante().getEmail(),
                reserva.getCodigoRetiro(),
                donacion.getNombre()
        );

        return donacionMapperToOng.mapToReservaResponseDto(reserva);
    }

    @Transactional(readOnly = true)
    @Override
    public ReservaResponseDto obtenerReservaActiva(Long ongId) {
        Reserva reserva = reservaRepository.findByOngIdAndEstado(ongId, EstadoReserva.ACTIVA)
                .orElseThrow(() -> new RuntimeException("No tienes ninguna reserva activa"));
        return donacionMapperToOng.mapToReservaResponseDto(reserva);
    }

    @Transactional
    @Override
    public void cancelarReserva(Long reservaId, Long ongId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!reserva.getOng().getId().equals(ongId)) {
            throw new RuntimeException("Esta reserva no pertenece a tu ONG");
        }

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new RuntimeException("Solo se pueden cancelar reservas activas");
        }

        Donacion donacion = reserva.getDonacion();

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);

        donacion.setEstado(EstadoDonacion.PUBLICADO);
        donacion.setOng(null);
        donacionRepository.save(donacion);

        HistorialEstado historial = new HistorialEstado();
        historial.setDonacion(donacion);
        historial.setEstadoAnterior(EstadoDonacion.RESERVADO);
        historial.setEstadoNuevo(EstadoDonacion.PUBLICADO);
        Usuario ong = usuarioRepository.findById(ongId).orElseThrow();
        historial.setUsuarioQueCambio(ong);
        historialEstadoRepository.save(historial);
    }

    @Transactional
    @Override
    public ReservaResponseDto validarRetiro(ValidarRetiroRequestDto request, Long ongId) {
        Reserva reserva = reservaRepository.findByCodigoRetiro(request.getCodigo())
                .orElseThrow(() -> new RuntimeException("Código de retiro inválido"));

        if (!reserva.getOng().getId().equals(ongId)) {
            throw new RuntimeException("Esta reserva no pertenece a tu ONG");
        }

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new RuntimeException("La reserva no está activa");
        }

        Donacion donacion = reserva.getDonacion();

        reserva.setEstado(EstadoReserva.COMPLETADA);
        reserva.setFechaEntrega(LocalDateTime.now());
        reservaRepository.save(reserva);

        donacion.setEstado(EstadoDonacion.ENTREGADO);
        donacionRepository.save(donacion);

        HistorialEstado historial = new HistorialEstado();
        historial.setDonacion(donacion);
        historial.setEstadoAnterior(EstadoDonacion.RESERVADO);
        historial.setEstadoNuevo(EstadoDonacion.ENTREGADO);
        Usuario ong = usuarioRepository.findById(ongId).orElseThrow();
        historial.setUsuarioQueCambio(ong);
        historialEstadoRepository.save(historial);

        return donacionMapperToOng.mapToReservaResponseDto(reserva);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservaResponseDto> obtenerHistorial(Long ongId) {
        return reservaRepository.findByOngIdAndEstadoOrderByFechaReservaDesc(ongId, EstadoReserva.COMPLETADA)
                .stream()
                .map(donacionMapperToOng::mapToReservaResponseDto)
                .collect(Collectors.toList());
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = "DON-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (reservaRepository.existsByCodigoRetiro(codigo));
        return codigo;
    }
}