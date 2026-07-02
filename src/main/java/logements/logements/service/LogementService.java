package logements.logements.service;

import logements.logements.entity.Logement;
import logements.logements.entity.StatutReservation;
import logements.logements.entity.Utilisateur;
import logements.logements.exception.ConflitException;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.LogementRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LogementService {

    private final LogementRepository logementRepository;

    public LogementService(LogementRepository logementRepository) {
        this.logementRepository = logementRepository;
    }

    public List<Logement> findAll() {
        return logementRepository.findAll();
    }

    public Logement findById(Long id) {
        return logementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logement introuvable: " + id));
    }

    public Logement creer(Logement logement, Utilisateur proprietaire) {
        logement.setProprietaire(proprietaire);
        return logementRepository.save(logement);
    }

    public Logement modifier(Long id, Logement donnees, String emailUtilisateur) {
        Logement logement = findById(id);
        verifierProprietaire(logement, emailUtilisateur);

        logement.setTitre(donnees.getTitre());
        logement.setDescription(donnees.getDescription());
        logement.setVille(donnees.getVille());
        logement.setAdresse(donnees.getAdresse());
        logement.setPays(donnees.getPays());
        logement.setPrixParNuit(donnees.getPrixParNuit());
        logement.setNombreChambres(donnees.getNombreChambres());
        logement.setNombreVoyageursMax(donnees.getNombreVoyageursMax());
        logement.setImageUrl(donnees.getImageUrl());
        logement.getPhotos().clear();
        logement.getPhotos().addAll(donnees.getPhotos());

        return logementRepository.save(logement);
    }

    public void supprimer(Long id, String emailUtilisateur) {
        Logement logement = findById(id);
        verifierProprietaire(logement, emailUtilisateur);

        boolean reservationsActives = logement.getReservations().stream()
                .anyMatch(r -> r.getStatut() != StatutReservation.ANNULEE && !r.getDateFin().isBefore(LocalDate.now()));
        if (reservationsActives) {
            throw new ConflitException("Impossible de supprimer ce logement : il a des réservations en cours ou à venir");
        }

        logementRepository.delete(logement);
    }

    private void verifierProprietaire(Logement logement, String emailUtilisateur) {
        if (logement.getProprietaire() == null || !logement.getProprietaire().getEmail().equals(emailUtilisateur)) {
            throw new AccessDeniedException("Vous ne pouvez modifier que vos propres logements");
        }
    }

    public List<Logement> rechercher(String ville, String pays, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut != null && dateFin != null) {
            if (dateFin.isBefore(dateDebut)) {
                throw new IllegalArgumentException("La date de fin doit être après la date de début");
            }
            return logementRepository.rechercherDisponibles(ville, pays, dateDebut, dateFin);
        }
        if ((ville != null && !ville.isBlank()) || (pays != null && !pays.isBlank())) {
            return logementRepository.rechercherParVilleEtPays(ville, pays);
        }
        return logementRepository.findAll();
    }
}
