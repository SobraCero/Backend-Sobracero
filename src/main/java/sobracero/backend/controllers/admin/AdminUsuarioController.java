package sobracero.backend.controllers.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sobracero.backend.models.Usuario;
import sobracero.backend.services.admin.AdminUsuarioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;
    
    @org.springframework.beans.factory.annotation.Autowired
    private sobracero.backend.services.FileService fileService;

    public AdminUsuarioController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Usuario>> listarPendientes() {
        return ResponseEntity.ok(adminUsuarioService.obtenerUsuariosPendientes());
    }

    @GetMapping("/documento/{fileName}")
    public ResponseEntity<byte[]> verDocumento(@PathVariable String fileName) {
        try {
            byte[] decryptedData = fileService.downloadEncryptedDocument(fileName);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName.replace(".enc", "") + "\"")
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(decryptedData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Usuario> aprobarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminUsuarioService.aprobarUsuario(id));
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosUsuarios() {
        return ResponseEntity.ok(adminUsuarioService.obtenerTodosLosUsuarios());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            adminUsuarioService.eliminarUsuario(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/editar")
    public ResponseEntity<?> editarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        try {
            return ResponseEntity.ok(adminUsuarioService.actualizarUsuario(id, usuario));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }
}
