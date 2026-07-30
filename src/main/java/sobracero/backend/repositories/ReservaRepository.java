package sobracero.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sobracero.backend.models.Reserva;
import sobracero.backend.models.EstadoReserva;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Optional<Reserva> findByDonacionIdAndEstado(Long donacionId, EstadoReserva estado);
    Optional<Reserva> findByOngIdAndEstado(Long ongId, EstadoReserva estado);
    List<Reserva> findByOngIdAndEstadoOrderByFechaReservaDesc(Long ongId, EstadoReserva estado);
    Optional<Reserva> findByCodigoRetiro(String codigoRetiro);
    boolean existsByCodigoRetiro(String codigoRetiro);
    void deleteByDonacionId(Long donacionId);
}
