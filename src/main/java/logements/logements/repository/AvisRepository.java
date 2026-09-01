package logements.logements.repository;

import logements.logements.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {
    List<Avis> findByLogementIdOrderByDateCreationDesc(Long logementId);
    boolean existsByLogementIdAndVoyageurId(Long logementId, Long voyageurId);
}
