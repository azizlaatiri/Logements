package logements.logements.dto;

import logements.logements.entity.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Utilisateur utilisateur;
}
