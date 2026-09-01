package logements.logements.repository;

import logements.logements.entity.Reservation;
import logements.logements.entity.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByVoyageurId(Long voyageurId);
    List<Reservation> findByLogementId(Long logementId);
    boolean existsByVoyageurIdAndLogementIdAndStatut(Long voyageurId, Long logementId, StatutReservation statut);
}
