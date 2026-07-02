package logements.logements.service;

import logements.logements.entity.Logement;
import logements.logements.entity.Utilisateur;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.LogementRepository;
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
