package sobracero.backend.dtos.ong;

import lombok.Data;
import sobracero.backend.models.EstadoReserva;

import java.time.LocalDateTime;

@Data
public class ReservaResponseDto {
    private Long id;
    private Long donacionId;
    private String nombreDonacion;
    private String codigoRetiro;
    private String direccion;
    private LocalDateTime horaLimite;
    private String nombreDonante;
    private EstadoReserva estado;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaEntrega;
}
