-- Insert mock customer data
INSERT INTO customers (customer_id, email, password, first_name, last_name, phone_number)
VALUES 
(1, 'customer1@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'John', 'Doe', '+1234567890'),
(2, 'customer2@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Jane', 'Smith', '+1987654321');

-- Insert mock wallet for merchant
INSERT INTO wallet (wallet_id, balance, created_at, updated_at)
VALUES (1, 1000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert mock merchant data
INSERT INTO merchant (merchant_id, email, password, first_name, last_name, phone_number, wallet_id)
VALUES 
(1, 'test.merchant@cartlink.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Test', 'Merchant', '+1122334455', 1);

-- Insert mock products (all required fields)
INSERT INTO product (product_id, name, description, price, production_year, type, units_in_stock)
VALUES 
(2001, 'Test Product 1', 'Description for test product 1', 99.99, 2023, 'ELECTRONICS', 100),
(2002, 'Test Product 2', 'Description for test product 2', 149.99, 2023, 'ELECTRONICS', 50);

-- Insert mock merchant product data
INSERT INTO merchant_product (id, merchant_id, product_id, stock)
VALUES 
(2001, 151, 2001, 100),
(2002, 151, 2002, 50);

-- Insert mock orders (all required fields)
INSERT INTO orders (order_id, customer_id, merchant_product_id, status, order_size, paid, created_at, updated_at)
VALUES 
(3001, 1, 2001, 'DELIVERED', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3002, 2, 2002, 'DELIVERED', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert mock reviews for existing merchant (ID: 151)
INSERT INTO review (review_id, merchant_id, customer_id, rating, comment, created_at, updated_at)
VALUES 
(4001, 151, 1, 5, 'Excellent service and fast delivery!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4002, 151, 2, 4, 'Good products but shipping was a bit slow.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4003, 151, 3, 5, 'Very satisfied with the quality of products.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4004, 151, 4, 3, 'Average experience, could be better.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert mock complaints
INSERT INTO complaints (complaint_id, order_id, customer_id, title, description, category, status, created_at, resolved_at)
VALUES 
(5001, 3001, 1, 'Damaged Product', 'Received the package with damaged items', 'PRODUCT_QUALITY', 'PENDING', CURRENT_TIMESTAMP, NULL),
(5002, 3002, 2, 'Wrong Item Delivered', 'Received different item than ordered', 'WRONG_ITEM', 'RESOLVED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(5003, 3001, 1, 'Late Delivery', 'Package arrived 3 days later than expected', 'DELIVERY', 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL); 