package com.foodcourt.serviceusagers.services;

import com.foodcourt.serviceusagers.dtos.LivreurRequest;
import com.foodcourt.serviceusagers.dtos.LivreurResponse;
import com.foodcourt.serviceusagers.entities.Livreur;
import com.foodcourt.serviceusagers.exceptions.EmailDejaUtiliseException;
import com.foodcourt.serviceusagers.exceptions.RessourceIntrouvableException;
import com.foodcourt.serviceusagers.repositories.LivreurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LivreurService {

    private final LivreurRepository livreurRepository;

    @Transactional(readOnly = true)
    public List<LivreurResponse> lister(Boolean actif) {
        List<Livreur> livreurs = actif == null
                ? livreurRepository.findAll(Sort.by("nom"))
                : livreurRepository.findByActif(actif);
        return livreurs.stream().map(this::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LivreurResponse> listerDisponibles() {
        return livreurRepository.findByActifTrueAndDisponibleTrue().stream()
                .map(this::versReponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LivreurResponse trouver(Long id) {
        return versReponse(chercher(id));
    }

    @Transactional
    public LivreurResponse creer(LivreurRequest request) {
        String email = normaliserEmail(request.email());
        if (livreurRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailDejaUtiliseException(email);
        }
        Livreur livreur = new Livreur();
        appliquer(livreur, request, email);
        livreur.setActif(true);
        livreur.setDisponible(true);
        return versReponse(livreurRepository.save(livreur));
    }

    @Transactional
    public LivreurResponse modifier(Long id, LivreurRequest request) {
        Livreur livreur = chercher(id);
        String email = normaliserEmail(request.email());
        if (livreurRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new EmailDejaUtiliseException(email);
        }
        appliquer(livreur, request, email);
        return versReponse(livreurRepository.save(livreur));
    }

    @Transactional
    public LivreurResponse changerDisponibilite(Long id, boolean disponible) {
        Livreur livreur = chercher(id);
        if (!livreur.isActif()) {
            throw new RessourceIntrouvableException("Livreur inactif : " + id);
        }
        livreur.setDisponible(disponible);
        return versReponse(livreurRepository.save(livreur));
    }

    @Transactional
    public void desactiver(Long id) {
        Livreur livreur = chercher(id);
        livreur.setActif(false);
        livreur.setDisponible(false);
        livreurRepository.save(livreur);
    }

    private Livreur chercher(Long id) {
        return livreurRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Livreur introuvable : " + id));
    }

    private void appliquer(Livreur livreur, LivreurRequest request, String email) {
        livreur.setNom(request.nom().trim());
        livreur.setEmail(email);
        livreur.setTelephone(request.telephone().trim());
        livreur.setVehicule(request.vehicule().trim());
    }

    private String normaliserEmail(String email) {
        return email.trim().toLowerCase();
    }

    private LivreurResponse versReponse(Livreur livreur) {
        return new LivreurResponse(
                livreur.getId(),
                livreur.getNom(),
                livreur.getEmail(),
                livreur.getTelephone(),
                livreur.getVehicule(),
                livreur.isDisponible(),
                livreur.isActif(),
                livreur.getCreeLe()
        );
    }
}
