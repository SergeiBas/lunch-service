-- ===== Автентифікація =====
CREATE TABLE IF NOT EXISTS roles (
                                     id   INTEGER PRIMARY KEY AUTOINCREMENT,
                                     name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
                                     id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                                     email               TEXT NOT NULL UNIQUE,
                                     password_hash       TEXT NOT NULL,
                                     full_name           TEXT NOT NULL,
                                     phone               TEXT,
                                     address             TEXT,
                                     dietary_preferences TEXT,
                                     bonus_balance       INTEGER NOT NULL DEFAULT 0 CHECK (bonus_balance >= 0),
    role_id             INTEGER NOT NULL REFERENCES roles(id),
    created_at          TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- ===== Меню =====
CREATE TABLE IF NOT EXISTS categories (
                                          id   INTEGER PRIMARY KEY AUTOINCREMENT,
                                          name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS allergens (
                                         id   INTEGER PRIMARY KEY AUTOINCREMENT,
                                         name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS dishes (
                                      id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                      name        TEXT NOT NULL,
                                      description TEXT,
                                      composition TEXT,
                                      price       REAL NOT NULL CHECK (price >= 0),
    category_id INTEGER NOT NULL REFERENCES categories(id),
    diet_labels TEXT,
    available   INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0, 1)),
    archived    INTEGER NOT NULL DEFAULT 0 CHECK (archived IN (0, 1)),
    image_url   TEXT
    );

CREATE TABLE IF NOT EXISTS dish_allergens (
                                              dish_id     INTEGER NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    allergen_id INTEGER NOT NULL REFERENCES allergens(id),
    PRIMARY KEY (dish_id, allergen_id)
    );

CREATE TABLE IF NOT EXISTS user_allergens (
                                              user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    allergen_id INTEGER NOT NULL REFERENCES allergens(id),
    PRIMARY KEY (user_id, allergen_id)
    );

CREATE TABLE IF NOT EXISTS favorite_dishes (
                                               user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dish_id INTEGER NOT NULL REFERENCES dishes(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, dish_id)
    );

-- ===== Замовлення =====
CREATE TABLE IF NOT EXISTS group_orders (
                                            id           INTEGER PRIMARY KEY AUTOINCREMENT,
                                            collector_id INTEGER NOT NULL REFERENCES users(id),
    title        TEXT NOT NULL,
    room_code    TEXT NOT NULL UNIQUE,
    status       TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    created_at   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS orders (
                                      id               INTEGER PRIMARY KEY AUTOINCREMENT,
                                      user_id          INTEGER REFERENCES users(id),
    guest_name       TEXT,
    guest_phone      TEXT,
    guest_email      TEXT,
    group_order_id   INTEGER REFERENCES group_orders(id),
    delivery_address TEXT NOT NULL,
    status           TEXT NOT NULL DEFAULT 'NEW' CHECK (status IN
('COLLECTING', 'NEW', 'ACCEPTED', 'PREPARING', 'ON_THE_WAY',
                                                        'ARRIVED', 'COMPLETED', 'REJECTED')),
    subtotal         REAL NOT NULL DEFAULT 0,
    discount         REAL NOT NULL DEFAULT 0,
    bonus_used       INTEGER NOT NULL DEFAULT 0,
    total            REAL NOT NULL DEFAULT 0,
    comment          TEXT,
    created_at       TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS order_items (
                                           id                    INTEGER PRIMARY KEY AUTOINCREMENT,
                                           order_id              INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    dish_id               INTEGER NOT NULL REFERENCES dishes(id),
    quantity              INTEGER NOT NULL CHECK (quantity > 0),
    unit_price            REAL NOT NULL,
    participant_name      TEXT,
    participant_allergens TEXT,
    participant_wishes    TEXT
    );

-- ===== Оплата та логістика =====
CREATE TABLE IF NOT EXISTS payments (
                                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                                        order_id        INTEGER NOT NULL UNIQUE REFERENCES orders(id),
    method          TEXT NOT NULL CHECK (method IN ('CARD', 'CASH')),
    status          TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'FAILED')),
    amount          REAL NOT NULL,
    transaction_ref TEXT,
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS deliveries (
                                          id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                          order_id    INTEGER NOT NULL UNIQUE REFERENCES orders(id),
    courier_id  INTEGER NOT NULL REFERENCES users(id),
    route_order INTEGER,
    assigned_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TEXT
    );

-- ===== Бонуси та сповіщення =====
CREATE TABLE IF NOT EXISTS bonus_transactions (
                                                  id         INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  user_id    INTEGER NOT NULL REFERENCES users(id),
    order_id   INTEGER REFERENCES orders(id),
    amount     INTEGER NOT NULL,
    reason     TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS notifications (
                                             id         INTEGER PRIMARY KEY AUTOINCREMENT,
                                             user_id    INTEGER REFERENCES users(id),
    order_id   INTEGER REFERENCES orders(id),
    channel    TEXT NOT NULL CHECK (channel IN ('EMAIL', 'SCREEN')),
    message    TEXT NOT NULL,
    is_read    INTEGER NOT NULL DEFAULT 0 CHECK (is_read IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_dishes_name ON dishes(name);