package com.foodcourt.serviceusagers.controllers;

import com.foodcourt.serviceusagers.dtos.ClientRequest;
import com.foodcourt.serviceusagers.dtos.ClientResponse;
import com.foodcourt.serviceusagers.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/usagers/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientResponse> lister(@RequestParam(required = false) Boolean actif) {
        return clientService.lister(actif);
    }

    @GetMapping("/{id}")
    public ClientResponse trouver(@PathVariable Long id) {
        return clientService.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse creer(@Valid @RequestBody ClientRequest request) {
        return clientService.creer(request);
    }

    @PutMapping("/{id}")
    public ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactiver(@PathVariable Long id) {
        clientService.desactiver(id);
    }
}
