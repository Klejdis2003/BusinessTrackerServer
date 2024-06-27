INSERT INTO businesses(owner_email) VALUES
( 'john.doe@example.com'),
( 'jane.doe@example.com'),
( 'john.smith@example.com'),
( 'jane.smith@example.com');

INSERT INTO  customers (name, phone) VALUES
('John Doe', '1111111111'),
('Jane Doe', '2222222222'),
('John Smith', '1234567890');

INSERT INTO item_types (name, description) VALUES
('Electronics', 'Electronic devices'),
('Clothing', 'Clothing items'),
('Furniture', 'Furniture items'),
('Appliances', 'Home appliances'),
('Books', 'Books and magazines');

INSERT INTO items (business_id, name, description, purchase_price, price, item_type_id) VALUES
(2,'Laptop', 'Laptop computer', 500.00, 800.00, 1),
(1, 'Smartphone', 'Smartphone device', 300.00, 500.00, 1),
(2, 'Tablet', 'Tablet device', 200.00, 300.00, 1),
(1, 'T-shirt', 'T-shirt', 10.00, 20.00, 2),
(3, 'Jeans', 'Jeans', 20.00, 40.00, 2),
(4, 'Dress', 'Dress', 30.00, 60.00, 2),
(3, 'Sofa', 'Sofa', 300.00, 500.00, 3),
(4, 'Chair', 'Chair', 50.00, 100.00, 3),
(1, 'Refrigerator', 'Refrigerator', 500.00, 800.00, 4),
(2, 'Washing machine', 'Washing machine', 300.00, 500.00, 4),
(3, 'Book', 'Book', 5.00, 10.00, 5),
(4, 'Magazine', 'Magazine', 2.00, 5.00, 5);

INSERT INTO orders (business_id, customer_phone, date) VALUES
(1, '1111111111', '2021-01-01'),
(1, '2222222222', '2021-01-02'),
(2, '1234567890', '2021-01-03'),
(2, '1111111111', '2021-01-04');

INSERT INTO order_items (order_id, item_id, quantity) VALUES
(1, 1, 1),
(1, 2, 1),
(2, 3, 1),
(2, 4, 1);


-- End of file
