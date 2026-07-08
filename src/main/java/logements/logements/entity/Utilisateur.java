package logements.logements.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    private String nom;

    @NotBlank
    @Size(min = 2, max = 50)
    private String prenom;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 8)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean emailVerifie = false;

    @JsonIgnore
    private String tokenVerification;

    @JsonIgnore
    private LocalDateTime tokenVerificationExpiration;

    @Pattern(regexp = "^\\+?[0-9 .-]{6,20}$", message = "Numéro de téléphone invalide")
    private String telephone;

    private Boolean telephoneVerifie = false;

    @JsonIgnore
    private String codeVerificationTelephone;

    @JsonIgnore
    private LocalDateTime codeVerificationTelephoneExpiration;

    @JsonIgnore
    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL)
    private List<Logement> logements = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "voyageur", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();
}
