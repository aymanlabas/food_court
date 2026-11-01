-- Schema initialization for service-menu

CREATE TABLE IF NOT EXISTS dishes (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    prix NUMERIC(10, 2) NOT NULL,
    categorie VARCHAR(30) NOT NULL,
    allergenes VARCHAR(500),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ingredients (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    stock NUMERIC(10, 3) NOT NULL,
    unite VARCHAR(20) NOT NULL,
    seuil_alerte NUMERIC(10, 3) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recipe_lines (
    id BIGSERIAL PRIMARY KEY,
    dish_id BIGINT NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id) ON DELETE RESTRICT,
    quantite NUMERIC(10, 3) NOT NULL,
    CONSTRAINT uk_dish_ingredient UNIQUE (dish_id, ingredient_id)
);

CREATE INDEX IF NOT EXISTS idx_dishes_categorie ON dishes(categorie);
CREATE INDEX IF NOT EXISTS idx_dishes_disponible ON dishes(disponible);
CREATE INDEX IF NOT EXISTS idx_recipe_lines_dish_id ON recipe_lines(dish_id);
CREATE INDEX IF NOT EXISTS idx_recipe_lines_ingredient_id ON recipe_lines(ingredient_id);

-- Seed Ingredients
INSERT INTO ingredients (nom, stock, unite, seuil_alerte) VALUES
('Poulet fermier', 25.500, 'kg', 5.000),
('Boeuf haché', 18.000, 'kg', 4.000),
('Saumon frais', 12.000, 'kg', 3.000),
('Riz Basmati', 40.000, 'kg', 8.000),
('Pâtes Tagliatelles', 30.000, 'kg', 5.000),
('Tomates fraîches', 20.000, 'kg', 4.000),
('Salade verte', 15.000, 'kg', 3.000),
('Fromage Parmesan', 6.000, 'kg', 1.500),
('Huile d''olive', 20.000, 'L', 3.000),
('Crème fraîche', 15.000, 'L', 2.500),
('Pommes de terre', 50.000, 'kg', 10.000),
('Chocolat noir 70%', 8.000, 'kg', 2.000),
('Oeufs bio', 120.000, 'unité', 24.000),
('Farine de blé', 35.000, 'kg', 5.000),
('Sucre blanc', 25.000, 'kg', 4.000),
('Café grains', 10.000, 'kg', 2.000),
('Lait entier', 25.000, 'L', 5.000);

-- Seed Dishes
INSERT INTO dishes (nom, description, prix, categorie, allergenes, disponible, image_url) VALUES
('Salade César au Poulet', 'Salade romaine fraîche, filets de poulet grillé, copeaux de parmesan, croûtons dorés et sauce césar maison.', 8.50, 'ENTREE', 'Gluten, Lait, Oeufs', TRUE, 'https://images.unsplash.com/photo-1550304943-4f24f54ddde9?w=600&auto=format&fit=crop'),
('Velouté de Tomates Basilic', 'Soupe onctueuse de tomates mûres, relevée d''huile d''olive et d''un bouquet de basilic frais.', 5.50, 'ENTREE', 'Aucun', TRUE, 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=600&auto=format&fit=crop'),
('Assiette Fraîcheur Végétale', 'Mélange de crudités croquantes, tomates cerises et vinaigrette au citron.', 6.00, 'ENTREE', 'Aucun', TRUE, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop'),
('Pavé de Saumon Rôti et Riz Basmati', 'Saumon d''Atlantique saisi à l''huile d''olive, accompagné de riz parfumé et d''une émulsion citronnée.', 14.50, 'PLAT_PRINCIPAL', 'Poisson', TRUE, 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=600&auto=format&fit=crop'),
('Tagliatelles au Poulet et Crème de Parmesan', 'Pâtes fraîches enrobées d''une sauce onctueuse au parmesan, émincé de poulet doré.', 11.90, 'PLAT_PRINCIPAL', 'Gluten, Lait', TRUE, 'https://images.unsplash.com/photo-1621996346565-e3d5d6281699?w=600&auto=format&fit=crop'),
('Burger Gourmet du Chef', 'Steak de boeuf haché façon bouchère, sauce tomate relevée, salade et frites maison.', 13.50, 'PLAT_PRINCIPAL', 'Gluten, Oeufs', TRUE, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop'),
('Gratin Dauphinois Maison', 'Pommes de terre finement tranchées, mijotées dans la crème fraîche et gratinées.', 4.50, 'ACCOMPAGNEMENT', 'Lait', TRUE, 'https://images.unsplash.com/photo-1584947897554-46487188731d?w=600&auto=format&fit=crop'),
('Frites Rustiques', 'Frites croustillantes coupées au couteau, cuites à point.', 3.50, 'ACCOMPAGNEMENT', 'Aucun', TRUE, 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=600&auto=format&fit=crop'),
('Fondant au Chocolat Coeur Coulant', 'Gâteau au chocolat noir intense avec son cœur chaud et coulant.', 5.00, 'DESSERT', 'Gluten, Lait, Oeufs', TRUE, 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop'),
('Tartelette Citron Meringuée', 'Pâte sablée croustillante, crème citron acidulée et meringue italienne dorée.', 4.80, 'DESSERT', 'Gluten, Lait, Oeufs', TRUE, 'https://images.unsplash.com/photo-1519915028121-7d3463d20b13?w=600&auto=format&fit=crop'),
('Café Espresso Bio', 'Café arabica fraîchement moulu.', 1.80, 'BOISSON', 'Aucun', TRUE, 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=600&auto=format&fit=crop'),
('Eau Minérale Naturelle 50cl', 'Bouteille en verre consignée.', 2.00, 'BOISSON', 'Aucun', TRUE, 'https://images.unsplash.com/photo-1559839914-ba2ae6143c75?w=600&auto=format&fit=crop');

-- Seed Recipes
-- Salade César (id 1): Poulet (0.150kg), Salade (0.100kg), Parmesan (0.030kg)
INSERT INTO recipe_lines (dish_id, ingredient_id, quantite) VALUES
(1, 1, 0.150),
(1, 7, 0.100),
(1, 8, 0.030);

-- Velouté Tomates (id 2): Tomates (0.250kg), Huile d'olive (0.020L)
INSERT INTO recipe_lines (dish_id, ingredient_id, quantite) VALUES
(2, 6, 0.250),
(2, 9, 0.020);

-- Pavé de Saumon (id 4): Saumon (0.200kg), Riz (0.100kg), Huile d'olive (0.015L)
INSERT INTO recipe_lines (dish_id, ingredient_id, quantite) VALUES
(4, 3, 0.200),
(4, 4, 0.100),
(4, 9, 0.015);

-- Tagliatelles Poulet Parmesan (id 5): Pâtes (0.150kg), Poulet (0.120kg), Crème (0.080L), Parmesan (0.025kg)
INSERT INTO recipe_lines (dish_id, ingredient_id, quantite) VALUES
(5, 5, 0.150),
(5, 1, 0.120),
(5, 10, 0.080),
(5, 8, 0.025);

-- Burger Gourmet (id 6): Boeuf (0.180kg), Tomates (0.050kg), Salade (0.030kg), Pommes de terre (0.200kg)
INSERT INTO recipe_lines (dish_id, ingredient_id, quantite) VALUES
(6, 2, 0.180),
(6, 6, 0.050),
(6, 7, 0.030),
(6, 11, 0.200);

-- Fondant Chocolat (id 9): Chocolat (0.070kg), Oeufs (1.000 unité), Farine (0.030kg), Sucre (0.030kg)
INSERT INTO recipe_lines (dish_id, ingredient_id, quantite) VALUES
(9, 12, 0.070),
(9, 13, 1.000),
(9, 14, 0.030),
(9, 15, 0.030);

