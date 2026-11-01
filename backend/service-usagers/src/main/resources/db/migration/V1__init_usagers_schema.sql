-- V1__init_usagers_schema.sql
-- User management and payment tables for service-usagers

CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    nom             VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    solde_cantine   DECIMAL(10, 2)  NOT NULL DEFAULT 100.00,
    regime_alimentaire VARCHAR(100),
    role            VARCHAR(20)     NOT NULL DEFAULT 'EMPLOYEE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

CREATE TABLE payments (
    id              BIGSERIAL       PRIMARY KEY,
    meal_order_id   BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    montant         DECIMAL(10, 2)  NOT NULL,
    moyen           VARCHAR(50)     NOT NULL DEFAULT 'BADGE',
    statut          VARCHAR(20)     NOT NULL DEFAULT 'EN_ATTENTE',
    idempotency_key UUID            NOT NULL UNIQUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_payments_meal_order_id ON payments (meal_order_id);
CREATE INDEX idx_payments_user_id ON payments (user_id);
CREATE INDEX idx_payments_idempotency_key ON payments (idempotency_key);

-- Seed data
-- All passwords are BCrypt hash of 'password123'
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (nom, email, password, solde_cantine, regime_alimentaire, role) VALUES
    ('Admin FoodCourt', 'admin@foodcourt.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 500.00, NULL, 'ADMIN'),
    ('Kitchen Staff', 'kitchen@foodcourt.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0.00, NULL, 'KITCHEN'),
    ('Alice Dupont', 'alice.dupont@foodcourt.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 150.00, 'végétarien', 'EMPLOYEE'),
    ('Bob Martin', 'bob.martin@foodcourt.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 200.00, NULL, 'EMPLOYEE'),
    ('Claire Bernard', 'claire.bernard@foodcourt.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 75.00, 'sans gluten', 'EMPLOYEE'),
    ('David Leroy', 'david.leroy@foodcourt.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 50.00, 'halal', 'EMPLOYEE'),
    ('Emma Petit', 'emma.petit@foodcourt.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 120.00, 'végan', 'EMPLOYEE');
