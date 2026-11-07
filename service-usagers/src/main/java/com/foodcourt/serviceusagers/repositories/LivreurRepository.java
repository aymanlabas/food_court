package com.foodcourt.serviceusagers.repositories;

import com.foodcourt.serviceusagers.entities.Livreur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivreurRepository extends JpaRepository<Livreur, Long> {

    List<Livreur> findByActif(boolean actif);

    List<Livreur> findByActifTrueAndDisponibleTrue();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
