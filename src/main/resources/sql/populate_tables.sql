INSERT INTO businesses(owner_email)
VALUES ('john.doe@example.com'),
       ('jane.doe@example.com'),
       ('john.smith@example.com'),
       ('jane.smith@example.com');

INSERT INTO customers (name, phone)
VALUES ('John Doe', '1111111111'),
       ('Jane Doe', '2222222222'),
       ('John Smith', '1234567890');

INSERT INTO item_types (name, description)
VALUES ('Electronics', 'Electronic devices'),
       ('Clothing', 'Clothing items'),
       ('Furniture', 'Furniture items'),
       ('Appliances', 'Home appliances'),
       ('Books', 'Books and magazines');

INSERT INTO items (business_id, name, description, purchase_price, price, currency, item_type_id, image_filename)
VALUES (2, 'Laptop', 'Laptop computer', 500.00, 800.00, 'USD', 1, null),
       (1, 'Desktop', 'Desktop computer', 400.00, 700.00, 'USD', 1, null),
       (1, 'Smartphone', 'Smartphone device', 300.00, 500.00, 'USD', 1, 'https://example.com/smartphone.jpg'),
       (2, 'Tablet', 'Tablet device', 200.00, 300.00, 'USD', 1, 'https://example.com/tablet.jpg'),
       (1, 'T-shirt', 'T-shirt', 10.00, 20.00, 'USD', 2, 'https://example.com/tshirt.jpg'),
       (3, 'Jeans', 'Jeans', 20.00, 40.00, 'USD', 2, 'https://example.com/jeans.jpg'),
       (4, 'Dress', 'Dress', 30.00, 60.00, 'USD', 2,'https://example.com/dress.jpg'),
       (1, 'Sweater', 'Sweater', 40.00, 80.00, 'USD', 2, 'https://example.com/sweater.jpg'),
       (2, 'Sofa', 'Sofa', 300.00, 500.00, 'USD', 3, 'https://example.com/sofa.jpg'),
       (3, 'Chair', 'Chair', 50.00, 100.00, 'USD', 3, 'https://example.com/chair.jpg'),
       (1, 'Refrigerator', 'Refrigerator', 500.00, 800.00, 'USD', 4, 'https://example.com/refrigerator.jpg'),
       (2, 'Washing machine', 'Washing machine', 300.00, 500.00, 'USD', 4, 'https://example.com/washing_machine.jpg'),
       (3, 'Book', 'Book', 5.00, 10.00, 'USD', 5, 'https://example.com/book.jpg');

INSERT INTO orders (business_id, customer_phone, date) -- 1000 orders for business 1
VALUES
    (1, '1111111111', '2022-01-01'),
    (1, '2222222222', '2022-01-02');


INSERT INTO order_items (order_id, item_id, quantity) -- 1000 items for order 1
VALUES (1, 1, 2),
       (1, 2, 1),
       (2, 3, 3),
       (2, 4, 2);

INSERT INTO expense_categories (name)
VALUES ('Advertising'),
       ('Rent'),
       ('Utilities'),
       ('Salaries'),
       ('Marketing'),
       ('Office supplies');

INSERT INTO expenses (category, business_id, date, amount, currency, comment)
VALUES ('Advertising', 1, '2022-01-01', 1000.00, 'USD', 'Rent payment'),
       ('Advertising', 1, '2022-01-02', 200.00, 'USD', 'Electricity bill'),
         ('Advertising', 1, '2022-01-03', 500.00, 'USD', 'Employee salaries'),
         ('Advertising', 1, '2022-01-04', 300.00, 'USD', 'Marketing campaign'),
         ('Advertising', 1, '2022-01-05', 100.00, 'USD', 'Office supplies');



-- End of file
