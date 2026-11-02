# 🍽️ Food-Court — Enterprise Cafeteria Platform

**Food-Court** est une application web d'entreprise moderne dédiée à la gestion et à la commande de repas de cantine, basée sur une architecture microservices Spring Boot 3, un API Gateway réactif, une base de données PostgreSQL isolée par service, et une interface utilisateur React 18 / Tailwind CSS / Material UI.

---

## 🚀 Fonctionnalités implémentées

### 1. Gestion du Menu & Inventaire (`service-menu` :8081)
- **Plats** : CRUD complet (nom, description, prix, catégorie, allergènes, image, disponibilité).
- **Ingrédients** : Gestion des stocks et seuils d'alerte, détection automatique du stock critique.
- **Recettes** : Association des ingrédients nécessaires et quantités précises par portion de plat.
- **Calcul de disponibilité** : Algorithme de calcul automatique de la disponibilité des plats selon le stock restant.
- **Réservation atomique & libération** : Endpoints internes prêts pour l'orchestration des commandes (V4/V7).
- **Migration Flyway** : Schéma versionné et jeu de données initial (12 plats, 17 ingrédients, 6 recettes).

### 2. Gestion des Utilisateurs, Authentification & Soldes (`service-usagers` :8083)
- **Authentification JWT** : Inscription sécurisée, connexion, émission et vérification de tokens HS512.
- **Chiffrement BCrypt** : Mots de passe sécurisés dès la migration initiale.
- **Comptes préconfigurés** :
  - Administrateur : `admin@foodcourt.com` / `password123` (Solde: 500 €)
  - Cuisine : `kitchen@foodcourt.com` / `password123`
  - Employé : `alice.dupont@foodcourt.com` / `password123` (Solde: 150 €)
- **Gestion des Badges** : Consultation et rechargement de solde cantine par l'administrateur.
- **Débit & Remboursement idempotents** : Protection stricte contre les doubles débits et doubles remboursements via clé d'idempotence UUID (`idempotencyKey`).

### 3. Service des Commandes & Validation Externe OpenFeign (`service-commandes` :8082) — NOUVEAU V4
- **Cycle de vie complet** : `RECU` → `EN_PREPARATION` → `PRET` → `SERVI`.
- **Intégration synchrone OpenFeign** :
  - Validation de l'existence et du prix serveur auprès de `service-menu` (calcul autoritaire du prix, jamais de confiance dans le client).
  - Vérification du solde badge collaborateur auprès de `service-usagers`.
  - Réservation préalable des stocks d'ingrédients auprès de `service-menu`.
  - Débit atomique du badge cantine de façon idempotente.
  - Compensation automatique : si le paiement échoue, libération immédiate des ingrédients réservés.
- **Annulation sécurisée & Remboursement automatique** :
  - Annulation permise avant que la commande ne soit servie (`RECU`, `EN_PREPARATION`, `PRET` → `ANNULE`).
  - Remboursement automatique et idempotent du badge collaborateur.
  - Interdiction stricte d'annuler une commande déjà servie (`SERVI`).
  - Interdiction des transitions d'états illégitimes.
- **Écran Cuisine (Kanban)** : Endpoint dédié `/api/orders/kitchen` pour les cuisiniers avec transition en un clic (`Démarrer` → `Prêt` → `Servi`).
- **Supervision & Statistiques** : Métriques de commandes et chiffre d'affaires total.

### 4. Service de Notifications & Événements RabbitMQ (`service-notification` :8084) — NOUVEAU V5
- **Messagerie Asynchrone RabbitMQ** :
  - Échangeur Topic : `foodcourt.exchange`.
  - File d'attente durable : `order.notifications.queue`.
  - Clés de routage : `order.ready` (commande prête au comptoir) et `order.cancelled` (commande annulée et remboursée).
