package sobracero.backend.services.auth;

import sobracero.backend.models.Usuario;
import java.util.Map;

public interface AuthService {
    Usuario registrarUsuario(Usuario nuevoUsuario);
    Object autenticar(String email, String password);
    Usuario verificarOtp(String email, String otp);
    Usuario cambiarPasswordPrimerIngreso(Long id, String nuevaPassword);
    String solicitarRecuperacionPassword(String email);
    String restablecerPassword(String token, String nuevaPassword);
    void eliminarMiCuenta(Long id);
    Usuario actualizarMiPerfil(Long id, Map<String, String> datos);
}
