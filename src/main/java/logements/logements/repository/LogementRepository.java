package logements.logements.repository;

import logements.logements.entity.Logement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LogementRepository extends JpaRepository<Logement, Long> {

    @Query("""
            SELECT l FROM Logement l
            WHERE (:ville IS NULL OR LOWER(l.ville) LIKE LOWER(CONCAT('%', :ville, '%')))
            AND (:pays IS NULL OR LOWER(l.pays) LIKE LOWER(CONCAT('%', :pays, '%')))
            """)
    List<Logement> rechercherParVilleEtPays(@Param("ville") String ville, @Param("pays") String pays);

    @Query("""
            SELECT l FROM Logement l
            WHERE (:ville IS NULL OR LOWER(l.ville) LIKE LOWER(CONCAT('%', :ville, '%')))
            AND (:pays IS NULL OR LOWER(l.pays) LIKE LOWER(CONCAT('%', :pays, '%')))
            AND l.id NOT IN (
                SELECT r.logement.id FROM Reservation r
                WHERE r.statut <> logements.logements.entity.StatutReservation.ANNULEE
                AND r.dateDebut <= :dateFin
                AND r.dateFin >= :dateDebut
            )
            """)
    List<Logement> rechercherDisponibles(@Param("ville") String ville,
                                          @Param("pays") String pays,
                                          @Param("dateDebut") LocalDate dateDebut,
                                          @Param("dateFin") LocalDate dateFin);
}
