CREATE TABLE businesses(
    id SERIAL PRIMARY KEY,
    owner_email VARCHAR(50) UNIQUE NOT NULL,
    CONSTRAINT businesses_owner_email_check CHECK (owner_email ~* '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$')
);


CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS item_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS items (
    id SERIAL PRIMARY KEY,
    business_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    purchase_price DECIMAL(8) NOT NULL,
    price DECIMAL(8) NOT NULL,
    item_type_id INT NOT NULL,
    FOREIGN KEY (business_id) REFERENCES businesses(id),
    FOREIGN KEY (item_type_id) REFERENCES item_types(id)
);

CREATE TABLE IF NOT EXISTS ORDERS (
    id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    business_id INT NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE TABLE IF NOT EXISTS ORDER_ITEMS (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (item_id) REFERENCES items(id)
);



