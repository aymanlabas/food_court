package com.foodcourt.serviceusagers.services;

import com.foodcourt.serviceusagers.dtos.ClientRequest;
import com.foodcourt.serviceusagers.dtos.ClientResponse;
import com.foodcourt.serviceusagers.entities.Client;
import com.foodcourt.serviceusagers.exceptions.EmailDejaUtiliseException;
import com.foodcourt.serviceusagers.exceptions.RessourceIntrouvableException;
import com.foodcourt.serviceusagers.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientResponse> lister(Boolean actif) {
        List<Client> clients = actif == null
                ? clientRepository.findAll(Sort.by("nom"))
                : clientRepository.findByActif(actif);
        return clients.stream().map(this::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse trouver(Long id) {
        return versReponse(chercher(id));
    }

    @Transactional
    public ClientResponse creer(ClientRequest request) {
        String email = normaliserEmail(request.email());
        if (clientRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailDejaUtiliseException(email);
        }
        Client client = new Client();
        appliquer(client, request, email);
        client.setActif(true);
        return versReponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse modifier(Long id, ClientRequest request) {
        Client client = chercher(id);
        String email = normaliserEmail(request.email());
        if (clientRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new EmailDejaUtiliseException(email);
        }
        appliquer(client, request, email);
        return versReponse(clientRepository.save(client));
    }

    @Transactional
    public void desactiver(Long id) {
        Client client = chercher(id);
        client.setActif(false);
        clientRepository.save(client);
    }

    private Client chercher(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Client introuvable : " + id));
    }

    private void appliquer(Client client, ClientRequest request, String email) {
        client.setNom(request.nom().trim());
        client.setEmail(email);
        client.setTelephone(request.telephone().trim());
        client.setAdresseLivraison(request.adresseLivraison().trim());
    }

    private String normaliserEmail(String email) {
        return email.trim().toLowerCase();
    }

    private ClientResponse versReponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getNom(),
                client.getEmail(),
                client.getTelephone(),
                client.getAdresseLivraison(),
                client.isActif(),
                client.getCreeLe()
        );
    }
}
