package logements.logements.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
    private String titre;

    @Column(length = 2000)
    private String description;

    @NotBlank
    private String ville;

    private String adresse;

    @NotBlank
    private String pays;

    @Positive
    private BigDecimal prixParNuit;

    private Integer nombreChambres;

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
}
