package logements.logements.service;

import logements.logements.dto.GoogleUtilisateurInfo;
import logements.logements.entity.Role;
import logements.logements.entity.Utilisateur;
import logements.logements.exception.ConflitException;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
                               EmailService emailService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + id));
    }

    public Utilisateur inscrire(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet email");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        utilisateur.setEmailVerifie(false);
        genererTokenEmail(utilisateur);

        Utilisateur cree = utilisateurRepository.save(utilisateur);
        emailService.envoyerEmailVerification(cree.getEmail(), cree.getPrenom(), cree.getTokenVerification());
        return cree;
    }

    public Utilisateur verifierEmail(String token) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenVerification(token)
                .orElseThrow(() -> new ResourceNotFoundException("Lien de vérification invalide"));

        if (utilisateur.getTokenVerificationExpiration() == null
                || utilisateur.getTokenVerificationExpiration().isBefore(LocalDateTime.now())) {
            throw new ConflitException("Ce lien de vérification a expiré, demandez-en un nouveau");
        }

        utilisateur.setEmailVerifie(true);
        utilisateur.setTokenVerification(null);
        utilisateur.setTokenVerificationExpiration(null);
        return utilisateurRepository.save(utilisateur);
    }

    public void renvoyerEmailVerification(String email) {
        Utilisateur utilisateur = findByEmail(email);
        if (utilisateur.getEmailVerifie()) {
            throw new ConflitException("Cet email est déjà vérifié");
        }
        genererTokenEmail(utilisateur);
        utilisateurRepository.save(utilisateur);
        emailService.envoyerEmailVerification(utilisateur.getEmail(), utilisateur.getPrenom(), utilisateur.getTokenVerification());
    }

    private void genererTokenEmail(Utilisateur utilisateur) {
        utilisateur.setTokenVerification(UUID.randomUUID().toString());
        utilisateur.setTokenVerificationExpiration(LocalDateTime.now().plusHours(24));
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + email));
    }

    public Utilisateur connecterOuCreerAvecGoogle(GoogleUtilisateurInfo infoGoogle) {
        return utilisateurRepository.findByEmail(infoGoogle.email())
                .orElseGet(() -> {
                    Utilisateur nouveau = new Utilisateur();
                    nouveau.setEmail(infoGoogle.email());
                    nouveau.setPrenom(infoGoogle.prenom());
                    nouveau.setNom(infoGoogle.nom());
                    nouveau.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
                    nouveau.setRole(Role.VOYAGEUR);
                    nouveau.setEmailVerifie(infoGoogle.emailVerifie());
                    return utilisateurRepository.save(nouveau);
                });
    }
}
