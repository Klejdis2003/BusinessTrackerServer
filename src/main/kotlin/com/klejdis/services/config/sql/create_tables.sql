CREATE TABLE businesses
(
    id          SERIAL PRIMARY KEY,
    owner_email VARCHAR(50) UNIQUE NOT NULL,
    CONSTRAINT businesses_owner_email_check CHECK (owner_email ~* '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$')
);


CREATE TABLE IF NOT EXISTS customers
(
    phone VARCHAR(10) NOT NULL PRIMARY KEY,
    name  VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS item_types
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description TEXT        NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS items
(
    id             SERIAL PRIMARY KEY,
    business_id    INT         NOT NULL,
    name           VARCHAR(50) NOT NULL,
    description    TEXT        NOT NULL DEFAULT '',
    purchase_price DECIMAL(8)  NOT NULL,
    price          DECIMAL(8)  NOT NULL,
    item_type_id   INT         NOT NULL,
    CONSTRAINT items_price_check CHECK (purchase_price > 0 AND price > purchase_price),
    FOREIGN KEY (business_id) REFERENCES businesses (id),
    FOREIGN KEY (item_type_id) REFERENCES item_types (id)
);

CREATE TABLE IF NOT EXISTS orders
(
    id             SERIAL PRIMARY KEY,
    customer_phone VARCHAR(10) NOT NULL,
    business_id    INT         NOT NULL,
    date           DATE        NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (customer_phone) REFERENCES customers (phone),
    FOREIGN KEY (business_id) REFERENCES businesses (id)
);

CREATE TABLE IF NOT EXISTS order_items
(
    id       SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    item_id  INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT order_items_quantity_check CHECK (quantity > 0),
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (item_id) REFERENCES items (id)
)



