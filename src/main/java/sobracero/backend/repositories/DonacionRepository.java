package sobracero.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import sobracero.backend.models.Donacion;
import sobracero.backend.models.EstadoDonacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Long> {
    List<Donacion> findByEstado(EstadoDonacion estado);
    List<Donacion> findByDonanteIdOrderByFechaPublicacionDesc(Long donanteId);
    List<Donacion> findAllByOrderByFechaPublicacionDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Donacion d WHERE d.id = :id")
    Optional<Donacion> findByIdForUpdate(@Param("id") Long id);
}