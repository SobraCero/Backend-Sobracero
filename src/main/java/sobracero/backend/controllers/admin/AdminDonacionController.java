package sobracero.backend.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.services.admin.AdminDonacionService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/donaciones")
public class AdminDonacionController {

    @Autowired
    private AdminDonacionService adminDonacionService;

    @GetMapping
    public ResponseEntity<List<DonacionResponseDto>> listarTodas() {
        return ResponseEntity.ok(adminDonacionService.obtenerTodasLasDonaciones());
    }

    @GetMapping("/historial")
    public ResponseEntity<List<sobracero.backend.dtos.admin.HistorialEstadoDto>> obtenerHistorial() {
        return ResponseEntity.ok(adminDonacionService.obtenerHistorialPublicaciones());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDonacion(@PathVariable Long id) {
        adminDonacionService.eliminarDonacion(id);
        return ResponseEntity.noContent().build();
    }
}
