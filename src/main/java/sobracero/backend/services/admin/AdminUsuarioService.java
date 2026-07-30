package sobracero.backend.services.admin;

import sobracero.backend.models.Usuario;
import java.util.List;

public interface AdminUsuarioService {
    List<Usuario> obtenerUsuariosPendientes();
    Usuario aprobarUsuario(Long id);
    List<Usuario> obtenerTodosLosUsuarios();
    void eliminarUsuario(Long id);
    Usuario actualizarUsuario(Long id, Usuario datosActualizados);
}
