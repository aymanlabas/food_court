package com.foodcourt.serviceusagers.services;

import com.foodcourt.serviceusagers.dtos.RestaurantRequest;
import com.foodcourt.serviceusagers.dtos.RestaurantResponse;
import com.foodcourt.serviceusagers.entities.Restaurant;
import com.foodcourt.serviceusagers.exceptions.EmailDejaUtiliseException;
import com.foodcourt.serviceusagers.exceptions.RessourceIntrouvableException;
import com.foodcourt.serviceusagers.repositories.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<RestaurantResponse> lister(Boolean actif) {
        List<Restaurant> restaurants = actif == null
                ? restaurantRepository.findAll(Sort.by("nom"))
                : restaurantRepository.findByActif(actif);
        return restaurants.stream().map(this::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse trouver(Long id) {
        return versReponse(chercher(id));
    }

    @Transactional
    public RestaurantResponse creer(RestaurantRequest request) {
        String email = normaliserEmail(request.email());
        if (restaurantRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailDejaUtiliseException(email);
        }
        Restaurant restaurant = new Restaurant();
        appliquer(restaurant, request, email);
        restaurant.setActif(true);
        return versReponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse modifier(Long id, RestaurantRequest request) {
        Restaurant restaurant = chercher(id);
        String email = normaliserEmail(request.email());
        if (restaurantRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new EmailDejaUtiliseException(email);
        }
        appliquer(restaurant, request, email);
        return versReponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void desactiver(Long id) {
        Restaurant restaurant = chercher(id);
        restaurant.setActif(false);
        restaurantRepository.save(restaurant);
    }

    private Restaurant chercher(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Restaurant introuvable : " + id));
    }

    private void appliquer(Restaurant restaurant, RestaurantRequest request, String email) {
        restaurant.setNom(request.nom().trim());
        restaurant.setEmail(email);
        restaurant.setTelephone(request.telephone().trim());
        restaurant.setAdresse(request.adresse().trim());
        restaurant.setHoraires(request.horaires().trim());
    }

    private String normaliserEmail(String email) {
        return email.trim().toLowerCase();
    }

    private RestaurantResponse versReponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getNom(),
                restaurant.getEmail(),
                restaurant.getTelephone(),
                restaurant.getAdresse(),
                restaurant.getHoraires(),
                restaurant.isActif(),
                restaurant.getCreeLe()
        );
    }
}
