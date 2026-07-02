package logements.logements.controller;

import logements.logements.entity.Utilisateur;
import logements.logements.service.UtilisateurService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/moi")
    public Utilisateur profil(Authentication authentication) {
        return utilisateurService.findByEmail(authentication.getName());
    }

    @GetMapping("/{id}")
    public Utilisateur obtenir(@PathVariable Long id) {
        return utilisateurService.findById(id);
    }
}
