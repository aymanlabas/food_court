-- V1__init_commandes_schema.sql
-- Schema initialization for service-commandes

CREATE TABLE IF NOT EXISTS meal_orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    montant_total NUMERIC(10, 2) NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'RECU',
    idempotency_key UUID NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    meal_order_id BIGINT NOT NULL REFERENCES meal_orders(id) ON DELETE CASCADE,
    dish_id BIGINT NOT NULL,
    quantite INTEGER NOT NULL,
    prix_unitaire NUMERIC(10, 2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_meal_orders_user_id ON meal_orders(user_id);
CREATE INDEX IF NOT EXISTS idx_meal_orders_statut ON meal_orders(statut);
CREATE INDEX IF NOT EXISTS idx_meal_orders_idempotency_key ON meal_orders(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_order_items_meal_order_id ON order_items(meal_order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_dish_id ON order_items(dish_id);

