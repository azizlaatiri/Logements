package logements.logements.service;

import java.time.LocalDate;

public interface EmailService {
    void envoyerEmailVerification(String destinataire, String prenom, String token);

    void envoyerNotificationNouvelleReservation(String destinataire, String prenomHote, String titreLogement,
                                                 LocalDate dateDebut, LocalDate dateFin);
}
