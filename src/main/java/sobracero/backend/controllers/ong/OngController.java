package sobracero.backend.controllers.ong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sobracero.backend.dtos.ong.*;
import sobracero.backend.services.ong.OngService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ong")
public class OngController {

    @Autowired
    private OngService ongService;

    private Long obtenerOngIdActual() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<DonacionResponseDto>> obtenerCatalogo() {
        return ResponseEntity.ok(ongService.obtenerCatalogo());
    }

    @GetMapping("/catalogo/{id}")
    public ResponseEntity<DonacionResponseDto> obtenerDetalleDonacion(@PathVariable Long id) {
        return ResponseEntity.ok(ongService.obtenerDonacion(id));
    }

    @PostMapping("/reservas")
    public ResponseEntity<?> crearReserva(@RequestBody ReservaRequestDto request) {
        try {
            Long id = obtenerOngIdActual();
            return ResponseEntity.ok(ongService.crearReserva(request, id));
        } catch (RuntimeException e) {
            // Ahora, en lugar de un error 500, devolveremos un 400 con el mensaje amigable
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/reserva-activa")
    public ResponseEntity<?> obtenerReservaActiva() {
        try {
            Long id = obtenerOngIdActual();
            return ResponseEntity.ok(ongService.obtenerReservaActiva(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id) {
        try {
            Long ongId = obtenerOngIdActual();
            ongService.cancelarReserva(id, ongId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/validar-retiro")
    public ResponseEntity<ReservaResponseDto> validarRetiro(@RequestBody ValidarRetiroRequestDto request) {
        Long id = obtenerOngIdActual();
        return ResponseEntity.ok(ongService.validarRetiro(request, id));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<ReservaResponseDto>> obtenerHistorial() {
        Long id = obtenerOngIdActual();
        return ResponseEntity.ok(ongService.obtenerHistorial(id));
    }
}
