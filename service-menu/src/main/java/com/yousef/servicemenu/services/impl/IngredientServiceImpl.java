package com.yousef.servicemenu.services.impl;



import com.yousef.servicemenu.dtos.request.IngredientRequest;
import com.yousef.servicemenu.dtos.response.IngredientResponse;
import com.yousef.servicemenu.entities.Ingredient;
import com.yousef.servicemenu.exceptions.ResourceNotFoundException;
import com.yousef.servicemenu.mapper.IngredientMapper;
import com.yousef.servicemenu.repositories.IngredientRepository;
import com.yousef.servicemenu.services.IngredientService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    @Override
    public IngredientResponse createIngredient(
            IngredientRequest request) {

        if (ingredientRepository.existsByNomIgnoreCase(request.getNom())) {
            throw new IllegalArgumentException(
                    "Un ingrédient avec ce nom existe déjà"
            );
        }

        Ingredient ingredient = ingredientMapper.toEntity(request);

        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        return ingredientMapper.toResponse(savedIngredient);
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientResponse getIngredientById(Long id) {

        Ingredient ingredient = findIngredientById(id);

        return ingredientMapper.toResponse(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getAllIngredients() {

        return ingredientRepository.findAll()
                .stream()
                .map(ingredientMapper::toResponse)
                .toList();
    }

    @Override
    public IngredientResponse updateIngredient(
            Long id,
            IngredientRequest request) {

        Ingredient existingIngredient = findIngredientById(id);

        if (ingredientRepository.existsByNomIgnoreCaseAndIdNot(
                request.getNom(), id)) {

            throw new IllegalArgumentException(
                    "Un autre ingrédient possède déjà ce nom"
            );
        }

        ingredientMapper.updateEntity(request, existingIngredient);

        Ingredient updatedIngredient =
                ingredientRepository.save(existingIngredient);

        return ingredientMapper.toResponse(updatedIngredient);
    }

    @Override
    public void deleteIngredient(Long id) {

        Ingredient ingredient = findIngredientById(id);

        ingredientRepository.delete(ingredient);
    }

    private Ingredient findIngredientById(Long id) {

        return ingredientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aucun ingrédient trouvé avec l'identifiant : " + id
                        )
                );
    }
}

