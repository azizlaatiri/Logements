package logements.logements.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendrierDto(List<PeriodeDto> periodesReservees, List<PeriodeDto> periodesBloquees) {
    public record PeriodeDto(LocalDate dateDebut, LocalDate dateFin) {}
}
