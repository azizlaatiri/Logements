package logements.logements.service;

public interface EmailService {
    void envoyerEmailVerification(String destinataire, String prenom, String token);
}
