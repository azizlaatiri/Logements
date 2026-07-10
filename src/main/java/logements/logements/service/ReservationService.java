package logements.logements.service;

import logements.logements.dto.ReservationRecurrenteRequest;
import logements.logements.entity.Logement;
import logements.logements.entity.Reservation;
import logements.logements.entity.StatutReservation;
import logements.logements.entity.Utilisateur;
import logements.logements.exception.ConflitException;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.IndisponibiliteRepository;
import logements.logements.repository.LogementRepository;
import logements.logements.repository.ReservationRepository;
import logements.logements.repository.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final LogementRepository logementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final IndisponibiliteRepository indisponibiliteRepository;

    public ReservationService(ReservationRepository reservationRepository,
                               LogementRepository logementRepository,
                               UtilisateurRepository utilisateurRepository,
                               IndisponibiliteRepository indisponibiliteRepository) {
        this.reservationRepository = reservationRepository;
        this.logementRepository = logementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.indisponibiliteRepository = indisponibiliteRepository;
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

        verifierEligibiliteVoyageur(logement, voyageur);
        verifierDisponibilite(logement, demande.getDateDebut(), demande.getDateFin());

        return creerReservation(logement, voyageur, demande.getDateDebut(), demande.getDateFin(), null);
    }

    public List<Reservation> reserverRecurrent(Long logementId, Long voyageurId, ReservationRecurrenteRequest requete) {
        Logement logement = logementRepository.findById(logementId)
                .orElseThrow(() -> new ResourceNotFoundException("Logement introuvable: " + logementId));
        Utilisateur voyageur = utilisateurRepository.findById(voyageurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + voyageurId));

        verifierEligibiliteVoyageur(logement, voyageur);

        List<LocalDate[]> occurrences = RecurrenceUtils.genererOccurrences(
                requete.getDateDebut(), requete.getDateFin(), requete.getFrequence(), requete.getNombreOccurrences());
        RecurrenceUtils.verifierAucunChevauchementInterne(occurrences);
        for (LocalDate[] occurrence : occurrences) {
            verifierDisponibilite(logement, occurrence[0], occurrence[1]);
        }

        String groupeRecurrenceId = UUID.randomUUID().toString();
        return occurrences.stream()
                .map(occurrence -> creerReservation(logement, voyageur, occurrence[0], occurrence[1], groupeRecurrenceId))
                .toList();
    }

    private void verifierEligibiliteVoyageur(Logement logement, Utilisateur voyageur) {
        if (logement.getProprietaire() != null && logement.getProprietaire().getId().equals(voyageur.getId())) {
            throw new AccessDeniedException("Vous ne pouvez pas réserver votre propre logement");
        }
        if (!voyageur.getEmailVerifie()) {
            throw new AccessDeniedException("Veuillez vérifier votre adresse email avant de réserver");
        }
    }

    private void verifierDisponibilite(Logement logement, LocalDate dateDebut, LocalDate dateFin) {
        boolean chevauchementReservation = logement.getReservations().stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .anyMatch(r -> RecurrenceUtils.chevauche(dateDebut, dateFin, r.getDateDebut(), r.getDateFin()));
        if (chevauchementReservation) {
            throw new ConflitException("Le logement n'est pas disponible sur cette période (" + dateDebut + " - " + dateFin + ")");
        }

        boolean chevauchementBlocage = indisponibiliteRepository.findByLogementId(logement.getId()).stream()
                .anyMatch(i -> RecurrenceUtils.chevauche(dateDebut, dateFin, i.getDateDebut(), i.getDateFin()));
        if (chevauchementBlocage) {
            throw new ConflitException("Le logement n'est pas disponible sur cette période, bloqué par l'hôte (" + dateDebut + " - " + dateFin + ")");
        }
    }

    private Reservation creerReservation(Logement logement, Utilisateur voyageur, LocalDate dateDebut, LocalDate dateFin, String groupeRecurrenceId) {
        long nuits = ChronoUnit.DAYS.between(dateDebut, dateFin);
        BigDecimal prixTotal = logement.getPrixParNuit().multiply(BigDecimal.valueOf(nuits));

        Reservation reservation = new Reservation();
        reservation.setLogement(logement);
        reservation.setVoyageur(voyageur);
        reservation.setDateDebut(dateDebut);
        reservation.setDateFin(dateFin);
        reservation.setPrixTotal(prixTotal);
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservation.setGroupeRecurrenceId(groupeRecurrenceId);

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
