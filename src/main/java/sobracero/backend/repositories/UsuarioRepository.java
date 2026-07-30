package sobracero.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sobracero.backend.models.EstadoCuenta;
import sobracero.backend.models.Usuario;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

   List<Usuario> findByEstado(EstadoCuenta estado);
}