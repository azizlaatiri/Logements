package logements.logements.controller;

import jakarta.validation.Valid;
import logements.logements.dto.AuthResponse;
import logements.logements.dto.LoginRequest;
import logements.logements.entity.Utilisateur;
import logements.logements.security.JwtUtils;
import logements.logements.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurService utilisateurService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
                           UtilisateurService utilisateurService,
                           JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.utilisateurService = utilisateurService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/inscription")
    public ResponseEntity<AuthResponse> inscrire(@Valid @RequestBody Utilisateur utilisateur) {
        Utilisateur cree = utilisateurService.inscrire(utilisateur);
        String token = jwtUtils.genererToken(cree.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, cree));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest requete) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requete.getEmail(), requete.getMotDePasse()));

        Utilisateur utilisateur = utilisateurService.findByEmail(requete.getEmail());
        String token = jwtUtils.genererToken(utilisateur.getEmail());
        return new AuthResponse(token, utilisateur);
    }
}
