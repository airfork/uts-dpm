-- password is - testAccount
INSERT INTO users (managerid, username, password, firstname, lastname, fulltime, changed, points, added)
VALUES (1, 'test@account.com', '$2a$10$vg3C7Eir1IgaYYAOoZQw6.N6ilmYU6DcghHuym3NYWA2mzVT9ryqK', 'Test',
        'Account', false, true, 0, now());

INSERT INTO users (managerid, username, password, firstname, lastname, fulltime, changed, points, added)
VALUES (1, 'jane@doe.com', '$2a$10$vg3C7Eir1IgaYYAOoZQw6.N6ilmYU6DcghHuym3NYWA2mzVT9ryqK', 'Jane',
        'Doe', false, true, 0, now());

INSERT INTO users (managerid, username, password, firstname, lastname, fulltime, changed, points, added)
VALUES (1, 'john@doe.com', '$2a$10$vg3C7Eir1IgaYYAOoZQw6.N6ilmYU6DcghHuym3NYWA2mzVT9ryqK', 'John',
        'Doe', true, true, 0, now());

-- Admin
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- Manager
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);

-- Analyst
INSERT INTO user_roles (user_id, role_id) VALUES (3, 3);
