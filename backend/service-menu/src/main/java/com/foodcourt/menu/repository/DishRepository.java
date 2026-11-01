package com.foodcourt.menu.repository;

import com.foodcourt.menu.entity.Categorie;
import com.foodcourt.menu.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByDisponibleTrue();

    List<Dish> findByCategorie(Categorie categorie);

    List<Dish> findByCategorieAndDisponible(Categorie categorie, boolean disponible);

    List<Dish> findByDisponible(boolean disponible);

    @Query("SELECT d FROM Dish d WHERE LOWER(d.nom) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Dish> searchByNom(@Param("search") String search);

    @Query("SELECT d FROM Dish d WHERE " +
            "(:categorie IS NULL OR d.categorie = :categorie) AND " +
            "(:disponible IS NULL OR d.disponible = :disponible) AND " +
            "(:search IS NULL OR LOWER(d.nom) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Dish> findWithFilters(
            @Param("categorie") Categorie categorie,
            @Param("disponible") Boolean disponible,
            @Param("search") String search);
}
