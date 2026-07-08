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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
                               EmailService emailService, SmsService smsService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.smsService = smsService;
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

        boolean avecTelephone = utilisateur.getTelephone() != null && !utilisateur.getTelephone().isBlank();
        if (avecTelephone) {
            utilisateur.setTelephoneVerifie(false);
            genererCodeTelephone(utilisateur);
        }

        Utilisateur cree = utilisateurRepository.save(utilisateur);
        emailService.envoyerEmailVerification(cree.getEmail(), cree.getPrenom(), cree.getTokenVerification());
        if (avecTelephone) {
            smsService.envoyerCodeVerification(cree.getTelephone(), cree.getCodeVerificationTelephone());
        }
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

    public Utilisateur verifierTelephone(String email, String code) {
        Utilisateur utilisateur = findByEmail(email);

        if (utilisateur.getTelephone() == null) {
            throw new ConflitException("Aucun numéro de téléphone associé à ce compte");
        }
        if (utilisateur.getCodeVerificationTelephone() == null || !utilisateur.getCodeVerificationTelephone().equals(code)) {
            throw new ConflitException("Code de vérification incorrect");
        }
        if (utilisateur.getCodeVerificationTelephoneExpiration() == null
                || utilisateur.getCodeVerificationTelephoneExpiration().isBefore(LocalDateTime.now())) {
            throw new ConflitException("Ce code a expiré, demandez-en un nouveau");
        }

        utilisateur.setTelephoneVerifie(true);
        utilisateur.setCodeVerificationTelephone(null);
        utilisateur.setCodeVerificationTelephoneExpiration(null);
        return utilisateurRepository.save(utilisateur);
    }

    public void renvoyerCodeTelephone(String email) {
        Utilisateur utilisateur = findByEmail(email);
        if (utilisateur.getTelephone() == null) {
            throw new ConflitException("Aucun numéro de téléphone associé à ce compte");
        }
        if (utilisateur.getTelephoneVerifie()) {
            throw new ConflitException("Ce téléphone est déjà vérifié");
        }
        genererCodeTelephone(utilisateur);
        utilisateurRepository.save(utilisateur);
        smsService.envoyerCodeVerification(utilisateur.getTelephone(), utilisateur.getCodeVerificationTelephone());
    }

    private void genererTokenEmail(Utilisateur utilisateur) {
        utilisateur.setTokenVerification(UUID.randomUUID().toString());
        utilisateur.setTokenVerificationExpiration(LocalDateTime.now().plusHours(24));
    }

    private void genererCodeTelephone(Utilisateur utilisateur) {
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        utilisateur.setCodeVerificationTelephone(code);
        utilisateur.setCodeVerificationTelephoneExpiration(LocalDateTime.now().plusMinutes(10));
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
