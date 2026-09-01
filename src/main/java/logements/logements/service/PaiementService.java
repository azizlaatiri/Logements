package logements.logements.service;

import com.stripe.Stripe;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import logements.logements.entity.Reservation;
import logements.logements.entity.StatutPaiement;
import logements.logements.entity.StatutReservation;
import logements.logements.exception.ConflitException;
import logements.logements.exception.ResourceNotFoundException;
import logements.logements.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaiementService {

    private final ReservationRepository reservationRepository;
    private final String cleSecrete;
    private final String cleWebhook;
    private final String devise;
    private final String urlFrontend;

    public PaiementService(ReservationRepository reservationRepository,
                            @Value("${app.stripe.secret-key}") String cleSecrete,
                            @Value("${app.stripe.webhook-secret}") String cleWebhook,
                            @Value("${app.stripe.currency}") String devise,
                            @Value("${app.frontend.url}") String urlFrontend) {
        this.reservationRepository = reservationRepository;
        this.cleSecrete = cleSecrete;
        this.cleWebhook = cleWebhook;
        this.devise = devise;
        this.urlFrontend = urlFrontend;
    }

    @PostConstruct
    public void initialiserStripe() {
        Stripe.apiKey = cleSecrete;
    }

    public String creerSessionPaiement(Long reservationId, String emailVoyageur) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable: " + reservationId));

        if (!reservation.getVoyageur().getEmail().equals(emailVoyageur)) {
            throw new AccessDeniedException("Vous ne pouvez payer que vos propres réservations");
        }
        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ConflitException("La réservation doit être confirmée par l'hôte avant paiement");
        }
        if (reservation.getStatutPaiement() == StatutPaiement.PAYE) {
            throw new ConflitException("Cette réservation est déjà payée");
        }

        long montantCentimes = reservation.getPrixTotal()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(urlFrontend + "/tableau-de-bord?paiement=succes")
                .setCancelUrl(urlFrontend + "/tableau-de-bord?paiement=annule")
                .putMetadata("reservationId", reservationId.toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(devise)
                                                .setUnitAmount(montantCentimes)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Réservation - " + reservation.getLogement().getTitre())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            Session session = Session.create(params);
            reservation.setStripeSessionId(session.getId());
            reservationRepository.save(reservation);
            return session.getUrl();
        } catch (StripeException e) {
            throw new ConflitException("Impossible de créer la session de paiement: " + e.getMessage());
        }
    }

    public void traiterWebhook(String payload, String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, cleWebhook);
        } catch (SignatureVerificationException e) {
            throw new AccessDeniedException("Signature de webhook Stripe invalide");
        }

        if (!"checkout.session.completed".equals(event.getType())) {
            return;
        }

        event.getDataObjectDeserializer().getObject().ifPresent(objet -> {
            if (!(objet instanceof Session session)) {
                return;
            }
            String reservationId = session.getMetadata().get("reservationId");
            if (reservationId == null) {
                return;
            }
            reservationRepository.findById(Long.valueOf(reservationId)).ifPresent(reservation -> {
                reservation.setStatutPaiement(StatutPaiement.PAYE);
                reservation.setStripePaymentIntentId(session.getPaymentIntent());
                reservationRepository.save(reservation);
            });
        });
    }

    /**
     * Rembourse integralement une reservation deja payee aupres de Stripe.
     * Ne persiste pas la reservation : c'est a l'appelant (ReservationService)
     * de sauvegarder l'entite avec le nouveau statut de reservation.
     */
    public void rembourser(Reservation reservation) {
        if (reservation.getStatutPaiement() != StatutPaiement.PAYE) {
            return;
        }

        try {
            String paymentIntentId = reservation.getStripePaymentIntentId();
            if (paymentIntentId == null) {
                // Reservation payee avant l'ajout de la capture du payment_intent dans le webhook :
                // on le retrouve a la demande via la session Stripe deja enregistree.
                if (reservation.getStripeSessionId() == null) {
                    throw new ConflitException("Aucun paiement Stripe associe a cette reservation, remboursement impossible");
                }
                Session session = Session.retrieve(reservation.getStripeSessionId());
                paymentIntentId = session.getPaymentIntent();
                if (paymentIntentId == null) {
                    throw new ConflitException("Aucun paiement Stripe associe a cette reservation, remboursement impossible");
                }
                reservation.setStripePaymentIntentId(paymentIntentId);
            }

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            Refund.create(params);
            reservation.setStatutPaiement(StatutPaiement.REMBOURSE);
        } catch (InvalidRequestException e) {
            if ("charge_already_refunded".equals(e.getCode())) {
                // Deja rembourse cote Stripe (ex: nouvelle tentative apres un echec de sauvegarde) : idempotent.
                reservation.setStatutPaiement(StatutPaiement.REMBOURSE);
                return;
            }
            throw new ConflitException("Impossible de rembourser le paiement: " + e.getMessage());
        } catch (StripeException e) {
            throw new ConflitException("Impossible de rembourser le paiement: " + e.getMessage());
        }
    }
}
