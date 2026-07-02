package logements.logements.controller;

import jakarta.validation.Valid;
import logements.logements.entity.Logement;
import logements.logements.entity.Utilisateur;
import logements.logements.service.LogementService;
import logements.logements.service.UtilisateurService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logements")
public class LogementController {

    private final LogementService logementService;
    private final UtilisateurService utilisateurService;

    public LogementController(LogementService logementService, UtilisateurService utilisateurService) {
        this.logementService = logementService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public List<Logement> lister() {
        return logementService.findAll();
    }

    @GetMapping("/{id}")
    public Logement obtenir(@PathVariable Long id) {
        return logementService.findById(id);
    }

    @GetMapping("/recherche")
    public List<Logement> rechercher(@RequestParam(required = false) String ville,
                                      @RequestParam(required = false) String pays,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return logementService.rechercher(ville, pays, dateDebut, dateFin);
    }

    @PostMapping
    public ResponseEntity<Logement> creer(@Valid @RequestBody Logement logement, Authentication authentication) {
        Utilisateur proprietaire = utilisateurService.findByEmail(authentication.getName());
        Logement cree = logementService.creer(logement, proprietaire);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }
}
