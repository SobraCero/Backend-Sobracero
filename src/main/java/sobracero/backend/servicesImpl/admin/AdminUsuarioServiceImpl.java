package sobracero.backend.servicesImpl.admin;

import sobracero.backend.services.admin.AdminUsuarioService;

import org.springframework.stereotype.Service;
import sobracero.backend.models.EstadoCuenta;
import sobracero.backend.models.Usuario;
import sobracero.backend.repositories.UsuarioRepository;
import sobracero.backend.services.email.EmailService;

import java.util.List;
import java.util.UUID;

@Service
public class AdminUsuarioServiceImpl implements AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public AdminUsuarioServiceImpl(UsuarioRepository usuarioRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @Override
    public List<Usuario> obtenerUsuariosPendientes() {
        return usuarioRepository.findByEstado(EstadoCuenta.PENDIENTE);
    }

    @Override
    public Usuario aprobarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setEstado(EstadoCuenta.ACTIVO);
        usuario.setDebeCambiarPassword(true);

        String claveTemporal = UUID.randomUUID().toString().substring(0, 8);
        usuario.setPassword(claveTemporal);

        // Envío real de la contraseña provisoria por correo
        emailService.enviarContrasenaProvisoria(usuario.getEmail(), claveTemporal);
        System.out.println("\n📧 Correo enviado a " + usuario.getEmail() + " | Clave: " + claveTemporal);

        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && !u.getRol().name().equals("ADMIN"))
                .filter(u -> u.getEstado() != EstadoCuenta.ELIMINADO)
                .toList();
    }

    @Override
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        String correoOriginal = usuario.getEmail();
        
        usuario.setEstado(EstadoCuenta.ELIMINADO);
        // Anonimizar el correo para liberar el email y permitir un nuevo registro con el mismo
        usuario.setEmail(correoOriginal + "_eliminado_" + System.currentTimeMillis());
        usuarioRepository.save(usuario);
        
        // Enviar correo de notificación de eliminación (Por Admin) al correo original
        emailService.enviarEliminacionCuentaPorAdmin(correoOriginal);
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario datosActualizados) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuarioExistente.setRazonSocial(datosActualizados.getRazonSocial());
        usuarioExistente.setEmail(datosActualizados.getEmail());
        usuarioExistente.setTelefono(datosActualizados.getTelefono());
        usuarioExistente.setDireccion(datosActualizados.getDireccion());

        if (datosActualizados.getPassword() != null && !datosActualizados.getPassword().trim().isEmpty()) {
            usuarioExistente.setPassword(datosActualizados.getPassword());
            usuarioExistente.setDebeCambiarPassword(true);
        }

        return usuarioRepository.save(usuarioExistente);
    }
}
