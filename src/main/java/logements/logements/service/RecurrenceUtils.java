package logements.logements.service;

import logements.logements.entity.FrequenceRecurrence;
import logements.logements.exception.ConflitException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

final class RecurrenceUtils {

    private RecurrenceUtils() {
    }

    static boolean chevauche(LocalDate debut1, LocalDate fin1, LocalDate debut2, LocalDate fin2) {
        return !fin1.isBefore(debut2) && !debut1.isAfter(fin2);
    }

    static List<LocalDate[]> genererOccurrences(LocalDate dateDebut, LocalDate dateFin,
                                                 FrequenceRecurrence frequence, int nombreOccurrences) {
        if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        long dureeJours = ChronoUnit.DAYS.between(dateDebut, dateFin);
        List<LocalDate[]> occurrences = new ArrayList<>();
        for (int i = 0; i < nombreOccurrences; i++) {
            LocalDate debutOccurrence = frequence == FrequenceRecurrence.HEBDOMADAIRE
                    ? dateDebut.plusWeeks(i)
                    : dateDebut.plusMonths(i);
            LocalDate finOccurrence = debutOccurrence.plusDays(dureeJours);
            occurrences.add(new LocalDate[]{debutOccurrence, finOccurrence});
        }
        return occurrences;
    }

    static void verifierAucunChevauchementInterne(List<LocalDate[]> occurrences) {
        for (int i = 0; i < occurrences.size(); i++) {
            for (int j = i + 1; j < occurrences.size(); j++) {
                LocalDate[] a = occurrences.get(i);
                LocalDate[] b = occurrences.get(j);
                if (chevauche(a[0], a[1], b[0], b[1])) {
                    throw new ConflitException(
                            "La récurrence choisie génère des périodes qui se chevauchent (ex: " + a[0] + " - " + a[1]
                                    + " et " + b[0] + " - " + b[1] + "), réduisez la durée ou espacez les occurrences");
                }
            }
        }
    }
}
