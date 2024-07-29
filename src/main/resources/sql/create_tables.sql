CREATE TABLE businesses
(
    id          SERIAL PRIMARY KEY,
    owner_email VARCHAR(50) UNIQUE NOT NULL,
    CONSTRAINT businesses_owner_email_check CHECK (owner_email ~* '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$')
);



CREATE TABLE IF NOT EXISTS business_preferences
(
    business_id INT PRIMARY KEY,
    currency   VARCHAR(3) NOT NULL,
    tax_rate    DECIMAL(3) NOT NULL DEFAULT 0,
    FOREIGN KEY (business_id) REFERENCES businesses (id)
);


CREATE TABLE IF NOT EXISTS customers
(
    phone VARCHAR(10) NOT NULL PRIMARY KEY,
    name  VARCHAR(50) NOT NULL,
    CONSTRAINT customers_name_check CHECK (name ~* '^[a-zA-Z ]+$')
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
    image_filename TEXT   UNIQUE  DEFAULT NULL,
    purchase_price DECIMAL(8)  NOT NULL,
    price          DECIMAL(8)  NOT NULL,
    currency       VARCHAR(3)  NOT NULL,
    item_type_id   INT         NOT NULL,
    CONSTRAINT items_price_check CHECK (purchase_price > 0 AND price > purchase_price),
    FOREIGN KEY (business_id) REFERENCES businesses (id),
    FOREIGN KEY (item_type_id) REFERENCES item_types (id)
);

CREATE TABLE IF NOT EXISTS expense_categories
(
    name VARCHAR(50) NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS expenses
(
    id          SERIAL PRIMARY KEY,
    category    VARCHAR(50) NOT NULL,
    business_id INT         NOT NULL,
    date        DATE        NOT NULL DEFAULT CURRENT_DATE,
    amount      DECIMAL(8)  NOT NULL,
    currency    VARCHAR(3)    NOT NULL,
    comment     TEXT        NOT NULL DEFAULT '',
    FOREIGN KEY (business_id) REFERENCES businesses (id),
    FOREIGN KEY (category) REFERENCES expense_categories (name)
);

CREATE TABLE IF NOT EXISTS orders
(
    id             SERIAL PRIMARY KEY,
    customer_phone VARCHAR(10) NOT NULL,
    business_id    INT         NOT NULL,
    date           DATE        NOT NULL DEFAULT CURRENT_DATE,
    total          DECIMAL(8)  NOT NULL DEFAULT 2000,
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



