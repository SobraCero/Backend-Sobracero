package sobracero.backend.servicesImpl.auth;

import sobracero.backend.services.auth.AuthService;


import org.springframework.stereotype.Service;
import sobracero.backend.models.EstadoCuenta;
import sobracero.backend.models.Usuario;
import sobracero.backend.repositories.UsuarioRepository;
import sobracero.backend.services.email.EmailService;


import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.Map;


@Service
public class AuthServiceImpl implements AuthService {


    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;


    public AuthServiceImpl(UsuarioRepository usuarioRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }


    @Override
    public Usuario registrarUsuario(Usuario nuevoUsuario) {
        nuevoUsuario.setEstado(EstadoCuenta.PENDIENTE);
        nuevoUsuario.setDebeCambiarPassword(false);
        return usuarioRepository.save(nuevoUsuario);
    }


    @Override
    public Object autenticar(String email, String password) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().trim().equalsIgnoreCase(email.trim()) && u.getPassword().equals(password))
                .filter(u -> u.getEstado() == EstadoCuenta.ACTIVO)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas o cuenta inactiva"));


        if (usuario.getRol() == sobracero.backend.models.Rol.ADMIN) {
            return usuario;
        }


        String otp = String.format("%06d", new Random().nextInt(999999));
        usuario.setCodigoOtp(otp);
        usuario.setFechaExpiracionOtp(LocalDateTime.now().plusMinutes(5));
        usuarioRepository.save(usuario);


        emailService.enviarOtp(usuario.getEmail(), otp);


        return "OTP enviado al correo";
    }


    @Override
    public Usuario verificarOtp(String email, String otp) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().trim().equalsIgnoreCase(email.trim()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        if (usuario.getCodigoOtp() == null || !usuario.getCodigoOtp().equals(otp)) {
            throw new RuntimeException("Código OTP inválido");
        }


        if (usuario.getFechaExpiracionOtp() != null && usuario.getFechaExpiracionOtp().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código OTP ha expirado");
        }


        usuario.setCodigoOtp(null);
        usuario.setFechaExpiracionOtp(null);
        return usuarioRepository.save(usuario);
    }


    @Override
    public Usuario cambiarPasswordPrimerIngreso(Long id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));


        usuario.setPassword(nuevaPassword);
        usuario.setDebeCambiarPassword(false);


        return usuarioRepository.save(usuario);
    }


    @Override
    public String solicitarRecuperacionPassword(String email) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().trim().equalsIgnoreCase(email.trim()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese correo"));


        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuario.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);


        emailService.enviarEnlaceRecuperacion(usuario.getEmail(), token);
        return "Enlace de recuperación enviado al correo";
    }


    @Override
    public String restablecerPassword(String token, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> token.equals(u.getTokenRecuperacion()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));


        if (usuario.getFechaExpiracionToken() != null && usuario.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token de recuperación ha expirado");
        }


        usuario.setPassword(nuevaPassword);
        usuario.setTokenRecuperacion(null);
        usuario.setFechaExpiracionToken(null);
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);


        return "Contraseña restablecida exitosamente";
    }

    @Override
    public void eliminarMiCuenta(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        String correoOriginal = usuario.getEmail();

        usuario.setEstado(EstadoCuenta.ELIMINADO);
        usuario.setEmail(correoOriginal + "_eliminado_" + System.currentTimeMillis());
        usuarioRepository.save(usuario);

        // Enviar correo al email original
        emailService.enviarConfirmacionEliminacionCuenta(correoOriginal);
    }

    @Override
    public Usuario actualizarMiPerfil(Long id, Map<String, String> datos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        if (datos.containsKey("razonSocial")) {
            usuario.setRazonSocial(datos.get("razonSocial"));
        }
        if (datos.containsKey("email")) {
            usuario.setEmail(datos.get("email"));
        }
        if (datos.containsKey("telefono")) {
            usuario.setTelefono(datos.get("telefono"));
        }
        if (datos.containsKey("direccion")) {
            usuario.setDireccion(datos.get("direccion"));
        }
        if (datos.containsKey("comuna")) {
            usuario.setComuna(datos.get("comuna"));
        }
        if (datos.containsKey("region")) {
            usuario.setRegion(datos.get("region"));
        }
        if (datos.containsKey("fotoUrl")) {
            usuario.setFotoUrl(datos.get("fotoUrl"));
        }

        return usuarioRepository.save(usuario);
    }
}
