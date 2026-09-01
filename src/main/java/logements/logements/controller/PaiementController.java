package logements.logements.controller;

import logements.logements.service.PaiementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @PostMapping("/reservations/{id}/paiement")
    public Map<String, String> creerPaiement(@PathVariable Long id, Authentication authentication) {
        String url = paiementService.creerSessionPaiement(id, authentication.getName());
        return Map.of("url", url);
    }

    @PostMapping("/paiements/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                         @RequestHeader("Stripe-Signature") String signature) {
        paiementService.traiterWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
