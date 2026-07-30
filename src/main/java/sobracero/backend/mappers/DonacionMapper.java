package sobracero.backend.mappers;

import org.springframework.stereotype.Component;
import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.models.Donacion;

@Component
public class DonacionMapper {

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

        if (donacion.getDonante() != null) {
            dto.setDonanteId(donacion.getDonante().getId());
            dto.setRazonSocialDonante(donacion.getDonante().getRazonSocial());
        }

        if (donacion.getOng() != null) {
            dto.setRazonSocialOng(donacion.getOng().getRazonSocial());
        }

        dto.setFechaPublicacion(donacion.getFechaPublicacion());

        dto.setImagenUrl(donacion.getImagenUrl());

        return dto;
    }
}
