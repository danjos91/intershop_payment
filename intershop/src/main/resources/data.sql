INSERT INTO users (username, password, email, created_at) VALUES
('currentUser', '$2a$10$7sKRup0OrLU0EHFj7dsTb.6nP01VmkRn9yzfRhRaJsxbZxAihEYqm', 'user@example.com', CURRENT_TIMESTAMP),
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@example.com', CURRENT_TIMESTAMP),
('user1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'user1@example.com', CURRENT_TIMESTAMP),
('user2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'user2@example.com', CURRENT_TIMESTAMP);

INSERT INTO items (title, description, price, img_path, stock, created_at) VALUES
('Laptop', 'High-performance laptop with latest specifications', 999.99, 'images/laptop.jpg', 10, CURRENT_TIMESTAMP),
('Smartphone', 'Latest smartphone with advanced features', 699.99, 'images/smartphone.jpg', 15, CURRENT_TIMESTAMP),
('Headphones', 'Wireless noise-canceling headphones', 199.99, 'images/headphones.jpg', 20, CURRENT_TIMESTAMP),
('Tablet', '10-inch tablet with high-resolution display', 399.99, 'images/tablet.jpg', 8, CURRENT_TIMESTAMP),
('Smartwatch', 'Fitness tracking smartwatch', 299.99, 'images/smartwatch.jpg', 12, CURRENT_TIMESTAMP),
('Laptop2', 'High-performance laptop with latest specifications', 999.99, 'images/laptop.jpg', 10, CURRENT_TIMESTAMP),
('Smartphone2', 'Latest smartphone with advanced features', 699.99, 'images/smartphone.jpg', 15, CURRENT_TIMESTAMP),
('Headphones2', 'Wireless noise-canceling headphones', 199.99, 'images/headphones.jpg', 20, CURRENT_TIMESTAMP),
('Tablet2', '10-inch tablet with high-resolution display', 399.99, 'images/tablet.jpg', 8, CURRENT_TIMESTAMP),
('Smartwatch2', 'Fitness tracking smartwatch', 299.99, 'images/smartwatch.jpg', 12, CURRENT_TIMESTAMP),
('Laptop3', 'High-performance laptop with latest specifications', 999.99, 'images/laptop.jpg', 10, CURRENT_TIMESTAMP),
('Smartphone3', 'Latest smartphone with advanced features', 699.99, 'images/smartphone.jpg', 15, CURRENT_TIMESTAMP),
('Headphones3', 'Wireless noise-canceling headphones', 199.99, 'images/headphones.jpg', 20, CURRENT_TIMESTAMP),
('Tablet3', '10-inch tablet with high-resolution display', 399.99, 'images/tablet.jpg', 8, CURRENT_TIMESTAMP),
('Smartwatch3', 'Fitness tracking smartwatch', 299.99, 'images/smartwatch.jpg', 12, CURRENT_TIMESTAMP);

-- Добавляем тестовые данные для корзины пользователя currentUser (ID = 1)
INSERT INTO cart_items (user_id, item_id, quantity, created_at) VALUES
(1, 1, 2, CURRENT_TIMESTAMP),
(1, 3, 1, CURRENT_TIMESTAMP); 