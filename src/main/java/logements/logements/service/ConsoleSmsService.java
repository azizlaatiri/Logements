package logements.logements.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implémentation de développement : n'envoie aucun SMS réel, se contente
 * d'afficher le code de vérification dans les logs du serveur. À remplacer
 * par une implémentation Twilio Verify (ou équivalent) en production.
 */
@Service
public class ConsoleSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleSmsService.class);

    @Override
    public void envoyerCodeVerification(String telephone, String code) {
        log.info("""

                ===================== SMS DE VERIFICATION (mode developpement) =====================
                A: {}
                Votre code de vérification Logements : {}
                Ce code expire dans 10 minutes.
                ======================================================================================
                """, telephone, code);
    }
}
