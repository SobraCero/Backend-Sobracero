package sobracero.backend.dtos.donante;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class DonacionRequestDto {
    private String tipoAlimento;
    private Integer porciones;
    private LocalDateTime horaLimite;
    private String ubicacion;
    private MultipartFile imagen;
}