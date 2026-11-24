# Système Food-Court (Restauration d'Entreprise)

## 1. Description du Projet
Système distribué de gestion des commandes de repas pour les collaborateurs d'une entreprise via leur badge cantine. L'application permet la prise de commande, le suivi en cuisine en temps réel, la gestion automatisée des stocks d'ingrédients et de la disponibilité des recettes, le débit/remboursement du solde cantine, ainsi que la notification asynchrone par email aux collaborateurs.

---

## 2. Architecture Globale et Ports

### Microservices Spring Boot / Spring Cloud

| Composant | Rôle & Responsabilité | Port par Défaut | Base de Données / Broker |
| :--- | :--- | :--- | :--- |
| **eureka-server** | Service Discovery (Enregistrement et découverte dynamique) | `8761` | — |
| **api-gateway** | Point d'entrée unique, routage dynamique et Load Balancing | `8080` | — |
| **config-server** | Centralisation des configurations applicatives (`config-repo/`) | `8888` | — |
| **service-commandes** | Gestion des tickets repas (`MealOrder`), lignes (`OrderItem`), validation OpenFeign/Resilience4j et **Producer** d'événements | `8081` | PostgreSQL (`:5433`) + RabbitMQ (`AFTER_COMMIT`) |
| **service-menu** | Gestion de la carte (`Dish`), ingrédients et recettes (`RecipeLine`) | `8082` | H2 In-Memory |
| **service-usagers** | Gestion des collaborateurs (`User`), solde cantine et paiements | `8083` | H2 In-Memory |
| **service-notification** | **Consumer** des événements JSON, persistance de l'historique et envoi d'alertes emails | `8084` | H2 In-Memory + RabbitMQ + SMTP Mailpit |

### Infrastructure Docker (Persistance, Broker & SMTP)

| Conteneur Docker | Image | Ports Exposés | Identifiants / Configuration |
| :--- | :--- | :--- | :--- |
| **postgres-foodcourt** | `postgres:16` | `5433` (hôte) -> `5432` | DB: `foodcourt_commandes_db` / User: `postgres` / Pass: `postgres` |
| **rabbitmq-foodcourt** | `rabbitmq:3-management` | `5672` (AMQP) / `15672` (Web UI) | User: `guest` / Pass: `guest` (`http://localhost:15672`) |
| **mailpit-foodcourt** | `axllent/mailpit` | `1025` (SMTP) / `8025` (Web UI) | Interface Web de test : `http://localhost:8025` |

---

## 3. Schéma d'Architecture Actualisé (Jalon V5)

![Architecture Microservices & Couches — Food Court](docs/architecture-v4-v5.jpg)

```text
                        +---------------------------------------+
                        |     Eureka Server (Port: 8761)        |
                        |     Config Server (Port: 8888)        |
                        +---------------------------------------+
                                            ^
                                            | (Découverte & Config)
  [ Client / Postman ]                      v
            |                    +---------------------+
            +--- HTTP :8080 ---> |     API Gateway     | (Port: 8080)
                                 +---------------------+
                                            | (Routage dynamique)
        +-----------------------------------+-----------------------------------+
        | /api/orders/**                    | /api/usagers/**                   | /api/notifications/**
        v                                   v                                   v
+-----------------------+  OpenFeign +   +-----------------------+      +-----------------------+
|  service-commandes    | --- Circuit -> |    service-usagers    |      | service-notification  |
|     (Port: 8081)      |    Breaker     |     (Port: 8083)      |      |     (Port: 8084)      |
+-----------------------+                +-----------------------+      +-----------------------+
     |             |                                  |                      |       ^       |
     | JDBC        | AMQP :5672                       | JDBC                 | JDBC  |       | SMTP
     | :5433       | (AFTER_COMMIT)                   v                      v       |       | :1025
     v             v                           [ H2 : usagers ]       [ H2 : notif ] |       v
+------------+  +---------------------------------------------------+                |  +------------+
| PostgreSQL |  |            RabbitMQ Broker (Port: 5672)           |                |  |  Mailpit   |
| (Docker)   |  | Exchange : foodcourt.orders.exchange (Topic)      | --- JSON ------+  | SMTP :1025 |
|            |  | Queue    : foodcourt.notifications.queue          |  @RabbitListener  | Web  :8025 |
+------------+  +---------------------------------------------------+                   +------------+
