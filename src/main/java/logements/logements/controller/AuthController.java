package logements.logements.controller;

import jakarta.validation.Valid;
import logements.logements.dto.AuthResponse;
import logements.logements.dto.GoogleAuthRequest;
import logements.logements.dto.GoogleUtilisateurInfo;
import logements.logements.dto.LoginRequest;
import logements.logements.dto.VerifierTelephoneRequest;
import logements.logements.entity.Utilisateur;
import logements.logements.security.JwtUtils;
import logements.logements.service.GoogleAuthService;
import logements.logements.service.RecaptchaService;
import logements.logements.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurService utilisateurService;
    private final GoogleAuthService googleAuthService;
    private final RecaptchaService recaptchaService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
                           UtilisateurService utilisateurService,
                           GoogleAuthService googleAuthService,
                           RecaptchaService recaptchaService,
                           JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.utilisateurService = utilisateurService;
        this.googleAuthService = googleAuthService;
        this.recaptchaService = recaptchaService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/inscription")
    public ResponseEntity<AuthResponse> inscrire(@Valid @RequestBody Utilisateur utilisateur,
                                                  @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken) {
        recaptchaService.verifier(captchaToken);
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

    @PostMapping("/google")
    public AuthResponse connexionGoogle(@Valid @RequestBody GoogleAuthRequest requete) {
        GoogleUtilisateurInfo infoGoogle = googleAuthService.verifierToken(requete.getIdToken());
        Utilisateur utilisateur = utilisateurService.connecterOuCreerAvecGoogle(infoGoogle);
        String token = jwtUtils.genererToken(utilisateur.getEmail());
        return new AuthResponse(token, utilisateur);
    }

    @GetMapping("/verifier-email")
    public Map<String, String> verifierEmail(@RequestParam String token) {
        Utilisateur utilisateur = utilisateurService.verifierEmail(token);
        return Map.of("message", "Email vérifié avec succès", "email", utilisateur.getEmail());
    }

    @PostMapping("/renvoyer-verification")
    public Map<String, String> renvoyerVerification(Authentication authentication) {
        utilisateurService.renvoyerEmailVerification(authentication.getName());
        return Map.of("message", "Email de vérification renvoyé");
    }

    @PostMapping("/verifier-telephone")
    public Map<String, String> verifierTelephone(@Valid @RequestBody VerifierTelephoneRequest requete, Authentication authentication) {
        utilisateurService.verifierTelephone(authentication.getName(), requete.getCode());
        return Map.of("message", "Téléphone vérifié avec succès");
    }

    @PostMapping("/renvoyer-code-telephone")
    public Map<String, String> renvoyerCodeTelephone(Authentication authentication) {
        utilisateurService.renvoyerCodeTelephone(authentication.getName());
        return Map.of("message", "Code renvoyé");
    }
}
