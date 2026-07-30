package sobracero.backend.controllers.donante;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sobracero.backend.dtos.donante.DonacionRequestDto;
import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.services.donante.DonanteService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donante/donaciones")
public class DonanteController {

    @Autowired
    private DonanteService donanteService;

    private Long obtenerDonanteIdActual() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> publicarDonacion(@ModelAttribute DonacionRequestDto request) {
        try {
            Long donanteId = obtenerDonanteIdActual();
            DonacionResponseDto response = donanteService.crearDonacion(request, donanteId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al publicar la donación");
        }
    }

    @GetMapping
    public ResponseEntity<List<DonacionResponseDto>> obtenerMisDonaciones() {
        Long donanteId = obtenerDonanteIdActual();
        List<DonacionResponseDto> donaciones = donanteService.obtenerMisDonaciones(donanteId);
        return ResponseEntity.ok(donaciones);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> modificarDonacion(@PathVariable Long id, @ModelAttribute DonacionRequestDto request) {
        try {
            Long donanteId = obtenerDonanteIdActual();
            DonacionResponseDto response = donanteService.modificarDonacion(id, request, donanteId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error interno al modificar la donación"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDonacion(@PathVariable Long id) {
        try {
            Long donanteId = obtenerDonanteIdActual();
            donanteService.eliminarDonacion(id, donanteId);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Donación eliminada"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error interno al eliminar la donación"));
        }
    }

    @PostMapping("/validar-retiro")
    public ResponseEntity<?> validarRetiro(@RequestBody Map<String, String> request) {
        Long donanteId = obtenerDonanteIdActual();
        try {
            String codigo = request.get("codigo");
            String mensaje = donanteService.validarRetiro(codigo, donanteId);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", mensaje));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error interno al validar el retiro"));
        }
    }
}