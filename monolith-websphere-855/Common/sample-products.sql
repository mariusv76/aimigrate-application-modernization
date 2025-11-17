-- Sample product data for testing
-- Insert categories
INSERT INTO CATEGORY (CAT_ID, CAT_NAME, PARENT_CAT) VALUES (1, 'Electronics', NULL) ON CONFLICT DO NOTHING;
INSERT INTO CATEGORY (CAT_ID, CAT_NAME, PARENT_CAT) VALUES (2, 'Movies', NULL) ON CONFLICT DO NOTHING;
INSERT INTO CATEGORY (CAT_ID, CAT_NAME, PARENT_CAT) VALUES (3, 'Computers', 1) ON CONFLICT DO NOTHING;
INSERT INTO CATEGORY (CAT_ID, CAT_NAME, PARENT_CAT) VALUES (4, 'Phones', 1) ON CONFLICT DO NOTHING;

-- Insert products
INSERT INTO PRODUCT (PRODUCT_ID, PRICE, NAME, DESCRIPTION, IMAGE) VALUES 
(1, 999.99, 'Laptop Pro', 'High-performance laptop with 16GB RAM', 'images/laptop.jpg'),
(2, 599.99, 'Smartphone X', 'Latest smartphone with 5G', 'images/phone.jpg'),
(3, 299.99, 'Wireless Headphones', 'Noise-canceling headphones', 'images/headphones.jpg'),
(4, 19.99, 'Action Movie Pack', 'Collection of action movies', 'images/movies.jpg'),
(5, 1499.99, 'Desktop Workstation', 'Powerful desktop for professionals', 'images/desktop.jpg')
ON CONFLICT DO NOTHING;

-- Link products to categories
INSERT INTO PROD_CAT (CAT_ID, PRODUCT_ID) VALUES 
(3, 1),  -- Laptop in Computers
(4, 2),  -- Smartphone in Phones
(1, 3),  -- Headphones in Electronics
(2, 4),  -- Movies in Movies
(3, 5)   -- Desktop in Computers
ON CONFLICT DO NOTHING;
