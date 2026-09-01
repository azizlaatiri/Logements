package logements.logements.service;

import logements.logements.entity.Avis;
import logements.logements.entity.Logement;
import logements.logements.entity.StatutReservation;
import logements.logements.entity.Utilisateur;
import logements.logements.exception.ConflitException;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.AvisRepository;
import logements.logements.repository.LogementRepository;
import logements.logements.repository.ReservationRepository;
import logements.logements.repository.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvisService {

    private final AvisRepository avisRepository;
    private final LogementRepository logementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ReservationRepository reservationRepository;

    public AvisService(AvisRepository avisRepository,
                        LogementRepository logementRepository,
                        UtilisateurRepository utilisateurRepository,
                        ReservationRepository reservationRepository) {
        this.avisRepository = avisRepository;
        this.logementRepository = logementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Avis> findByLogement(Long logementId) {
        return avisRepository.findByLogementIdOrderByDateCreationDesc(logementId);
    }

    public Avis creer(Long logementId, String emailVoyageur, int note, String commentaire) {
        Logement logement = logementRepository.findById(logementId)
                .orElseThrow(() -> new ResourceNotFoundException("Logement introuvable: " + logementId));
        Utilisateur voyageur = utilisateurRepository.findByEmail(emailVoyageur)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + emailVoyageur));

        boolean aReservationConfirmee = reservationRepository.existsByVoyageurIdAndLogementIdAndStatut(
                voyageur.getId(), logementId, StatutReservation.CONFIRMEE);
        if (!aReservationConfirmee) {
            throw new AccessDeniedException(
                    "Vous devez avoir une réservation confirmée pour ce logement afin de le noter");
        }

        if (avisRepository.existsByLogementIdAndVoyageurId(logementId, voyageur.getId())) {
            throw new ConflitException("Vous avez déjà laissé un avis pour ce logement");
        }

        Avis avis = new Avis();
        avis.setLogement(logement);
        avis.setVoyageur(voyageur);
        avis.setNote(note);
        avis.setCommentaire(commentaire);
        return avisRepository.save(avis);
    }
}
