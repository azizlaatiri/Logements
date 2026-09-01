package logements.logements.controller;

import jakarta.validation.Valid;
import logements.logements.dto.AvisRequest;
import logements.logements.entity.Avis;
import logements.logements.service.AvisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logements/{logementId}/avis")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    @GetMapping
    public List<Avis> lister(@PathVariable Long logementId) {
        return avisService.findByLogement(logementId);
    }

    @PostMapping
    public ResponseEntity<Avis> creer(@PathVariable Long logementId,
                                       @Valid @RequestBody AvisRequest requete,
                                       Authentication authentication) {
        Avis avis = avisService.creer(logementId, authentication.getName(), requete.getNote(), requete.getCommentaire());
        return ResponseEntity.status(HttpStatus.CREATED).body(avis);
    }
}
