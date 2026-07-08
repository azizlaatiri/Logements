package logements.logements.service;

import logements.logements.dto.GoogleUtilisateurInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Vérifie les jetons d'identité (ID token) émis par Google Identity Services
 * en interrogeant l'endpoint public tokeninfo de Google. Adapté à un usage
 * à faible volume ; pour un fort trafic, préférer la bibliothèque officielle
 * google-api-client avec vérification de signature locale (JWKS en cache).
 */
@Service
public class GoogleAuthService {

    private static final String URL_TOKENINFO = "https://oauth2.googleapis.com/tokeninfo";

    private final String clientId;
    private final RestClient restClient = RestClient.create();

    public GoogleAuthService(@Value("${app.google.client-id}") String clientId) {
        this.clientId = clientId;
    }

    public GoogleUtilisateurInfo verifierToken(String idToken) {
        if (clientId == null || clientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La connexion avec Google n'est pas configurée sur ce serveur");
        }

        Map<String, Object> reponse;
        try {
            reponse = restClient.get()
                    .uri(URL_TOKENINFO + "?id_token={idToken}", idToken)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton Google invalide ou expiré");
        }

        if (reponse == null || !clientId.equals(reponse.get("aud"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jeton Google invalide");
        }

        String email = (String) reponse.get("email");
        boolean emailVerifie = Boolean.parseBoolean(String.valueOf(reponse.get("email_verified")));
        String prenom = (String) reponse.getOrDefault("given_name", "Utilisateur");
        String nom = (String) reponse.getOrDefault("family_name", "Google");

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Impossible de récupérer l'email du compte Google");
        }

        return new GoogleUtilisateurInfo(email, prenom, nom, emailVerifie);
    }
}
