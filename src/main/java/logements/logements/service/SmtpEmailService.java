package logements.logements.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final String urlFrontend;
    private final String expediteur;

    public SmtpEmailService(JavaMailSender mailSender,
                             @Value("${app.frontend.url}") String urlFrontend,
                             @Value("${spring.mail.username}") String expediteur) {
        this.mailSender = mailSender;
        this.urlFrontend = urlFrontend;
        this.expediteur = expediteur;
    }

    @Override
    public void envoyerEmailVerification(String destinataire, String prenom, String token) {
        String lien = urlFrontend + "/verifier-email?token=" + token;
        String contenuHtml = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2>Bonjour %s,</h2>
                    <p>Merci de vérifier votre adresse email en cliquant sur le bouton ci-dessous :</p>
                    <p style="text-align: center; margin: 32px 0;">
                        <a href="%s" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;">
                            Vérifier mon email
                        </a>
                    </p>
                    <p>Ou copiez ce lien dans votre navigateur : <br><a href="%s">%s</a></p>
                    <p style="color:#666;font-size:13px;">Ce lien expire dans 24 heures.</p>
                </div>
                """.formatted(prenom, lien, lien, lien);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(expediteur);
            helper.setTo(destinataire);
            helper.setSubject("Vérifiez votre adresse email - Logements");
            helper.setText(contenuHtml, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Échec de l'envoi de l'email de vérification à {}", destinataire, e);
            throw new IllegalStateException("Impossible d'envoyer l'email de vérification", e);
        }
    }
}
