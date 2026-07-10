package logements.logements.repository;

import logements.logements.entity.Indisponibilite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndisponibiliteRepository extends JpaRepository<Indisponibilite, Long> {
    List<Indisponibilite> findByLogementId(Long logementId);
}
