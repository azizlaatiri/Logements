package logements.logements.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Vérifie les jetons reCAPTCHA v3 auprès de l'API siteverify de Google.
 * Si aucune clé secrète n'est configurée (mode développement), la
 * vérification est ignorée pour ne pas bloquer les tests locaux.
 */
@Service
public class RecaptchaService {

    private static final String URL_SITEVERIFY = "https://www.google.com/recaptcha/api/siteverify";
    private static final double SEUIL_SCORE_MINIMUM = 0.5;

    private final String secretKey;
    private final RestClient restClient = RestClient.create();

    public RecaptchaService(@Value("${app.recaptcha.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public void verifier(String token) {
        if (secretKey == null || secretKey.isBlank()) {
            return;
        }

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vérification anti-bot manquante");
        }

        Map<String, Object> reponse;
        try {
            reponse = restClient.post()
                    .uri(URL_SITEVERIFY)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("secret=" + secretKey + "&response=" + token)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Vérification anti-bot indisponible");
        }

        boolean succes = reponse != null && Boolean.TRUE.equals(reponse.get("success"));
        double score = (reponse != null && reponse.get("score") instanceof Number nombre) ? nombre.doubleValue() : 0;

        if (!succes || score < SEUIL_SCORE_MINIMUM) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vérification anti-bot échouée, veuillez réessayer");
        }
    }
}
