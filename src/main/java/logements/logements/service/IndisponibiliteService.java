package logements.logements.service;

import logements.logements.dto.BlocageRecurrentRequest;
import logements.logements.dto.BlocageRequest;
import logements.logements.entity.Indisponibilite;
import logements.logements.entity.Logement;
import logements.logements.entity.StatutReservation;
import logements.logements.exception.ConflitException;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.IndisponibiliteRepository;
import logements.logements.repository.LogementRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class IndisponibiliteService {

    private final IndisponibiliteRepository indisponibiliteRepository;
    private final LogementRepository logementRepository;

    public IndisponibiliteService(IndisponibiliteRepository indisponibiliteRepository, LogementRepository logementRepository) {
        this.indisponibiliteRepository = indisponibiliteRepository;
        this.logementRepository = logementRepository;
    }

    public List<Indisponibilite> lister(Long logementId, String emailProprietaire) {
        trouverEtVerifierProprietaire(logementId, emailProprietaire);
        return indisponibiliteRepository.findByLogementId(logementId);
    }

    public Indisponibilite bloquer(Long logementId, BlocageRequest requete, String emailProprietaire) {
        if (requete.getDateFin().isBefore(requete.getDateDebut()) || requete.getDateFin().isEqual(requete.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        Logement logement = trouverEtVerifierProprietaire(logementId, emailProprietaire);
        verifierAucunChevauchementAvecReservations(logement, requete.getDateDebut(), requete.getDateFin());

        Indisponibilite indisponibilite = new Indisponibilite();
        indisponibilite.setLogement(logement);
        indisponibilite.setDateDebut(requete.getDateDebut());
        indisponibilite.setDateFin(requete.getDateFin());
        indisponibilite.setMotif(requete.getMotif());
        return indisponibiliteRepository.save(indisponibilite);
    }

    public List<Indisponibilite> bloquerRecurrent(Long logementId, BlocageRecurrentRequest requete, String emailProprietaire) {
        Logement logement = trouverEtVerifierProprietaire(logementId, emailProprietaire);

        List<LocalDate[]> occurrences = RecurrenceUtils.genererOccurrences(
                requete.getDateDebut(), requete.getDateFin(), requete.getFrequence(), requete.getNombreOccurrences());
        RecurrenceUtils.verifierAucunChevauchementInterne(occurrences);
        for (LocalDate[] occurrence : occurrences) {
            verifierAucunChevauchementAvecReservations(logement, occurrence[0], occurrence[1]);
        }

        String groupeRecurrenceId = UUID.randomUUID().toString();
        return occurrences.stream()
                .map(occurrence -> {
                    Indisponibilite indisponibilite = new Indisponibilite();
                    indisponibilite.setLogement(logement);
                    indisponibilite.setDateDebut(occurrence[0]);
                    indisponibilite.setDateFin(occurrence[1]);
                    indisponibilite.setMotif(requete.getMotif());
                    indisponibilite.setGroupeRecurrenceId(groupeRecurrenceId);
                    return indisponibiliteRepository.save(indisponibilite);
                })
                .toList();
    }

    public void debloquer(Long logementId, Long indisponibiliteId, String emailProprietaire) {
        trouverEtVerifierProprietaire(logementId, emailProprietaire);
        Indisponibilite indisponibilite = indisponibiliteRepository.findById(indisponibiliteId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocage introuvable: " + indisponibiliteId));
        if (!indisponibilite.getLogement().getId().equals(logementId)) {
            throw new AccessDeniedException("Ce blocage n'appartient pas à ce logement");
        }
        indisponibiliteRepository.delete(indisponibilite);
    }

    private Logement trouverEtVerifierProprietaire(Long logementId, String emailProprietaire) {
        Logement logement = logementRepository.findById(logementId)
                .orElseThrow(() -> new ResourceNotFoundException("Logement introuvable: " + logementId));
        if (logement.getProprietaire() == null || !logement.getProprietaire().getEmail().equals(emailProprietaire)) {
            throw new AccessDeniedException("Vous ne pouvez gérer que vos propres logements");
        }
        return logement;
    }

    private void verifierAucunChevauchementAvecReservations(Logement logement, LocalDate dateDebut, LocalDate dateFin) {
        boolean chevauchement = logement.getReservations().stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .anyMatch(r -> RecurrenceUtils.chevauche(dateDebut, dateFin, r.getDateDebut(), r.getDateFin()));
        if (chevauchement) {
            throw new ConflitException(
                    "Impossible de bloquer ces dates : une réservation existe déjà sur cette période (" + dateDebut + " - " + dateFin + ")");
        }
    }
}
