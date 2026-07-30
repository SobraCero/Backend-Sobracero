package sobracero.backend.servicesImpl.admin;

import sobracero.backend.services.admin.AdminDonacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORTANTE
import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.mappers.DonacionMapper;
import sobracero.backend.repositories.DonacionRepository;
import sobracero.backend.repositories.HistorialEstadoRepository;
import sobracero.backend.repositories.ReservaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDonacionServiceImpl implements AdminDonacionService {

    @Autowired
    private DonacionRepository donacionRepository;

    @Autowired
    private DonacionMapper donacionMapper;

    @Autowired
    private HistorialEstadoRepository historialEstadoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private sobracero.backend.services.email.EmailService emailService;

    @Transactional(readOnly = true)
    @Override
    public List<DonacionResponseDto> obtenerTodasLasDonaciones() {
        return donacionRepository.findAllByOrderByFechaPublicacionDesc()
                .stream()
                .filter(d -> d.getEstado() != sobracero.backend.models.EstadoDonacion.CANCELADO)
                .map(donacionMapper::mapToDonacionResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<sobracero.backend.dtos.admin.HistorialEstadoDto> obtenerHistorialPublicaciones() {
        return historialEstadoRepository.findAllByOrderByFechaCambioDesc()
                .stream()
                .filter(h -> h.getEstadoNuevo() == sobracero.backend.models.EstadoDonacion.ENTREGADO)
                .map(historial -> {
                    sobracero.backend.dtos.admin.HistorialEstadoDto dto = new sobracero.backend.dtos.admin.HistorialEstadoDto();
                    dto.setId(historial.getId());

                    if (historial.getDonacion() != null) {
                        dto.setDonacionId(historial.getDonacion().getId());
                        dto.setDonacionNombre(historial.getDonacion().getNombre());
                        dto.setDescripcion(historial.getDonacion().getDescripcion());
                        dto.setDonanteNombre(historial.getDonacion().getDonante() != null ? historial.getDonacion().getDonante().getRazonSocial() : "N/A");

                        // Mapeo de campos críticos para el detalle
                        dto.setCantidadPorciones(historial.getDonacion().getCantidadPorciones());
                        dto.setHoraLimiteRetiro(historial.getDonacion().getHoraLimiteRetiro());
                        dto.setImagenUrl(historial.getDonacion().getImagenUrl());
                        dto.setUbicacionDonante(historial.getDonacion().getUbicacion());

                        // Buscar la reserva completada asociada para obtener el código y la ONG
                        reservaRepository.findAll().stream()
                                .filter(r -> r.getDonacion().getId().equals(historial.getDonacion().getId())
                                        && r.getEstado() == sobracero.backend.models.EstadoReserva.COMPLETADA)
                                .findFirst()
                                .ifPresentOrElse(reserva -> {
                                    dto.setCodigoRetiro(reserva.getCodigoRetiro());
                                    dto.setOngNombre(reserva.getOng() != null ? reserva.getOng().getRazonSocial() : "N/A");
                                }, () -> {
                                    dto.setCodigoRetiro("N/A");
                                    dto.setOngNombre(historial.getDonacion().getOng() != null ? historial.getDonacion().getOng().getRazonSocial() : "N/A");
                                });

                    } else {
                        dto.setDonacionNombre("N/A");
                        dto.setDonanteNombre("N/A");
                        dto.setOngNombre("N/A");
                        dto.setCodigoRetiro("N/A");
                    }

                    dto.setEstadoAnterior(historial.getEstadoAnterior() != null ? historial.getEstadoAnterior().name() : null);
                    dto.setEstadoNuevo(historial.getEstadoNuevo() != null ? historial.getEstadoNuevo().name() : null);
                    dto.setFechaCambio(historial.getFechaCambio());

                    if (historial.getUsuarioQueCambio() != null) {
                        dto.setUsuarioQueCambioNombre(historial.getUsuarioQueCambio().getRazonSocial());
                        dto.setUsuarioQueCambioRol(historial.getUsuarioQueCambio().getRol().name());
                    } else {
                        dto.setUsuarioQueCambioNombre("Sistema");
                        dto.setUsuarioQueCambioRol("N/A");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void eliminarDonacion(Long id) {
        sobracero.backend.models.Donacion donacion = donacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donación no encontrada con ID: " + id));
        
        String emailDonante = donacion.getDonante().getEmail();
        String nombreDonacion = donacion.getNombre();
        sobracero.backend.models.EstadoDonacion estadoViejo = donacion.getEstado();
        
        // No borramos la donacion, solo la cancelamos (Soft Delete) para mantener el historial
        donacion.setEstado(sobracero.backend.models.EstadoDonacion.CANCELADO);
        donacionRepository.save(donacion);
        
        // Se borran las reservas activas (si existiera alguna)
        reservaRepository.deleteByDonacionId(id);
        
        // Se añade un registro al historial de que el Admin la canceló
        sobracero.backend.models.HistorialEstado historial = new sobracero.backend.models.HistorialEstado();
        historial.setDonacion(donacion);
        historial.setEstadoAnterior(estadoViejo);
        historial.setEstadoNuevo(sobracero.backend.models.EstadoDonacion.CANCELADO);
        historialEstadoRepository.save(historial);

        // Enviar correo de notificación
        emailService.enviarNotificacionEliminacionDonacion(emailDonante, nombreDonacion);
    }
}