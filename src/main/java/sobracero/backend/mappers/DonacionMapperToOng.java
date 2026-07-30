package sobracero.backend.mappers;

import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.dtos.ong.ReservaResponseDto;
import sobracero.backend.models.Donacion;
import sobracero.backend.models.Reserva;
import org.springframework.stereotype.Component;

@Component
public class DonacionMapperToOng {
    public DonacionResponseDto mapToDonacionResponseDto(Donacion donacion) {
        DonacionResponseDto dto = new DonacionResponseDto();
        dto.setId(donacion.getId());
        dto.setNombre(donacion.getNombre());
        dto.setDescripcion(donacion.getDescripcion());
        dto.setCategoria(donacion.getCategoria());
        dto.setCantidadPorciones(donacion.getCantidadPorciones());
        dto.setHoraLimiteRetiro(donacion.getHoraLimiteRetiro());
        dto.setEstado(donacion.getEstado());
        dto.setUbicacion(donacion.getUbicacion());
        dto.setDonanteId(donacion.getDonante().getId());
        dto.setRazonSocialDonante(donacion.getDonante().getRazonSocial());
        if (donacion.getOng() != null) {
            dto.setRazonSocialOng(donacion.getOng().getRazonSocial());
        }
        dto.setFechaPublicacion(donacion.getFechaPublicacion());
        dto.setImagenUrl(donacion.getImagenUrl());

        return dto;
    }

    public ReservaResponseDto mapToReservaResponseDto(Reserva reserva) {
        ReservaResponseDto dto = new ReservaResponseDto();
        dto.setId(reserva.getId());
        dto.setDonacionId(reserva.getDonacion().getId());
        dto.setNombreDonacion(reserva.getDonacion().getNombre());
        dto.setCodigoRetiro(reserva.getCodigoRetiro());
        dto.setDireccion(reserva.getDonacion().getUbicacion());
        dto.setHoraLimite(reserva.getDonacion().getHoraLimiteRetiro());
        dto.setNombreDonante(reserva.getDonacion().getDonante().getRazonSocial());
        dto.setEstado(reserva.getEstado());
        dto.setFechaReserva(reserva.getFechaReserva());
        dto.setFechaEntrega(reserva.getFechaEntrega());
        return dto;
    }
}