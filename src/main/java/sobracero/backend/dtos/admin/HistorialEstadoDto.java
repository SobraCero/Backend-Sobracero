package sobracero.backend.dtos.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistorialEstadoDto {
    private Long id;
    private Long donacionId;
    private String donacionNombre;
    private String descripcion;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
    private String usuarioQueCambioNombre;
    private String usuarioQueCambioRol;
    private String donanteNombre;
    private String ongNombre;
    private String codigoRetiro;
    private Integer cantidadPorciones;
    private LocalDateTime horaLimiteRetiro;
    private String imagenUrl;
    private String ubicacionDonante;
}