- **Consommateur Asynchrone & Idempotence** :
  - [`NotificationRabbitConsumer`](file:///c:/Users/ayn12/OneDrive/Desktop/micross/Food_Court/backend/service-notification/src/main/java/com/foodcourt/notification/consumer/NotificationRabbitConsumer.java) : écoute et désérialisation JSON via `Jackson2JsonMessageConverter`.
  - Contrôle strict d'idempotence via `event_id` (UUID unique) stocké en base `db_notification` pour éliminer tout doublon de message.
- **Centre de Notifications Collaborateur** :
  - Stockage persistant des alertes repas.
  - Endpoints REST `/api/notifications/user/{userId}` pour consultation, filtrage non lus et marquage comme lu.

### 5. Architecture Événementielle Kafka & Saga Chorégraphiée (V7) — NOUVEAU V7
- **Chorégraphie Saga Distribuée** :
  - `meal-order-requested` : initié par `service-commandes` lors du passage de commande.
  - `ingredients-reserved` / `ingredients-rejected` : publié par `service-menu` après vérification atomique des stocks recettes.
  - `meal-payment-requested` : orchestré par `service-commandes` pour déclencher le débit badge.
  - `meal-payment-succeeded` / `meal-payment-failed` : publié par `service-usagers` après transaction locale de débit.
  - `order-placed` : confirmation de mise en préparation de la commande et déstockage définitif.
  - `order-ready` : émis lors du passage de la commande au statut `PRET` en cuisine.
  - `order-cancelled` : déclenche la compensation distribuée (remboursement automatique badge collaborateur et restitution des ingrédients en stock).
- **Pattern Transactional Outbox** :
  - Table `outbox_events` et `processed_events` dans chaque microservice.
  - Les modifications métier et l'événement sont enregistrés dans la **même transaction locale**.
  - Démon d'arrière-plan avec retries exponentielles et garantie de livraison *at-least-once*.
- **Résilience & Dead Letter Topics (DLT)** :
  - Configuration de `DefaultErrorHandler` avec renvoi automatique vers `<topic>.DLT` après épuisement des tentatives.
  - Déduplication et idempotence stricte via `event_id` et clé de corrélation (`correlationId`).
- **Kafka UI** : Interface de gestion et supervision des clusters, topics, offsets et messages disponible sur `http://localhost:8090`.

### 6. API Gateway Réactif (`api-gateway` :8080)
- **Routage dynamique** : Redirection transparente de `/api/dishes/**`, `/api/ingredients/**`, `/api/auth/**`, `/api/users/**`, `/api/payments/**`, `/api/orders/**`, et `/api/notifications/**`.
- **Filtre JWT global** : Validation précoce des requêtes et transmission des headers `X-User-Id`, `X-User-Email`, `X-User-Role` aux microservices avals.
- **CORS unifié** : Autorisation des requêtes du frontend Vite (port 5173).
- **Rate Limiting Redis** : Protection des endpoints avec limitation de débit.

### 7. Frontend SaaS Professionnel (`frontend` :5173)
- **Centre de Notifications en Direct** : Cloche interactive dans la barre de navigation avec compteur de pastilles d'alertes non lues et menu déroulant d'accès direct au suivi de commande.
- **Panier Repas & Commande Badge** : Steppers de quantité, estimation, vérification visuelle de solde, passage de commande en un clic.
- **Suivi de Commande en Temps Réel** : Timeline pas à pas, rafraîchissement automatique, bouton d'annulation avec avertissement de remboursement.
- **Historique Collaborateur** : Liste des commandes passées avec récapitulatif des plats et accès direct au suivi.
- **Écran Kanban Cuisine** : 3 colonnes interactives (Nouvelles Commandes, En Préparation, Prêt au Comptoir) avec mise à jour d'état instantanée.
- **Supervision Administrateur** : Tableau de contrôle des flux de commandes et journal des paiements.

---

## 📁 Architecture des Dossiers

```
Food_Court/
├── .env.example                     # Modèle des variables d'environnement (versionné)
├── .env                             # Variables locales (non versionné, gitignored)
├── docker-compose.yml               # Déploiement multi-conteneurs
├── infrastructure/
│   └── init-databases.sql           # Création des 4 bases de données PostgreSQL
├── docs/
│   └── postman/
│       └── Food-Court.postman_collection.json
├── backend/
│   ├── pom.xml                      # Parent POM multi-modules
│   ├── api-gateway/                 # Spring Cloud Gateway (:8080)
│   ├── service-menu/                # Service Menu & Stocks (:8081)
│   └── service-usagers/             # Service Employés & Paiements (:8083)
└── frontend/                        # Application React / Vite / Tailwind / MUI (:5173)
```

---

## 🔒 Configuration Externalisée

Toutes les configurations sensibles sont externalisées à l'aide de variables d'environnement.
Les fichiers `application.yml` ne contiennent aucun mot de passe en dur, permettant de pousser le code en toute sécurité sur GitHub.

Copiez le fichier d'exemple pour initialiser votre environnement local :
```bash
cp .env.example .env
```

---

## 🐳 Lancement avec Docker Compose

Pour démarrer l'ensemble de la plateforme (Bases de données, Redis, Microservices et Frontend) :

```bash
docker compose up --build
```

Services démarrés :
- **Frontend** : http://localhost:5173
- **API Gateway** : http://localhost:8080
- **Swagger Service Menu** : http://localhost:8081/swagger-ui.html
- **Swagger Service Commandes** : http://localhost:8082/swagger-ui.html
- **Swagger Service Usagers** : http://localhost:8083/swagger-ui.html
- **Swagger Service Notification** : http://localhost:8084/swagger-ui.html
- **RabbitMQ Management** : http://localhost:15672 (guest / guest)
- **Kafka UI** : http://localhost:8090
- **Kafka Broker** : localhost:9092
- **PostgreSQL** : localhost:5432
- **Redis** : localhost:6379

---

## 💻 Lancement en Mode Développement Local

### Prérequis
- Java 21 LTS
- Node.js 20+
- PostgreSQL et Redis en cours d'exécution (via `docker compose up postgres redis -d`)

### 1. Compiler le backend
```bash
mvn clean package -DskipTests -f backend/pom.xml
```

### 2. Démarrer les services backend
```bash
# Service Menu
java -jar backend/service-menu/target/service-menu-1.0.0-SNAPSHOT.jar

# Service Usagers
java -jar backend/service-usagers/target/service-usagers-1.0.0-SNAPSHOT.jar

# API Gateway
java -jar backend/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar
```

### 3. Lancer le frontend React
```bash
cd frontend
npm install
npm run dev
```

Rendez-vous sur `http://localhost:5173`.

---

## 🛣️ Feuille de Route d'Implémentation

- [x] **V1 — CRUD Fonctionnel & Bases de Données**
  - CRUD Menu, Ingrédients, Recettes, Employés, Authentification JWT, Gateway, Dashboard React.
- [x] **V4 — Service Commandes & Validation Externe OpenFeign**
  - Entités `MealOrder` & `OrderItem`, calcul autoritaire des prix, vérification de solde, réservation de stock, débit badge idempotent, compensation d'annulation, écran cuisine Kanban et suivi temps réel.
- [x] **V5 — Notifications Asynchrones avec RabbitMQ**
  - Publication des événements `order.ready` et `order.cancelled`, consommateurs idempotents avec `event_id`, centre de notifications interactif dans le frontend.
- [ ] **V7 — Architecture Événementielle Distribuée avec Apache Kafka**
  - Saga chorégraphiée, Transactional Outbox Pattern, compensation automatique lors des échecs de paiement ou de stock.

