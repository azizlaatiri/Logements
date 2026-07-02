package logements.logements.repository;

import logements.logements.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByVoyageurId(Long voyageurId);
    List<Reservation> findByLogementId(Long logementId);
}
