package logements.logements.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "logements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Logement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 5, max = 100)
    private String titre;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @NotBlank
    @Size(min = 2)
    private String ville;

    private String adresse;

    @NotBlank
    private String pays;

    @Positive
    @DecimalMax(value = "100000")
    private BigDecimal prixParNuit;

    @Max(50)
    private Integer nombreChambres;

    @Max(50)
    private Integer nombreVoyageursMax;

    @Column(length = 2048)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "logement_photos", joinColumns = @JoinColumn(name = "logement_id"))
    @Column(name = "url", length = 2048)
    @OrderColumn(name = "position")
    private List<String> photos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "proprietaire_id")
    private Utilisateur proprietaire;

    @JsonIgnore
    @OneToMany(mappedBy = "logement", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "logement", cascade = CascadeType.ALL)
    private List<Indisponibilite> indisponibilites = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "logement", cascade = CascadeType.ALL)
    private List<Avis> avis = new ArrayList<>();

    public Double getNoteMoyenne() {
        // avis peut etre null pour un logement fraichement desserialise depuis une requete
        // (Jackson utilise le constructeur @AllArgsConstructor et ne connait pas ce champ
        // ignore, donc il passe null au lieu de l'initialiseur par defaut).
        if (avis == null || avis.isEmpty()) {
            return null;
        }
        return avis.stream().mapToInt(Avis::getNote).average().orElse(0);
    }

    public int getNombreAvis() {
        return avis == null ? 0 : avis.size();
    }
}
