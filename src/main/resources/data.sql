INSERT INTO users (`email`,`password`, `username`) VALUES ('admin@bg', '$2a$10$saKJDrMZPxt1u8iKS74G5.JCH1A7FNRh9IdKd4xlaipsArtYqjadS', 'adminuser');
INSERT INTO users (`email`,`password`, `username`) VALUES ('user@bg', '$2a$10$saKJDrMZPxt1u8iKS74G5.JCH1A7FNRh9IdKd4xlaipsArtYqjadS', 'user');
INSERT INTO roles (`name`) VALUES ('ROLE_ADMIN');
INSERT INTO roles (`name`) VALUES ('ROLE_USER');
INSERT INTO user_roles (`user_id`, `role_id`) VALUES (1,1);
INSERT INTO user_roles (`user_id`, `role_id`) VALUES (2,2);

INSERT INTO loan_types (months, total_amount, interest) VALUES (6, 10000, 8);
INSERT INTO loan_types (months, total_amount, interest) VALUES (12, 15000, 7);
INSERT INTO loan_types (months, total_amount, interest) VALUES (18, 20000, 6);
INSERT INTO loan_types (months, total_amount, interest) VALUES (30, 30000, 5.5);