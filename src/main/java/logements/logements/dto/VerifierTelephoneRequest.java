package logements.logements.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifierTelephoneRequest {

    @NotBlank
    private String code;
}
