package sobracero.backend.dtos.ong;

import lombok.Data;
import sobracero.backend.models.EstadoDonacion;
import java.time.LocalDateTime;

@Data
public class DonacionResponseDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private Integer cantidadPorciones;
    private LocalDateTime horaLimiteRetiro;
    private EstadoDonacion estado;
    private String ubicacion; // Esta es la dirección de retiro (donde va la ONG)
    private Long donanteId;
    private String razonSocialDonante;
    private String razonSocialOng;
    private LocalDateTime fechaPublicacion;
    private String imagenUrl;

    // --- NUEVOS CAMPOS PARA EL DETALLE DEL HISTORIAL ---
    private String codigoRetiro;
    private LocalDateTime fechaEntrega;
    private String direccionOng; // Para el mapa o info adicional de la ONG
}