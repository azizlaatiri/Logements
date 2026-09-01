package logements.logements.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Implémentation de développement : n'envoie aucun email réel, se contente
 * d'afficher le lien de vérification dans les logs du serveur.
 * Active uniquement hors profil "prod" (voir {@link SmtpEmailService}).
 */
@Service
@Profile("!prod")
public class ConsoleEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailService.class);

    private final String urlFrontend;

    public ConsoleEmailService(@Value("${app.frontend.url}") String urlFrontend) {
        this.urlFrontend = urlFrontend;
    }

    @Override
    public void envoyerEmailVerification(String destinataire, String prenom, String token) {
        String lien = urlFrontend + "/verifier-email?token=" + token;
        log.info("""

                ================= EMAIL DE VERIFICATION (mode developpement) =================
                A: {}
                Bonjour {},

                Merci de vérifier votre adresse email en cliquant sur ce lien :
                {}

                Ce lien expire dans 24 heures.
                ================================================================================
                """, destinataire, prenom, lien);
    }

    @Override
    public void envoyerNotificationNouvelleReservation(String destinataire, String prenomHote, String titreLogement,
                                                         LocalDate dateDebut, LocalDate dateFin) {
        log.info("""

                ============= NOTIFICATION NOUVELLE RESERVATION (mode developpement) =============
                A: {}
                Bonjour {},

                Votre logement "{}" vient de recevoir une nouvelle demande de réservation
                du {} au {}. Connectez-vous pour la confirmer ou la refuser.
                ====================================================================================
                """, destinataire, prenomHote, titreLogement, dateDebut, dateFin);
    }
}
