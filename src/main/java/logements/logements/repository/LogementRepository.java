package logements.logements.repository;

import logements.logements.entity.Logement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LogementRepository extends JpaRepository<Logement, Long> {

    /**
     * Verrouille la ligne du logement (SELECT ... FOR UPDATE) pour la durée de la
     * transaction, afin de sérialiser les vérifications de disponibilité
     * concurrentes (réservation et blocage de dates) et éviter le double-booking.
     * Requête native : la variante JPQL + @Lock(PESSIMISTIC_WRITE) génère
     * "FOR UPDATE OF alias", une syntaxe invalide sous MariaDB.
     */
    @Query(value = "SELECT * FROM logements WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Logement> findByIdForUpdate(@Param("id") Long id);

    List<Logement> findByProprietaireId(Long proprietaireId);

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
            AND l.id NOT IN (
                SELECT i.logement.id FROM Indisponibilite i
                WHERE i.dateDebut <= :dateFin
                AND i.dateFin >= :dateDebut
            )
            """)
    List<Logement> rechercherDisponibles(@Param("ville") String ville,
                                          @Param("pays") String pays,
                                          @Param("dateDebut") LocalDate dateDebut,
                                          @Param("dateFin") LocalDate dateFin);
}
