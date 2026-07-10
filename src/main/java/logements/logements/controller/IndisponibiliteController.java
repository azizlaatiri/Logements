package logements.logements.controller;

import jakarta.validation.Valid;
import logements.logements.dto.BlocageRecurrentRequest;
import logements.logements.dto.BlocageRequest;
import logements.logements.entity.Indisponibilite;
import logements.logements.service.IndisponibiliteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logements/{logementId}/indisponibilites")
public class IndisponibiliteController {

    private final IndisponibiliteService indisponibiliteService;

    public IndisponibiliteController(IndisponibiliteService indisponibiliteService) {
        this.indisponibiliteService = indisponibiliteService;
    }

    @GetMapping
    public List<Indisponibilite> lister(@PathVariable Long logementId, Authentication authentication) {
        return indisponibiliteService.lister(logementId, authentication.getName());
    }

    @PostMapping
    public ResponseEntity<Indisponibilite> bloquer(@PathVariable Long logementId,
                                                    @Valid @RequestBody BlocageRequest requete,
                                                    Authentication authentication) {
        Indisponibilite cree = indisponibiliteService.bloquer(logementId, requete, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    @PostMapping("/recurrentes")
    public ResponseEntity<List<Indisponibilite>> bloquerRecurrent(@PathVariable Long logementId,
                                                                   @Valid @RequestBody BlocageRecurrentRequest requete,
                                                                   Authentication authentication) {
        List<Indisponibilite> creees = indisponibiliteService.bloquerRecurrent(logementId, requete, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creees);
    }

    @DeleteMapping("/{indisponibiliteId}")
    public ResponseEntity<Void> debloquer(@PathVariable Long logementId,
                                           @PathVariable Long indisponibiliteId,
                                           Authentication authentication) {
        indisponibiliteService.debloquer(logementId, indisponibiliteId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
