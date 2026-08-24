package logements.logements.service;

import logements.logements.entity.Logement;
import logements.logements.entity.Reservation;
import logements.logements.entity.Role;
import logements.logements.entity.Utilisateur;
import logements.logements.exception.ConflitException;
import logements.logements.repository.LogementRepository;
import logements.logements.repository.ReservationRepository;
import logements.logements.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prouve que le verrou pessimiste sur Logement empêche le double-booking :
 * deux réservations concurrentes sur les mêmes dates ne peuvent pas réussir
 * toutes les deux.
 */
@SpringBootTest
class ReservationConcurrenceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private LogementRepository logementRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private Logement logement;
    private Utilisateur hote;
    private Utilisateur voyageurA;
    private Utilisateur voyageurB;

    @Test
    void deuxReservationsSimultaneesSurLesMemesDatesUneSeuleReussit() throws Exception {
        hote = creerUtilisateur("hote-concurrence@test.local");
        voyageurA = creerUtilisateur("voyageur-a-concurrence@test.local");
        voyageurB = creerUtilisateur("voyageur-b-concurrence@test.local");

        Logement nouveauLogement = new Logement();
        nouveauLogement.setTitre("Logement test concurrence");
        nouveauLogement.setVille("Test");
        nouveauLogement.setPays("Test");
        nouveauLogement.setPrixParNuit(BigDecimal.valueOf(100));
        nouveauLogement.setProprietaire(hote);
        logement = logementRepository.save(nouveauLogement);

        LocalDate debut = LocalDate.now().plusDays(30);
        LocalDate fin = debut.plusDays(3);

        int nbThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        CountDownLatch depart = new CountDownLatch(1);
        AtomicInteger succes = new AtomicInteger(0);
        AtomicInteger conflits = new AtomicInteger(0);

        Long logementId = logement.getId();
        Long voyageurAId = voyageurA.getId();
        Long voyageurBId = voyageurB.getId();

        Runnable tache1 = creerTache(logementId, voyageurAId, debut, fin, depart, succes, conflits);
        Runnable tache2 = creerTache(logementId, voyageurBId, debut, fin, depart, succes, conflits);

        var future1 = executor.submit(tache1);
        var future2 = executor.submit(tache2);
        depart.countDown();
        executor.shutdown();
        assertEquals(true, executor.awaitTermination(15, TimeUnit.SECONDS), "Les deux tâches doivent se terminer");
        future1.get();
        future2.get();

        assertEquals(1, succes.get(), "Une seule des deux réservations concurrentes doit réussir");
        assertEquals(1, conflits.get(), "L'autre doit échouer avec un conflit de disponibilité");
        assertEquals(1, reservationRepository.findByLogementId(logementId).size(),
                "Une seule réservation ne doit être persistée en base");
    }

    private Runnable creerTache(Long logementId, Long voyageurId, LocalDate debut, LocalDate fin,
                                 CountDownLatch depart, AtomicInteger succes, AtomicInteger conflits) {
        return () -> {
            try {
                depart.await();
                Reservation demande = new Reservation();
                demande.setDateDebut(debut);
                demande.setDateFin(fin);
                reservationService.reserver(logementId, voyageurId, demande);
                succes.incrementAndGet();
            } catch (ConflitException e) {
                conflits.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        };
    }

    @AfterEach
    void nettoyer() {
        if (logement != null && logement.getId() != null) {
            reservationRepository.findByLogementId(logement.getId()).forEach(reservationRepository::delete);
            logementRepository.deleteById(logement.getId());
        }
        if (voyageurA != null) utilisateurRepository.deleteById(voyageurA.getId());
        if (voyageurB != null) utilisateurRepository.deleteById(voyageurB.getId());
        if (hote != null) utilisateurRepository.deleteById(hote.getId());
    }

    private Utilisateur creerUtilisateur(String email) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom("Test");
        utilisateur.setPrenom("Test");
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse("motdepassehashefictif123");
        utilisateur.setRole(Role.VOYAGEUR);
        utilisateur.setEmailVerifie(true);
        return utilisateurRepository.save(utilisateur);
    }
}
