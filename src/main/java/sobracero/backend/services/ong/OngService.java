package sobracero.backend.services.ong;

import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.dtos.ong.ReservaRequestDto;
import sobracero.backend.dtos.ong.ReservaResponseDto;
import sobracero.backend.dtos.ong.ValidarRetiroRequestDto;
import java.util.List;

public interface OngService {
    List<DonacionResponseDto> obtenerCatalogo();
    DonacionResponseDto obtenerDonacion(Long id);
    ReservaResponseDto crearReserva(ReservaRequestDto requestDto, Long ongId);
    ReservaResponseDto obtenerReservaActiva(Long ongId);
    void cancelarReserva(Long reservaId, Long ongId);
    ReservaResponseDto validarRetiro(ValidarRetiroRequestDto request, Long ongId);
    List<ReservaResponseDto> obtenerHistorial(Long ongId);
}
