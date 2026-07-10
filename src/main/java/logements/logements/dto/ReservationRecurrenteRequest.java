package logements.logements.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import logements.logements.entity.FrequenceRecurrence;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservationRecurrenteRequest {

    @NotNull
    private LocalDate dateDebut;

    @NotNull
    private LocalDate dateFin;

    @NotNull
    private FrequenceRecurrence frequence;

    @Min(2)
    @Max(52)
    private int nombreOccurrences;
}
