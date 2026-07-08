package logements.logements.service;

public interface SmsService {
    void envoyerCodeVerification(String telephone, String code);
}
