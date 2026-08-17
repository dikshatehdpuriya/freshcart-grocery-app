CREATE DATABASE IF NOT EXISTS freshcart_db;
USE freshcart_db;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category VARCHAR(50) NOT NULL,
  price DECIMAL(6,2) NOT NULL,
  unit VARCHAR(30) NOT NULL,
  image_url VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_code VARCHAR(20) NOT NULL UNIQUE,
  user_id INT NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  address VARCHAR(300) NOT NULL,
  delivery_date DATE NOT NULL,
  delivery_slot VARCHAR(50) NOT NULL,
  subtotal DECIMAL(8,2) NOT NULL,
  delivery_fee DECIMAL(8,2) NOT NULL,
  total DECIMAL(8,2) NOT NULL,
  placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  product_name VARCHAR(100) NOT NULL,
  price DECIMAL(6,2) NOT NULL,
  quantity INT NOT NULL,
  line_total DECIMAL(8,2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);

INSERT INTO products (name, category, price, unit, image_url) VALUES
('Fresh Bananas',    'Fruits',     0.59, 'per lb',        'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&q=80'),
('Red Apples',       'Fruits',     1.99, 'per lb',        'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&q=80'),
('Strawberries',     'Fruits',     3.49, 'per box',       'https://images.unsplash.com/photo-1518635017498-87f514b751ba?w=400&q=80'),
('Carrots',          'Vegetables', 1.29, 'per lb',        'https://images.unsplash.com/photo-1447175008436-054170c2e979?w=400&q=80'),
('Broccoli',         'Vegetables', 2.19, 'per lb',        'https://images.unsplash.com/photo-1584270354949-1f7e5b6c7d3b?w=400&q=80'),
('Tomatoes',         'Vegetables', 2.49, 'per lb',        'https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400&q=80'),
('Whole Milk',       'Dairy',      3.29, 'per gallon',    'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400&q=80'),
('Cheddar Cheese',   'Dairy',      4.99, 'per block',     'https://images.unsplash.com/photo-1618164436241-4473940d1f5c?w=400&q=80'),
('Greek Yogurt',     'Dairy',      4.49, 'per 32oz tub',  'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&q=80'),
('Sourdough Bread',  'Bakery',     3.99, 'per loaf',      'https://images.unsplash.com/photo-1585478259715-4d3a5f3a3f3e?w=400&q=80'),
('Croissants',       'Bakery',     5.49, 'pack of 4',     'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400&q=80'),
('Blueberry Muffins','Bakery',     4.29, 'pack of 6',     'https://images.unsplash.com/photo-1607958996333-41aef7caefaa?w=400&q=80');
