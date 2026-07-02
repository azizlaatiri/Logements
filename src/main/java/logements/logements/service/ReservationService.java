package logements.logements.service;

import logements.logements.entity.Logement;
import logements.logements.entity.Reservation;
import logements.logements.entity.StatutReservation;
import logements.logements.entity.Utilisateur;
import logements.logements.exception.ConflitException;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.LogementRepository;
import logements.logements.repository.ReservationRepository;
import logements.logements.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final LogementRepository logementRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ReservationService(ReservationRepository reservationRepository,
                               LogementRepository logementRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.reservationRepository = reservationRepository;
        this.logementRepository = logementRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Reservation> findByUtilisateur(Long utilisateurId) {
        return reservationRepository.findByVoyageurId(utilisateurId);
    }

    public Reservation reserver(Long logementId, Long voyageurId, Reservation demande) {
        if (demande.getDateFin().isBefore(demande.getDateDebut()) || demande.getDateFin().isEqual(demande.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        Logement logement = logementRepository.findById(logementId)
                .orElseThrow(() -> new ResourceNotFoundException("Logement introuvable: " + logementId));
        Utilisateur voyageur = utilisateurRepository.findById(voyageurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + voyageurId));

        if (logement.getProprietaire() != null && logement.getProprietaire().getId().equals(voyageurId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez pas réserver votre propre logement");
        }

        boolean chevauchement = logement.getReservations().stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .anyMatch(r -> !demande.getDateFin().isBefore(r.getDateDebut()) && !demande.getDateDebut().isAfter(r.getDateFin()));
        if (chevauchement) {
            throw new ConflitException("Le logement n'est pas disponible sur cette période");
        }

        long nuits = ChronoUnit.DAYS.between(demande.getDateDebut(), demande.getDateFin());
        BigDecimal prixTotal = logement.getPrixParNuit().multiply(BigDecimal.valueOf(nuits));

        Reservation reservation = new Reservation();
        reservation.setLogement(logement);
        reservation.setVoyageur(voyageur);
        reservation.setDateDebut(demande.getDateDebut());
        reservation.setDateFin(demande.getDateFin());
        reservation.setPrixTotal(prixTotal);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        return reservationRepository.save(reservation);
    }

    public Reservation annuler(Long reservationId, String emailUtilisateur) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable: " + reservationId));
        if (!reservation.getVoyageur().getEmail().equals(emailUtilisateur)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez annuler que vos propres réservations");
        }
        reservation.setStatut(StatutReservation.ANNULEE);
        return reservationRepository.save(reservation);
    }
}
