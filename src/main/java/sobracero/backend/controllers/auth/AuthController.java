package sobracero.backend.controllers.auth;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sobracero.backend.models.Rol;
import sobracero.backend.models.Usuario;
import sobracero.backend.services.auth.AuthService;
import sobracero.backend.security.JwtUtil;
import sobracero.backend.dtos.auth.CambioPasswordRequestDto;


import java.util.Map;



@RestController
@RequestMapping("/api/auth")
public class AuthController {


   private final AuthService authService;

   @org.springframework.beans.factory.annotation.Autowired
   private sobracero.backend.services.FileService fileService;


   @org.springframework.beans.factory.annotation.Autowired
   private JwtUtil jwtUtil;


   public AuthController(AuthService authService) {
       this.authService = authService;
   }


   @PostMapping("/login")
   public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
       try {
           String email = credentials.get("email");
           String password = credentials.get("password");


           Object authResult = authService.autenticar(email, password);


           if (authResult instanceof Usuario) {
               Usuario usuario = (Usuario) authResult;
               String token = jwtUtil.generateToken(usuario);
               return ResponseEntity.ok(Map.of(
                       "token", token,
                       "usuario", usuario
               ));
           } else {
               return ResponseEntity.ok(Map.of(
                       "mensaje", authResult,
                       "email", email,
                       "requiereOtp", true
               ));
           }
       } catch (Exception e) {
           return ResponseEntity.status(401).body(e.getMessage());
       }
   }


   @PostMapping("/verify-otp")
   public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
       try {
           String email = request.get("email");
           String otp = request.get("otp");


           Usuario usuario = authService.verificarOtp(email, otp);
           String token = jwtUtil.generateToken(usuario);
           return ResponseEntity.ok(Map.of(
                   "token", token,
                   "usuario", usuario
           ));
       } catch (Exception e) {
           return ResponseEntity.status(401).body(e.getMessage());
       }
   }


   @PostMapping("/registro")
   public ResponseEntity<?> registrar(
           @RequestParam("email") String email,
           @RequestParam("rol") String rol,
           @RequestParam("razonSocial") String razonSocial,
           @RequestParam("telefono") String telefono,
           @RequestParam(value = "direccion", required = false) String direccion,
           @RequestParam(value = "rut", required = false) String rut,
           @RequestParam(value = "comuna", required = false) String comuna,
           @RequestParam(value = "region", required = false) String region,
           @RequestParam(value = "nombreResponsable", required = false) String nombreResponsable,
           @RequestParam(value = "cargoResponsable", required = false) String cargoResponsable,
           @RequestParam(value = "documento", required = false) MultipartFile documento) {


       try {
           Usuario usuario = new Usuario();
           usuario.setEmail(email);
           usuario.setPassword("PENDIENTE_APROBACION");
           usuario.setRol(Rol.valueOf(rol));
           usuario.setRazonSocial(razonSocial);
           usuario.setTelefono(telefono);
           usuario.setDireccion(direccion);
           usuario.setRut(rut);
           usuario.setComuna(comuna);
           usuario.setRegion(region);
           usuario.setNombreResponsable(nombreResponsable);
           usuario.setCargoResponsable(cargoResponsable);


           if (documento != null && !documento.isEmpty()) {
               String nombreUnico = fileService.uploadEncryptedDocument(documento);
               usuario.setDocumentoUrl("/api/admin/usuarios/documento/" + nombreUnico);
           }


           return ResponseEntity.ok(authService.registrarUsuario(usuario));


       } catch (Exception e) {
           e.printStackTrace();
           return ResponseEntity.badRequest().body("Error en el registro: " + e.getMessage());
       }
   }


    @PutMapping("/{id}/cambiar-password-obligatorio")
    public ResponseEntity<?> cambiarPasswordObligatorio(
            @PathVariable Long id,
            @Valid @RequestBody CambioPasswordRequestDto requestDto) {

        // Ahora sí llamamos al método que ya existe en tu AuthService
        authService.cambiarPasswordPrimerIngreso(id, requestDto.getNuevaPassword());

        return ResponseEntity.ok().build();
    }


   @PostMapping("/forgot-password")
   public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
       try {
           String email = request.get("email");
           if (email == null || email.trim().isEmpty()) {
               return ResponseEntity.badRequest().body("El correo electrónico es requerido");
           }
           String mensaje = authService.solicitarRecuperacionPassword(email);
           return ResponseEntity.ok(Map.of("mensaje", mensaje));
       } catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
       }
   }


   @PostMapping("/reset-password")
   public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
       try {
           String token = request.get("token");
           String nuevaPassword = request.get("nuevaPassword");


           if (token == null || nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
               return ResponseEntity.badRequest().body("El token y la nueva contraseña son requeridos");
           }


           String mensaje = authService.restablecerPassword(token, nuevaPassword);
           return ResponseEntity.ok(Map.of("mensaje", mensaje));
       } catch (Exception e) {
           return ResponseEntity.status(401).body(e.getMessage());
       }
   }

   @DeleteMapping("/eliminar-cuenta")
   public ResponseEntity<?> eliminarMiCuenta() {
       try {
           Long usuarioId = (Long) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           authService.eliminarMiCuenta(usuarioId);
           return ResponseEntity.ok(Map.of("mensaje", "Cuenta eliminada correctamente"));
       } catch (Exception e) {
           return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
       }
   }

   @PutMapping("/actualizar-perfil")
   public ResponseEntity<?> actualizarMiPerfil(
           @RequestParam(value = "razonSocial", required = false) String razonSocial,
           @RequestParam(value = "email", required = false) String email,
           @RequestParam(value = "telefono", required = false) String telefono,
           @RequestParam(value = "direccion", required = false) String direccion,
           @RequestParam(value = "comuna", required = false) String comuna,
           @RequestParam(value = "region", required = false) String region,
           @RequestParam(value = "foto", required = false) MultipartFile foto) {
       try {
           Long usuarioId = (Long) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           
           Map<String, String> datos = new java.util.HashMap<>();
           if (razonSocial != null) datos.put("razonSocial", razonSocial);
           if (email != null) datos.put("email", email);
           if (telefono != null) datos.put("telefono", telefono);
           if (direccion != null) datos.put("direccion", direccion);
           if (comuna != null) datos.put("comuna", comuna);
           if (region != null) datos.put("region", region);

           if (foto != null && !foto.isEmpty()) {
               String nombreUnico = fileService.uploadImage(foto);
               datos.put("fotoUrl", nombreUnico);
           }

           Usuario usuarioActualizado = authService.actualizarMiPerfil(usuarioId, datos);
           return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado correctamente", "usuario", usuarioActualizado));
       } catch (Exception e) {
           return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
       }
   }
}
