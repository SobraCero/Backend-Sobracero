package sobracero.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sobracero.backend.models.HistorialEstado;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    void deleteByDonacionId(Long donacionId);
    java.util.List<HistorialEstado> findAllByOrderByFechaCambioDesc();
}
