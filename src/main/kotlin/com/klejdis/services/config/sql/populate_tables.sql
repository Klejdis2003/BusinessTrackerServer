INSERT INTO  customers (name, phone) VALUES
('John Doe', '1234567890'),
('Jane Doe', '0987654321'),
('John Smith', '1231231234'),
('Jane Smith', '0980980987'),
('John Johnson', '1234567890'),
('Jane Johnson', '0987654321'),
('John Brown', '1231231234'),
('Jane Brown', '0980980987'),
('John White', '1234567890'),
('Jane White', '0987654321');

INSERT INTO item_types (name, description) VALUES
('Electronics', 'Electronic devices'),
('Clothing', 'Clothing items'),
('Furniture', 'Furniture items'),
('Appliances', 'Home appliances'),
('Books', 'Books and magazines');

INSERT INTO items (name, description, purchase_price, price, item_type_id) VALUES
('Laptop', 'Laptop computer', 500.00, 800.00, 1),
('Smartphone', 'Smartphone device', 300.00, 500.00, 1),
('Tablet', 'Tablet device', 200.00, 300.00, 1),
('T-shirt', 'T-shirt', 10.00, 20.00, 2),
('Jeans', 'Jeans', 20.00, 40.00, 2),
('Dress', 'Dress', 30.00, 60.00, 2),
('Sofa', 'Sofa', 300.00, 500.00, 3),
('Bed', 'Bed', 200.00, 400.00, 3),
('Table', 'Table', 100.00, 200.00, 3),
('Refrigerator', 'Refrigerator', 500.00, 800.00, 4),
('Washing machine', 'Washing machine', 400.00, 700.00, 4),
('Microwave', 'Microwave oven', 100.00, 200.00, 4),
('Java Programming', 'Java Programming book', 20.00, 40.00, 5),
('Kotlin Programming', 'Kotlin Programming book', 20.00, 40.00, 5),
('Spring Framework', 'Spring Framework book', 30.00, 60.00, 5);

INSERT INTO orders (customer_id, order_date) VALUES
(1, '2021-01-01'),
(2, '2021-01-02'),
(3, '2021-01-03'),
(4, '2021-01-04'),
(5, '2021-01-05'),
(6, '2021-01-06'),
(7, '2021-01-07'),
(8, '2021-01-08'),
(9, '2021-01-09'),
(10, '2021-01-10');

INSERT INTO order_items (order_id, item_id, quantity) VALUES
(1, 1, 1),
(1, 2, 2),
(2, 3, 1),
(2, 4, 2),
(3, 5, 1),
(3, 6, 2),
(4, 7, 1),
(4, 8, 2),
(5, 9, 1),
(5, 10, 2),
(6, 11, 1),
(6, 12, 2),
(7, 13, 1),
(7, 14, 2),
(8, 1, 1),
(8, 2, 2),
(9, 3, 1),
(9, 4, 2),
(10, 5, 1),
(10, 6, 2);

-- End of file
