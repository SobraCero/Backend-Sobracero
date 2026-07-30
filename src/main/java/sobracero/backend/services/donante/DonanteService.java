package sobracero.backend.services.donante;

import sobracero.backend.dtos.donante.DonacionRequestDto;
import sobracero.backend.dtos.ong.DonacionResponseDto;
import java.util.List;

public interface DonanteService {
    DonacionResponseDto crearDonacion(DonacionRequestDto request, Long donanteId);
    List<DonacionResponseDto> obtenerMisDonaciones(Long donanteId);
    void eliminarDonacion(Long donacionId, Long donanteId);
    DonacionResponseDto modificarDonacion(Long donacionId, DonacionRequestDto request, Long donanteId);
    String validarRetiro(String codigoIngresado, Long donanteId);
}
