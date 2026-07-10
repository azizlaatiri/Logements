package logements.logements.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BlocageRequest {

    @NotNull
    private LocalDate dateDebut;

    @NotNull
    private LocalDate dateFin;

    private String motif;
}
