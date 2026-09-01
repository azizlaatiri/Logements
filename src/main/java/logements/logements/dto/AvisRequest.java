package logements.logements.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AvisRequest {

    @Min(1)
    @Max(5)
    private int note;

    @Size(max = 1000)
    private String commentaire;
}
