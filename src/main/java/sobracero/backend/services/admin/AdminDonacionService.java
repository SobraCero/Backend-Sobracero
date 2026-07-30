package sobracero.backend.services.admin;

import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.dtos.admin.HistorialEstadoDto;
import java.util.List;

public interface AdminDonacionService {
    List<DonacionResponseDto> obtenerTodasLasDonaciones();
    List<HistorialEstadoDto> obtenerHistorialPublicaciones();
    void eliminarDonacion(Long id);
}
