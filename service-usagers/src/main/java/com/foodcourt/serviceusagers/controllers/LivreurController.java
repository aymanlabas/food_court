package com.foodcourt.serviceusagers.controllers;

import com.foodcourt.serviceusagers.dtos.DisponibiliteRequest;
import com.foodcourt.serviceusagers.dtos.LivreurRequest;
import com.foodcourt.serviceusagers.dtos.LivreurResponse;
import com.foodcourt.serviceusagers.services.LivreurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usagers/livreurs")
@RequiredArgsConstructor
public class LivreurController {

    private final LivreurService livreurService;

    @GetMapping
    public List<LivreurResponse> lister(@RequestParam(required = false) Boolean actif) {
        return livreurService.lister(actif);
    }

    @GetMapping("/disponibles")
    public List<LivreurResponse> listerDisponibles() {
        return livreurService.listerDisponibles();
    }

    @GetMapping("/{id}")
    public LivreurResponse trouver(@PathVariable Long id) {
        return livreurService.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivreurResponse creer(@Valid @RequestBody LivreurRequest request) {
        return livreurService.creer(request);
    }

    @PutMapping("/{id}")
    public LivreurResponse modifier(@PathVariable Long id, @Valid @RequestBody LivreurRequest request) {
        return livreurService.modifier(id, request);
    }

    @PatchMapping("/{id}/disponibilite")
    public LivreurResponse changerDisponibilite(@PathVariable Long id,
                                                 @Valid @RequestBody DisponibiliteRequest request) {
        return livreurService.changerDisponibilite(id, request.disponible());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactiver(@PathVariable Long id) {
        livreurService.desactiver(id);
    }
}
