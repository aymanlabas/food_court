package com.foodcourt.serviceusagers;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsagerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void creerListerEtDesactiverUnClient() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/usagers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Amina Benali",
                                  "email": "Amina@Example.com",
                                  "telephone": "0600000001",
                                  "adresseLivraison": "12 rue de Paris"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("amina@example.com"))
                .andExpect(jsonPath("$.actif").value(true))
                .andReturn();
        Number id = JsonPath.read(creation.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/usagers/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].nom").value("Amina Benali"));

        mockMvc.perform(delete("/api/usagers/clients/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usagers/clients/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actif").value(false));
    }

    @Test
    void refuseUnClientInvalideEtUnEmailDejaPris() throws Exception {
        mockMvc.perform(post("/api/usagers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom": "", "email": "pas-un-email", "telephone": "", "adresseLivraison": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.email").exists());

        String client = """
                {
                  "nom": "Karim",
                  "email": "karim@example.com",
                  "telephone": "0600000002",
                  "adresseLivraison": "3 avenue des Lilas"
                }
                """;
        mockMvc.perform(post("/api/usagers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(client))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/usagers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(client))
                .andExpect(status().isConflict());
    }

    @Test
    void creerUnRestaurant() throws Exception {
        mockMvc.perform(post("/api/usagers/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Pizza Roma",
                                  "email": "roma@example.com",
                                  "telephone": "0140000000",
                                  "adresse": "8 place du Marché",
                                  "horaires": "11:00-23:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Pizza Roma"))
                .andExpect(jsonPath("$.horaires").value("11:00-23:00"));

        mockMvc.perform(get("/api/usagers/restaurants/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void livreurDisponiblePuisIndisponible() throws Exception {
        MvcResult creation = mockMvc.perform(post("/api/usagers/livreurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Yassine",
                                  "email": "yassine@example.com",
                                  "telephone": "0700000003",
                                  "vehicule": "Vélo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.disponible").value(true))
                .andReturn();
        Number id = JsonPath.read(creation.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/usagers/livreurs/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].nom").value("Yassine"));

        mockMvc.perform(patch("/api/usagers/livreurs/" + id + "/disponibilite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"disponible": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(get("/api/usagers/livreurs/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
