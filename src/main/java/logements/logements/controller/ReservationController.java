package logements.logements.controller;

import jakarta.validation.Valid;
import logements.logements.entity.Reservation;
import logements.logements.entity.Utilisateur;
import logements.logements.service.ReservationService;
import logements.logements.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;
    private final UtilisateurService utilisateurService;

    public ReservationController(ReservationService reservationService, UtilisateurService utilisateurService) {
        this.reservationService = reservationService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/logements/{logementId}/reservations")
    public ResponseEntity<Reservation> reserver(@PathVariable Long logementId,
                                                 @Valid @RequestBody Reservation demande,
                                                 Authentication authentication) {
        Utilisateur voyageur = utilisateurService.findByEmail(authentication.getName());
        Reservation reservation = reservationService.reserver(logementId, voyageur.getId(), demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @GetMapping("/reservations/moi")
    public List<Reservation> tableauUtilisateur(Authentication authentication) {
        Utilisateur utilisateur = utilisateurService.findByEmail(authentication.getName());
        return reservationService.findByUtilisateur(utilisateur.getId());
    }

    @DeleteMapping("/reservations/{id}")
    public Reservation annuler(@PathVariable Long id, Authentication authentication) {
        return reservationService.annuler(id, authentication.getName());
    }
}
